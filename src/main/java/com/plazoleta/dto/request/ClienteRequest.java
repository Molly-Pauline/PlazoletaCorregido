package com.plazoleta.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada de alta de cliente: las restricciones se ejecutan en el limite HTTP antes de persistir. */
public record ClienteRequest(
        // Los nombres no pueden estar vacios; el documento solo admite digitos.
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Pattern(regexp = "\\d+") String documentoDeIdentidad,
        // Se permite un signo + inicial y se limita la longitud total del telefono a 13 caracteres.
        @NotBlank @Size(max = 13) @Pattern(regexp = "\\+?\\d+") String celular,
        // El correo debe tener formato valido y la clave debe tener al menos ocho caracteres.
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8) String clave) {
}
