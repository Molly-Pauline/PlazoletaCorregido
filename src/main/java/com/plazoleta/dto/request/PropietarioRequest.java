package com.plazoleta.dto.request;

import jakarta.validation.constraints.*;
import java.util.Date;

/** HU1: el rol PROPIETARIO se fija en el servidor. */
public record PropietarioRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Pattern(regexp = "\\d+") String documentoDeIdentidad,
        @NotBlank @Size(max = 13) @Pattern(regexp = "\\+?\\d+") String celular,
        @NotNull Date fechaDeNacimiento,
        @NotBlank @Email String correo,
        @NotBlank String clave
) {}
