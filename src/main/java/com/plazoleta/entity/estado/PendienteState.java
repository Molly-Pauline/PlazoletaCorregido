package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;

/** Pedido recién creado: se puede asignar a un empleado o cancelar. */
class PendienteState implements PedidoState {

    @Override
    public EstadoPedido estado() {
        return EstadoPedido.PENDIENTE;
    }

    @Override
    public EstadoPedido asignar() {
        return EstadoPedido.EN_PREPARACION;
    }

    @Override
    public EstadoPedido cancelar() {
        return EstadoPedido.CANCELADO;
    }
}
