package com.plazoleta.dto.response;

/** Vista minima de una cuenta para respuestas de altas sin exponer su clave ni otros datos internos. */
public record CuentaResponse(Integer id, String nombre, String apellido, String correo, String rol) {
}
