package com.plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Limita la actualización HU4 a precio y descripción, protegiendo los demás atributos del plato. */
public record PlatoUpdateRequest(
	// Nuevo precio unitario; debe ser mayor que cero.
	@NotNull @Positive Integer precio,
	// Texto que reemplaza la descripción actual del plato.
	@NotBlank String descripcion) {
}