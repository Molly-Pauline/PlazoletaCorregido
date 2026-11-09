package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;

/** Estado final: un pedido cancelado no admite más cambios. */
class CanceladoState implements PedidoState {

    @Override
    public EstadoPedido estado() {
        return EstadoPedido.CANCELADO;
    }
}
