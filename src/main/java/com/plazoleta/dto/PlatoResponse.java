package com.plazoleta.dto;

import com.plazoleta.domain.Plato;

public record PlatoResponse(Long id, String nombre, Integer precio, String descripcion, String urlImagen,
                            String categoria, Long idRestaurante, boolean activo) {
    public static PlatoResponse from(Plato plato) {
        return new PlatoResponse(plato.getId(), plato.getNombre(), plato.getPrecio(), plato.getDescripcion(),
                plato.getUrlImagen(), plato.getCategoria(), plato.getRestaurante().getId(), plato.isActivo());
    }
}