package com.plazoleta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RestauranteRequest(
        @NotBlank @Pattern(regexp = ".*\\D.*", message = "El nombre no puede contener solo numeros") String nombre,
        @NotBlank @Pattern(regexp = "\\d+", message = "El NIT debe contener solo numeros") String nit,
        @NotBlank String direccion,
        @NotBlank @Size(max = 13) @Pattern(regexp = "\\+?\\d{1,13}", message = "Telefono invalido") String telefono,
        @NotBlank @Pattern(regexp = "https?://.+", message = "La URL del logo debe ser HTTP o HTTPS") String urlLogo,
        @NotNull @Positive Long idPropietario) {
}