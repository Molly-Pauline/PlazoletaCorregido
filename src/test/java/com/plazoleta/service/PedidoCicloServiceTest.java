package com.plazoleta.service;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.Pedido;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PedidoRepository;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas de HU13: asignación del pedido al empleado. */
class PedidoCicloServiceTest {

    private static final int ID_PEDIDO = 5;
    private static final int ID_CLIENTE = 25;
    private static final int ID_RESTAURANTE = 77;

    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final PedidoServiceImpl service = new PedidoServiceImpl(pedidoRepository, mock(PlatoRepository.class),
            mock(RestauranteRepository.class), mock(PedidoCreationLock.class));

    private final AuthenticatedUser empleado = new AuthenticatedUser(30, "EMPLEADO", ID_RESTAURANTE);
    private final AuthenticatedUser cliente = new AuthenticatedUser(ID_CLIENTE, "CLIENTE");
    private Pedido pedido;

    @BeforeEach
    void prepararPedido() {
        Restaurante restaurante = mock(Restaurante.class);
        when(restaurante.getId()).thenReturn(ID_RESTAURANTE);
        pedido = new Pedido(ID_CLIENTE, restaurante, 50000);
        when(pedidoRepository.findByIdParaActualizar(ID_PEDIDO)).thenReturn(Optional.of(pedido));
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
}
