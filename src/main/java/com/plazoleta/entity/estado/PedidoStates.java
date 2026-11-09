package com.plazoleta.entity.estado;

import com.plazoleta.entity.EstadoPedido;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Obtiene el objeto State que corresponde al estado guardado en la base de datos. */
public final class PedidoStates {

    /** Los estados no guardan datos, así que basta con una instancia compartida de cada uno. */
    private static final Map<EstadoPedido, PedidoState> ESTADOS = new EnumMap<>(EstadoPedido.class);

    static {
        List.of(new PendienteState(), new EnPreparacionState(), new ListoState(),
                new EntregadoState(), new CanceladoState())
                .forEach(state -> ESTADOS.put(state.estado(), state));
    }

    private PedidoStates() {
    }

    /** Devuelve el comportamiento asociado al estado indicado. */
    public static PedidoState de(EstadoPedido estado) {
        return ESTADOS.get(estado);
    }
}
