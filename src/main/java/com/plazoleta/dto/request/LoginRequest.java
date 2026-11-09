package com.plazoleta.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenciales recibidas por login; no se normaliza ni se revela la clave en la respuesta. */
public record LoginRequest(@NotBlank @Email String correo, @NotBlank String clave) {
}
