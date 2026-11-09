package com.plazoleta.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos requeridos para crear un empleado, incluido el rol solicitado y el restaurante asociado. */
public record EmpleadoRequest(
        // Los campos personales obligatorios se validan antes de entrar a la capa de negocio.
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Pattern(regexp = "\\d+") String documentoDeIdentidad,
        @NotBlank @Size(max = 13) @Pattern(regexp = "\\+?\\d+") String celular,
        @NotBlank @Email String correo,
        // BCrypt se aplica en el servicio; aqui se exige una clave con longitud minima.
        @NotBlank @Size(min = 8) String clave,
        // Ambos identificadores deben existir como valores positivos; el servicio comprueba el rol permitido.
        @NotNull @Positive Integer idRol,
        @NotNull @Positive Integer idRestaurante) {
}
