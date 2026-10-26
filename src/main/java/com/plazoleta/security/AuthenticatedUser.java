package com.plazoleta.security;

public record AuthenticatedUser(Long idUsuario, String rol, Long idRestaurante) {

	/** Mantiene compatibilidad para usuarios que no están asociados a un restaurante. */
	public AuthenticatedUser(Long idUsuario, String rol) {
		this(idUsuario, rol, null);
	}
}