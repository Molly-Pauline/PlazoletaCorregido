package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;

/** Pedido en preparación: solo puede pasar a listo. */
class EnPreparacionState implements PedidoState {

    @Override
    public EstadoPedido estado() {
        return EstadoPedido.EN_PREPARACION;
    }

    @Override
    public EstadoPedido marcarListo() {
        return EstadoPedido.LISTO;
    }
}
