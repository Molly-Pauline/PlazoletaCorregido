package com.plazoleta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record PlatoRequest(
        @NotBlank String nombre,
        @NotNull @Positive Integer precio,
        @NotBlank String descripcion,
        @NotBlank @Pattern(regexp = "https?://.+", message = "La URL de imagen debe ser HTTP o HTTPS") String urlImagen,
        @NotBlank String categoria,
        @NotNull @Positive Long idRestaurante) {
}