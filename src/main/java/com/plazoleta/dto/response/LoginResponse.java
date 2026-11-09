package com.plazoleta.dto.response;

/** Resultado de login: token portador, identidad, rol y duracion configurada en segundos. */
public record LoginResponse(String token, Integer idUsuario, String rol, long expiraEnSegundos) {
}
