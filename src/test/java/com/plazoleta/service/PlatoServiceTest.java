package com.plazoleta.service;

import com.plazoleta.domain.Plato;
import com.plazoleta.domain.Restaurante;
import com.plazoleta.dto.PlatoRequest;
import com.plazoleta.dto.PlatoUpdateRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatoServiceTest {

    private final PlatoRepository platoRepository = mock(PlatoRepository.class);
    private final RestauranteRepository restauranteRepository = mock(RestauranteRepository.class);
    private final PlatoService service = new PlatoService(platoRepository, restauranteRepository);
    private final Restaurante restaurant = new Restaurante(
            "La Casona", "123456789", "Calle 123", "+573001234567", "https://example.com/logo.png", 10L);
    private final AuthenticatedUser owner = new AuthenticatedUser(10L, "PROPIETARIO");

    @Test
    void createsActiveDishForOwningRestaurant() {
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(platoRepository.save(any(Plato.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PlatoRequest request = new PlatoRequest("Hamburguesa", 25000, "Con queso",
                "https://example.com/burger.png", "Hamburguesas", 1L);

        Plato created = service.crear(request, owner);

        assertTrue(created.isActivo());
        assertEquals(restaurant, created.getRestaurante());
    }

    @Test
    void refusesDishForAnotherOwnersRestaurant() {
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        PlatoRequest request = new PlatoRequest("Hamburguesa", 25000, "Con queso",
                "https://example.com/burger.png", "Hamburguesas", 1L);

        assertThrows(DomainException.class, () -> service.crear(request, new AuthenticatedUser(11L, "PROPIETARIO")));

        verify(platoRepository, never()).save(any());
    }

    @Test
    void updateChangesOnlyPriceAndDescription() {
        Plato dish = new Plato("Hamburguesa", 25000, "Con queso", "https://example.com/burger.png",
                "Hamburguesas", restaurant);
        when(platoRepository.findById(4L)).thenReturn(Optional.of(dish));
        when(platoRepository.save(dish)).thenReturn(dish);

        Plato updated = service.modificar(4L, new PlatoUpdateRequest(30000, "Con queso y tocineta"), owner);

        assertEquals(30000, updated.getPrecio());
        assertEquals("Con queso y tocineta", updated.getDescripcion());
        assertEquals("Hamburguesa", updated.getNombre());
        assertEquals("Hamburguesas", updated.getCategoria());
        assertEquals("https://example.com/burger.png", updated.getUrlImagen());
    }
}