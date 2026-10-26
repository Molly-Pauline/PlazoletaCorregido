package com.plazoleta.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Convierte claims de JWT verificadas al principal usado por la lógica de negocio. */
@Component
public class AuthenticatedUserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "PROPIETARIO", "EMPLEADO", "CLIENTE");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            long userId = Long.parseLong(jwt.getSubject());
            String role = jwt.getClaimAsString("rol");
            Object restaurantClaim = jwt.getClaim("idRestaurante");
            Long restaurantId = restaurantClaim == null ? null : Long.valueOf(restaurantClaim.toString());
            if (userId <= 0 || role == null || !ROLES.contains(role)
                    || (restaurantId != null && restaurantId <= 0)) {
                throw new InvalidBearerTokenException("Claims de identidad invalidas");
            }
            AuthenticatedUser principal = new AuthenticatedUser(userId, role, restaurantId);
            return UsernamePasswordAuthenticationToken.authenticated(principal, jwt,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        } catch (IllegalArgumentException exception) {
            throw new InvalidBearerTokenException("Claims de identidad invalidas", exception);
        }
    }
}