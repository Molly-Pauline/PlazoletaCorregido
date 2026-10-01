package com.plazoleta.service;

import com.plazoleta.domain.Restaurante;
import com.plazoleta.dto.RestauranteRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.RestauranteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final UsuarioValidationPort usuarioValidationPort;

    public RestauranteService(RestauranteRepository restauranteRepository, UsuarioValidationPort usuarioValidationPort) {
        this.restauranteRepository = restauranteRepository;
        this.usuarioValidationPort = usuarioValidationPort;
    }

    @Transactional
    public Restaurante crear(RestauranteRequest request) {
        if (restauranteRepository.existsByNit(request.nit())) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Ya existe un restaurante con ese NIT");
        }
        if (!usuarioValidationPort.existePropietarioValido(request.idPropietario())) {
            throw new DomainException(HttpStatus.BAD_REQUEST,
                    "El propietario asociado no existe o no tiene rol PROPIETARIO");
        }
        Restaurante restaurante = new Restaurante(request.nombre(), request.nit(), request.direccion(),
                request.telefono(), request.urlLogo(), request.idPropietario());
        return restauranteRepository.save(restaurante);
    }
}