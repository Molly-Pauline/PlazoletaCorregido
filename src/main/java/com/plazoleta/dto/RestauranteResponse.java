package com.plazoleta.dto;

import com.plazoleta.domain.Restaurante;

public record RestauranteResponse(Long id, String nombre, String nit, String direccion, String telefono,
                                  String urlLogo, Long idPropietario) {
    public static RestauranteResponse from(Restaurante restaurante) {
        return new RestauranteResponse(restaurante.getId(), restaurante.getNombre(), restaurante.getNit(),
                restaurante.getDireccion(), restaurante.getTelefono(), restaurante.getUrlLogo(),
                restaurante.getIdPropietario());
    }
}