package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.exception.DomainException;
import org.springframework.http.HttpStatus;

/**
 * Patrón State: cada estado del pedido decide qué transiciones admite y cuál es el estado siguiente.
 * Por defecto toda transición se rechaza; cada estado concreto redefine solo las que tiene permitidas.
 */
public interface PedidoState {

    /** Mensaje exigido por HU16 cuando el pedido ya no está pendiente. */
    String MENSAJE_NO_CANCELABLE = "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";

    /** Valor persistido que representa este estado. */
    EstadoPedido estado();

    /** HU13: un empleado toma el pedido. */
    default EstadoPedido asignar() {
        throw transicionInvalida("asignar un empleado a");
    }

    /** HU14: la preparación terminó. */
    default EstadoPedido marcarListo() {
        throw transicionInvalida("marcar como listo");
    }

    /** HU15: el cliente recibió el pedido. */
    default EstadoPedido entregar() {
        throw transicionInvalida("entregar");
    }

    /** HU16: el cliente desiste del pedido. */
    default EstadoPedido cancelar() {
        throw new DomainException(HttpStatus.CONFLICT, MENSAJE_NO_CANCELABLE);
    }

    /** Construye el error común de una transición que este estado no admite. */
    private DomainException transicionInvalida(String accion) {
        return new DomainException(HttpStatus.CONFLICT,
                "No se puede " + accion + " un pedido en estado " + estado());
    }
}
