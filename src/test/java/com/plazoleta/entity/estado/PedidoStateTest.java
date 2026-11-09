package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Comprueba todas las transiciones permitidas y prohibidas del patrón State. */
class PedidoStateTest {

    @Test
    void permiteElCicloCompletoDelPedido() {
        assertEquals(EstadoPedido.EN_PREPARACION, PedidoStates.de(EstadoPedido.PENDIENTE).asignar());
        assertEquals(EstadoPedido.LISTO, PedidoStates.de(EstadoPedido.EN_PREPARACION).marcarListo());
        assertEquals(EstadoPedido.ENTREGADO, PedidoStates.de(EstadoPedido.LISTO).entregar());
        assertEquals(EstadoPedido.CANCELADO, PedidoStates.de(EstadoPedido.PENDIENTE).cancelar());
    }

    @Test
    void cadaEstadoSoloPermiteSusTransiciones() {
        comprobar(PedidoState::asignar, EstadoPedido.PENDIENTE);
        comprobar(PedidoState::marcarListo, EstadoPedido.EN_PREPARACION);
        comprobar(PedidoState::entregar, EstadoPedido.LISTO);
        comprobar(PedidoState::cancelar, EstadoPedido.PENDIENTE);
    }

    private void comprobar(Function<PedidoState, EstadoPedido> transicion, EstadoPedido unicoPermitido) {
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (estado != unicoPermitido) {
                assertThrows(DomainException.class, () -> transicion.apply(PedidoStates.de(estado)), estado.name());
            }
        }
    }
}
