package com.plazoleta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plazoleta.service.NotificacionSmsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

/** Recorre por HTTP el flujo HU1-HU17: rutas, seguridad, transacciones y persistencia trabajando juntas. */
@SpringBootTest(properties = "app.bootstrap.admin-registration-enabled=true")
@AutoConfigureMockMvc
class PedidoFlujoIntegrationTest {

    private static final String USUARIOS = "/api/v1/usuarios";
    private static final String CLAVE = "ClaveDePrueba123";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @MockitoBean
    private NotificacionSmsService sms;

    @Test
    void recorreElCicloCompletoDelPedido() throws Exception {
        // Cuentas, restaurante, plato y empleado.
        crearCuenta("/administradores", null, cuenta("admin@example.com", "1001"));
        String tokenAdmin = login("admin@example.com");
        JsonNode propietario = crearCuenta("/propietarios", tokenAdmin,
                cuenta("propietario@example.com", "1002").replace("}", ",\"fechaDeNacimiento\":\"1990-01-01T00:00:00.000+0000\"}"));
        String tokenPropietario = login("propietario@example.com");
        int idRestaurante = enviar(HttpMethod.POST, "/restaurantes", tokenAdmin,
                "{\"nombre\":\"La Casona\",\"nit\":\"900123\",\"direccion\":\"Calle 1\",\"telefono\":\"+573001234567\","
                        + "\"urlLogo\":\"https://example.com/l.png\",\"idPropietario\":" + propietario.get("id") + "}", 201)
                .at("/restaurante/id").asInt();
        int idPlato = enviar(HttpMethod.POST, "/platos", tokenPropietario,
                "{\"nombre\":\"Hamburguesa clásica\",\"precio\":25000,\"descripcion\":\"Con queso\","
                        + "\"urlImagen\":\"https://example.com/b.png\",\"categoria\":\"Hamburguesas\",\"idRestaurante\":" + idRestaurante + "}", 201)
                .at("/plato/id").asInt();
        crearCuenta("/empleados", tokenPropietario, cuenta("empleado@example.com", "1003")
                .replace("}", ",\"idRol\":3,\"idRestaurante\":" + idRestaurante + "}"));
        String tokenEmpleado = login("empleado@example.com");
        crearCuenta("/clientes", null, cuenta("cliente@example.com", "1004"));
        String tokenCliente = login("cliente@example.com");
        crearCuenta("/clientes", null, cuenta("otro@example.com", "1005"));
        String tokenOtroCliente = login("otro@example.com");

        String cuerpoPedido = "{\"idRestaurante\":" + idRestaurante + ",\"platos\":[{\"idPlato\":" + idPlato + ",\"cantidad\":2}]}";
        int idPedido = enviar(HttpMethod.POST, "/pedidos", tokenCliente, cuerpoPedido, 201).at("/pedido/id").asInt();
        String pedido = "/pedidos/" + idPedido;

        // Permisos por rol y por dueño.
        enviar(HttpMethod.PATCH, pedido + "/asignar", null, null, 401);
        enviar(HttpMethod.PATCH, pedido + "/asignar", tokenCliente, null, 403);
        enviar(HttpMethod.PATCH, pedido + "/cancelar", tokenEmpleado, null, 403);
        enviar(HttpMethod.PATCH, pedido + "/cancelar", tokenOtroCliente, null, 403);
        enviar(HttpMethod.GET, pedido + "/trazabilidad", tokenOtroCliente, null, 403);
        enviar(HttpMethod.GET, "/pedidos/999999/trazabilidad", tokenCliente, null, 404);

        // El orden del ciclo no se puede saltar.
        enviar(HttpMethod.PATCH, pedido + "/listo", tokenEmpleado, null, 409);
        enviar(HttpMethod.PATCH, pedido + "/entregar", tokenEmpleado, "{\"pin\":\"123456\"}", 409);

        // HU13.
        JsonNode asignado = enviar(HttpMethod.PATCH, pedido + "/asignar", tokenEmpleado, null, 200).get("pedido");
        assertEquals("EN_PREPARACION", asignado.get("estado").asText());
        assertTrue(asignado.get("idEmpleado").isInt());

        // HU16 con un pedido que ya no está pendiente.
        JsonNode rechazo = enviar(HttpMethod.PATCH, pedido + "/cancelar", tokenCliente, null, 409);
        assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", rechazo.get("detail").asText());

        // HU14.
        assertEquals("LISTO", enviar(HttpMethod.PATCH, pedido + "/listo", tokenEmpleado, null, 200).at("/pedido/estado").asText());
        ArgumentCaptor<String> mensaje = ArgumentCaptor.forClass(String.class);
        verify(sms).enviar(anyString(), mensaje.capture());
        String pin = mensaje.getValue().substring(mensaje.getValue().length() - 6);
        String pinIncorrecto = pin.equals("000000") ? "000001" : "000000";

        // HU15.
        enviar(HttpMethod.PATCH, pedido + "/entregar", tokenEmpleado, "{\"pin\":\"abc\"}", 400);
        enviar(HttpMethod.PATCH, pedido + "/entregar", tokenEmpleado, "{\"pin\":\"" + pinIncorrecto + "\"}", 400);
        enviar(HttpMethod.PATCH, pedido + "/entregar", tokenEmpleado, "{\"pin\":\"" + pin + "\"}", 200);
        enviar(HttpMethod.PATCH, pedido + "/listo", tokenEmpleado, null, 409);

        // HU17: creación, asignación, listo y entrega, en ese orden.
        JsonNode cambios = enviar(HttpMethod.GET, pedido + "/trazabilidad", tokenCliente, null, 200);
        assertEquals(4, cambios.size());
        assertTrue(cambios.get(0).get("estadoAnterior").isNull());
        assertEquals("PENDIENTE", cambios.get(0).get("estadoNuevo").asText());
        assertEquals("EN_PREPARACION", cambios.get(1).get("estadoNuevo").asText());
        assertEquals("LISTO", cambios.get(2).get("estadoNuevo").asText());
        assertEquals("ENTREGADO", cambios.get(3).get("estadoNuevo").asText());
        assertNotNull(cambios.get(3).get("fecha").asText());

        // HU16: como el pedido anterior se cerró, el cliente puede pedir de nuevo y cancelar.
        int idSegundo = enviar(HttpMethod.POST, "/pedidos", tokenCliente, cuerpoPedido, 201).at("/pedido/id").asInt();
        assertEquals("CANCELADO", enviar(HttpMethod.PATCH, "/pedidos/" + idSegundo + "/cancelar", tokenCliente, null, 200)
                .at("/pedido/estado").asText());

        // Búsqueda con merge sort y búsqueda binaria.
        String buscar = "/restaurantes/" + idRestaurante + "/platos/buscar?nombre=";
        JsonNode encontrados = enviar(HttpMethod.GET, buscar + "HAMB", tokenCliente, null, 200);
        assertEquals(1, encontrados.size());
        assertEquals(idPlato, encontrados.get(0).get("id").asInt());
        assertEquals(0, enviar(HttpMethod.GET, buscar + "pizza", tokenCliente, null, 200).size());
        enviar(HttpMethod.GET, buscar + "hamb", tokenEmpleado, null, 403);
    }

    private String cuenta(String correo, String documento) {
        return "{\"nombre\":\"Nombre\",\"apellido\":\"Apellido\",\"documentoDeIdentidad\":\"" + documento
                + "\",\"celular\":\"+573001234567\",\"correo\":\"" + correo + "\",\"clave\":\"" + CLAVE + "\"}";
    }

    private JsonNode crearCuenta(String ruta, String token, String cuerpo) throws Exception {
        return enviar(HttpMethod.POST, USUARIOS + ruta, token, cuerpo, 201);
    }

    private String login(String correo) throws Exception {
        return enviar(HttpMethod.POST, USUARIOS + "/login", null,
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + CLAVE + "\"}", 200).get("token").asText();
    }

    /** Envía la solicitud, comprueba el código esperado y devuelve el JSON de la respuesta. */
    private JsonNode enviar(HttpMethod metodo, String ruta, String token, String cuerpo, int esperado) throws Exception {
        MockHttpServletRequestBuilder solicitud = MockMvcRequestBuilders.request(metodo, ruta);
        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }
        if (cuerpo != null) {
            solicitud.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        var respuesta = mvc.perform(solicitud).andReturn().getResponse();
        String texto = respuesta.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(esperado, respuesta.getStatus(), metodo + " " + ruta + " -> " + texto);
        return texto.isBlank() ? json.createObjectNode() : json.readTree(texto);
    }
}
