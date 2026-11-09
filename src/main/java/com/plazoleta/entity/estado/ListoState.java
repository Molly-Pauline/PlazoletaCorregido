package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;

/** Pedido listo: solo puede pasar a entregado. */
class ListoState implements PedidoState {

    @Override
    public EstadoPedido estado() {
        return EstadoPedido.LISTO;
    }

    @Override
    public EstadoPedido entregar() {
        return EstadoPedido.ENTREGADO;
    }
}
