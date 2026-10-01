package com.plazoleta.service;

import com.plazoleta.domain.Restaurante;
import com.plazoleta.dto.RestauranteRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestauranteServiceTest {

    private final RestauranteRepository repository = mock(RestauranteRepository.class);
    private final UsuarioValidationPort usuarioValidation = mock(UsuarioValidationPort.class);
    private final RestauranteService service = new RestauranteService(repository, usuarioValidation);
    private final RestauranteRequest request = new RestauranteRequest(
            "La Casona", "123456789", "Calle 123", "+573001234567", "https://example.com/logo.png", 10L);

    @Test
    void createsRestaurantOnlyAfterValidatingOwner() {
        when(repository.existsByNit(request.nit())).thenReturn(false);
        when(usuarioValidation.existePropietarioValido(10L)).thenReturn(true);
        when(repository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurante created = service.crear(request);

        assertEquals("La Casona", created.getNombre());
        assertEquals(10L, created.getIdPropietario());
        verify(usuarioValidation).existePropietarioValido(10L);
    }

    @Test
    void rejectsDuplicateNitBeforeCallingUsersService() {
        when(repository.existsByNit(request.nit())).thenReturn(true);

        assertThrows(DomainException.class, () -> service.crear(request));

        verify(usuarioValidation, never()).existePropietarioValido(any());
    }

    @Test
    void rejectsUnknownOrNonOwnerUser() {
        when(repository.existsByNit(request.nit())).thenReturn(false);
        when(usuarioValidation.existePropietarioValido(10L)).thenReturn(false);

        assertThrows(DomainException.class, () -> service.crear(request));

        verify(repository, never()).save(any());
    }
}