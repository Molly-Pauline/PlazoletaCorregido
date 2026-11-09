package com.plazoleta.service;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.Pedido;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.entity.Usuario;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PedidoRepository;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.repository.UsuarioRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas de HU13 a HU15: ciclo del pedido y PIN. */
class PedidoCicloServiceTest {

    private static final int ID_PEDIDO = 5;
    private static final int ID_CLIENTE = 25;
    private static final int ID_RESTAURANTE = 77;

    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final NotificacionSmsService sms = mock(NotificacionSmsService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final PedidoServiceImpl service = new PedidoServiceImpl(pedidoRepository, mock(PlatoRepository.class),
            mock(RestauranteRepository.class), mock(PedidoCreationLock.class),
            usuarioRepository, encoder, sms);

    private final AuthenticatedUser empleado = new AuthenticatedUser(30, "EMPLEADO", ID_RESTAURANTE);
    private final AuthenticatedUser cliente = new AuthenticatedUser(ID_CLIENTE, "CLIENTE");
    private Pedido pedido;

    @BeforeEach
    void prepararPedido() {
        Restaurante restaurante = mock(Restaurante.class);
        when(restaurante.getId()).thenReturn(ID_RESTAURANTE);
        when(restaurante.getNombre()).thenReturn("La Casona");
        pedido = new Pedido(ID_CLIENTE, restaurante, 50000);
        when(pedidoRepository.findByIdParaActualizar(ID_PEDIDO)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.findById(ID_PEDIDO)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findById(ID_CLIENTE))
                .thenReturn(Optional.of(Usuario.builder().celular("+573001234569").build()));
    }

    @Test
    void hu13AsignaAlEmpleadoYPasaAPreparacion() {
        var respuesta = service.asignar(ID_PEDIDO, empleado);

        assertEquals("EN_PREPARACION", respuesta.estado());
        assertEquals(30, respuesta.idEmpleado());
    }

    @Test
    void hu13RechazaPedidoDeOtroRestaurante() {
        DomainException error = assertThrows(DomainException.class,
                () -> service.asignar(ID_PEDIDO, new AuthenticatedUser(31, "EMPLEADO", 99)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    @Test
    void hu14EnviaPinPorSmsYGuardaSoloSuHash() {
        pedido.asignarEmpleado(30);

        var respuesta = service.marcarListo(ID_PEDIDO, empleado);

        assertEquals("LISTO", respuesta.estado());
        String pin = pinEnviado();
        assertTrue(encoder.matches(pin, pedido.getPinHash()));
    }

    @Test
    void hu14NoAplicaAUnPedidoPendiente() {
        assertThrows(DomainException.class, () -> service.marcarListo(ID_PEDIDO, empleado));

        verify(sms, never()).enviar(any(), any());
    }

    @Test
    void hu15EntregaConElPinCorrecto() {
        pedido.asignarEmpleado(30);
        service.marcarListo(ID_PEDIDO, empleado);

        var respuesta = service.entregar(ID_PEDIDO, pinEnviado(), empleado);

        assertEquals("ENTREGADO", respuesta.estado());
        assertNull(pedido.getPinHash());
    }

    @Test
    void hu15RechazaUnPinIncorrecto() {
        pedido.asignarEmpleado(30);
        service.marcarListo(ID_PEDIDO, empleado);
        String incorrecto = pinEnviado().equals("000000") ? "000001" : "000000";

        DomainException error = assertThrows(DomainException.class,
                () -> service.entregar(ID_PEDIDO, incorrecto, empleado));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(EstadoPedido.LISTO, pedido.getEstado());
    }

    @Test
    void hu15SoloEntregaPedidosListos() {
        DomainException error = assertThrows(DomainException.class,
                () -> service.entregar(ID_PEDIDO, "123456", empleado));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void hu15UnPedidoEntregadoNoCambiaDeEstado() {
        pedido.asignarEmpleado(30);
        pedido.marcarListo("hash");
        pedido.entregar();

        assertThrows(DomainException.class, () -> service.asignar(ID_PEDIDO, empleado));
        assertThrows(DomainException.class, () -> service.marcarListo(ID_PEDIDO, empleado));
        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    }

    /** Lee del SMS capturado el PIN de seis dígitos que recibió el cliente. */
    private String pinEnviado() {
        ArgumentCaptor<String> mensaje = ArgumentCaptor.forClass(String.class);
        verify(sms, times(1)).enviar(eq("+573001234569"), mensaje.capture());
        return mensaje.getValue().substring(mensaje.getValue().length() - 6);
    }
}
