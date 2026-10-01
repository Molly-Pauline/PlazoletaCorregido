package com.plazoleta.controller;

import com.plazoleta.domain.Restaurante;
import com.plazoleta.dto.RestauranteApiResponse;
import com.plazoleta.dto.RestauranteRequest;
import com.plazoleta.dto.RestauranteResponse;
import com.plazoleta.service.RestauranteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurantes")
public class RestauranteController {

    private final RestauranteService restauranteService;

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestauranteApiResponse crear(@Valid @RequestBody RestauranteRequest request) {
        Restaurante restaurante = restauranteService.crear(request);
        return new RestauranteApiResponse(true, "Restaurante creado", RestauranteResponse.from(restaurante));
    }
}