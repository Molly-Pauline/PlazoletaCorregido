package com.plazoleta.service;

import com.plazoleta.domain.Plato;
import com.plazoleta.domain.Restaurante;
import com.plazoleta.dto.PlatoRequest;
import com.plazoleta.dto.PlatoUpdateRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatoService {

    private final PlatoRepository platoRepository;
    private final RestauranteRepository restauranteRepository;

    public PlatoService(PlatoRepository platoRepository, RestauranteRepository restauranteRepository) {
        this.platoRepository = platoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    @Transactional
    public Plato crear(PlatoRequest request, AuthenticatedUser user) {
        Restaurante restaurante = restauranteRepository.findById(request.idRestaurante())
                .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "El restaurante no existe"));
        verificarPertenencia(restaurante, user);
        return platoRepository.save(new Plato(request.nombre(), request.precio(), request.descripcion(),
                request.urlImagen(), request.categoria(), restaurante));
    }

    @Transactional
    public Plato modificar(Long idPlato, PlatoUpdateRequest request, AuthenticatedUser user) {
        Plato plato = platoRepository.findById(idPlato)
                .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "El plato no existe"));
        verificarPertenencia(plato.getRestaurante(), user);
        plato.actualizarPrecioYDescripcion(request.precio(), request.descripcion());
        return platoRepository.save(plato);
    }

    private void verificarPertenencia(Restaurante restaurante, AuthenticatedUser user) {
        if (!restaurante.getIdPropietario().equals(user.idUsuario())) {
            throw new DomainException(HttpStatus.FORBIDDEN,
                    "El restaurante no pertenece al propietario autenticado");
        }
    }
}