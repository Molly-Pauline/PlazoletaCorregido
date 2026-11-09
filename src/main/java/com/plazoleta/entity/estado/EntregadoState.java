package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;

/** Estado final: un pedido entregado no admite más cambios. */
class EntregadoState implements PedidoState {

    @Override
    public EstadoPedido estado() {
        return EstadoPedido.ENTREGADO;
    }
}
