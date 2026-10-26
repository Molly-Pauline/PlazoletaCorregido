package com.plazoleta.dto.request;

import jakarta.validation.constraints.NotNull;

/** Solicita explícitamente el nuevo estado de disponibilidad, sin aceptar un valor nulo. */
public record PlatoEstadoRequest(
	// true incluye el plato en menús y pedidos nuevos; false lo conserva pero lo deshabilita.
	@NotNull Boolean activo) {
}