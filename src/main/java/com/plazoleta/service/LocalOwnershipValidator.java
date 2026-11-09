package com.plazoleta.service;

import org.springframework.stereotype.Component;

@Component
public class LocalOwnershipValidator {
    private final RestauranteService restauranteService;
    public LocalOwnershipValidator(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }
    public boolean propietarioEsDueno(Integer idRestaurante, Integer idPropietario, String bearerToken) {
        return idRestaurante != null && idPropietario != null &&
                restauranteService.perteneceAPropietario(idRestaurante, idPropietario);
    }
}
