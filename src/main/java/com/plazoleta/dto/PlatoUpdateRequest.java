package com.plazoleta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlatoUpdateRequest(@NotNull @Positive Integer precio, @NotBlank String descripcion) {
}