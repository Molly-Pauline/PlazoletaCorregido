
package com.plazoleta.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdministradorRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank
        @Pattern(regexp = "\\d+")
        String documentoDeIdentidad,
        @NotBlank
        @Size(max = 13)
        @Pattern(regexp = "\\+?\\d+")
        String celular,
        @NotBlank @Email String correo,
        @NotBlank String clave
) {}
