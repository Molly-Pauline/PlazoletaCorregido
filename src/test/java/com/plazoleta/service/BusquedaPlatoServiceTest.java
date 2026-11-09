package com.plazoleta.service;

import com.plazoleta.dto.response.PlatoResponse;
import com.plazoleta.entity.Plato;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.service.impl.PlatoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas de la búsqueda de platos por nombre. */
class BusquedaPlatoServiceTest {

    private final PlatoRepository platoRepository = mock(PlatoRepository.class);
    private final RestauranteRepository restauranteRepository = mock(RestauranteRepository.class);
    private final PlatoServiceImpl service = new PlatoServiceImpl(platoRepository, restauranteRepository);
    private final Restaurante restaurante = new Restaurante(
            "La Casona", "123456789", "Calle 123", "+573001234567", "https://example.com/logo.png", 10);

    @BeforeEach
    void prepararMenu() {
        when(restauranteRepository.existsById(1)).thenReturn(true);
        when(platoRepository.findByRestaurante_IdAndActivoTrue(1)).thenReturn(List.of(
                plato("Pizza"), plato("Hamburguesa doble"), plato("Ájiaco"), plato("hamburguesa clásica"), plato("Sopa")));
    }

    @Test
    void encuentraPorInicioDelNombreSinImportarMayusculas() {
        assertEquals(List.of("hamburguesa clásica", "Hamburguesa doble"), nombres(service.buscarEnMenu(1, "HAMB")));
    }

    @Test
    void ignoraLasTildes() {
        assertEquals(List.of("Ájiaco"), nombres(service.buscarEnMenu(1, "aji")));
    }

    @Test
    void devuelveVacioSiNoHayCoincidencias() {
        assertTrue(service.buscarEnMenu(1, "zzz").isEmpty());
    }

    @Test
    void exigeUnTextoDeBusqueda() {
        DomainException error = assertThrows(DomainException.class, () -> service.buscarEnMenu(1, " "));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void informaSiElRestauranteNoExiste() {
        DomainException error = assertThrows(DomainException.class, () -> service.buscarEnMenu(2, "pizza"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    private Plato plato(String nombre) {
        return new Plato(nombre, 10000, "Descripcion", "https://example.com/plato.png", "General", restaurante);
    }

    private List<String> nombres(List<PlatoResponse> platos) {
        return platos.stream().map(PlatoResponse::nombre).toList();
    }
}
