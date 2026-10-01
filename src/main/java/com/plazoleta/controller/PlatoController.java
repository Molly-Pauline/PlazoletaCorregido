package com.plazoleta.controller;

import com.plazoleta.domain.Plato;
import com.plazoleta.dto.PlatoApiResponse;
import com.plazoleta.dto.PlatoRequest;
import com.plazoleta.dto.PlatoResponse;
import com.plazoleta.dto.PlatoUpdateRequest;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platos")
public class PlatoController {

    private final PlatoService platoService;

    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatoApiResponse crear(@Valid @RequestBody PlatoRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        Plato plato = platoService.crear(request, user);
        return new PlatoApiResponse(true, "Plato creado", PlatoResponse.from(plato));
    }

    @PutMapping("/{idPlato}")
    public PlatoApiResponse modificar(@PathVariable Long idPlato, @Valid @RequestBody PlatoUpdateRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        Plato plato = platoService.modificar(idPlato, request, user);
        return new PlatoApiResponse(true, "Plato modificado", PlatoResponse.from(plato));
    }
}