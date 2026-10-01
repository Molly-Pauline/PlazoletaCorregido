package com.plazoleta.dto.request;

import jakarta.validation.constraints.NotNull;

/** Solicita explícitamente el nuevo estado de disponibilidad, sin aceptar un valor nulo. */
public record PlatoEstadoRequest(@NotNull Boolean activo) {
}