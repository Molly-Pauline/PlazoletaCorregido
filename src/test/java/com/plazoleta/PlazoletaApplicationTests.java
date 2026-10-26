package com.plazoleta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración de contexto y autorización HTTP usando JWT y la configuración H2. */
@SpringBootTest
@AutoConfigureMockMvc
class PlazoletaApplicationTests {

        /** Clave de prueba con longitud suficiente para firmar JWT HS256. */
        private static final String AUTH_SECRET = "test-secret-with-at-least-32-bytes";

        /** Cliente MVC que ejecuta solicitudes contra los filtros y controladores reales de Spring. */
    @Autowired
    private MockMvc mockMvc;

        /** Comprueba que el contexto Spring Boot y sus beans se puedan construir. */
        @Test
    void contextLoads() {
    }

        /** Distingue una solicitud anónima (401) de una identidad autenticada con rol incorrecto (403) en HU12. */
        @Test
    void orderRoutesRequireAuthenticationAndEmployeeRole() throws Exception {
                // Sin Bearer no existe principal y el endpoint protegido debe rechazar la solicitud.
        mockMvc.perform(get("/pedidos").param("estado", "PENDIENTE"))
                .andExpect(status().isUnauthorized());
                // El token identifica un propietario válido, pero la ruta de consulta requiere rol EMPLEADO.
        mockMvc.perform(get("/pedidos").param("estado", "PENDIENTE")
                        .header("Authorization", "Bearer " + token(7, "PROPIETARIO", null)))
                .andExpect(status().isForbidden());
    }

        /** Comprueba que el endpoint interno requiere tanto identidad propietaria como credencial entre servicios. */
        @Test
    void serviceOwnershipEndpointRequiresBothProprietorAndInternalToken() throws Exception {
                // El Bearer de propietario no sustituye al token interno requerido para la llamada de Usuarios.
        mockMvc.perform(get("/restaurantes/1/propietario/7")
                        .header("Authorization", "Bearer " + token(7, "PROPIETARIO", null)))
                .andExpect(status().isUnauthorized());
        // Un token interno correcto no concede acceso a un principal CLIENTE, pues la ruta exige PROPIETARIO.
        mockMvc.perform(get("/restaurantes/1/propietario/7")
                        .header("Authorization", "Bearer " + token(7, "CLIENTE", null))
                        .header("X-Service-Token", "test-internal-service-token-at-least-32-chars"))
                .andExpect(status().isForbidden());
    }

        /** Verifica que la creación de restaurantes está reservada al rol ADMINISTRADOR (HU2). */
        @Test
    void onlyAdministratorCanCreateRestaurant() throws Exception {
                // El cliente autenticado debe recibir 403 antes de procesar el cuerpo enviado.
        mockMvc.perform(post("/restaurantes")
                        .header("Authorization", "Bearer " + token(7, "CLIENTE", null))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    /** Emula el emisor de Usuarios para probar el contrato JWT HS256 compartido.
     * @param userId ID que se coloca en la claim estándar sub
     * @param role rol firmado para construir la autoridad de Spring Security
     * @param restaurantId asociación opcional firmada para las pruebas de empleado
     * @return token compacto JWT firmado con la clave de prueba
     */
    private String token(long userId, String role, Long restaurantId) throws Exception {
        // El header fija HS256 y el payload replica las claims compartidas con el emisor Usuarios.
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String restaurantClaim = restaurantId == null ? "" : ",\"idRestaurante\":" + restaurantId;
        String claims = "{\"sub\":\"" + userId + "\",\"rol\":\"" + role
                + "\",\"exp\":" + (Instant.now().getEpochSecond() + 60) + restaurantClaim + "}";
        String payload = encode(claims);
        // La firma cubre header y payload codificados, en ese orden y separados por un punto.
        String signingInput = header + "." + payload;
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(AUTH_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)));
        return signingInput + "." + signature;
    }

    /** Codifica un segmento JWT usando Base64 URL, que es seguro para encabezados HTTP. */
    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}