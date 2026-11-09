package com.plazoleta.dto.response;

import com.plazoleta.entity.HistorialPedido;

import java.time.LocalDateTime;

/**
 * Cambio de estado de un pedido que puede consultar su cliente (HU17).
 * @param estadoAnterior estado previo; es nulo cuando se creó el pedido
 * @param estadoNuevo estado resultante del cambio
 * @param idUsuario cliente o empleado que hizo el cambio
 * @param fecha fecha y hora del cambio
 */
public record TrazabilidadResponse(String estadoAnterior, String estadoNuevo, Integer idUsuario, LocalDateTime fecha) {
    /** Convierte el registro persistido en la respuesta pública. */
    public static TrazabilidadResponse from(HistorialPedido registro) {
        return new TrazabilidadResponse(
                registro.getEstadoAnterior() == null ? null : registro.getEstadoAnterior().name(),
                registro.getEstadoNuevo().name(), registro.getIdUsuario(), registro.getFecha());
    }
}
