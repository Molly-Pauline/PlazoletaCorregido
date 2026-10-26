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

/** Convierte únicamente claims JWT firmadas y validadas al principal que consume la lógica de negocio. */
@Component
public class AuthenticatedUserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /** Roles válidos del sistema; una claim firmada con cualquier otro rol se rechaza. */
    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "PROPIETARIO", "EMPLEADO", "CLIENTE");

    /** Convierte las claims ya verificadas en el principal y autoridad usados por controladores y servicios. */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            // El sujeto estándar de JWT contiene el ID numérico del usuario.
            long userId = Long.parseLong(jwt.getSubject());
            // El rol es una claim propia del contrato compartido entre Usuarios y Plazoleta.
            String role = jwt.getClaimAsString("rol");
            // Solo empleados suelen traer restaurante; el valor firmado limita sus consultas de HU12.
            Object restaurantClaim = jwt.getClaim("idRestaurante");
            Long restaurantId = restaurantClaim == null ? null : Long.valueOf(restaurantClaim.toString());
            // La firma prueba el emisor, pero también se validan dominio e identificadores antes de autorizar.
            if (userId <= 0 || role == null || !ROLES.contains(role)
                    || (restaurantId != null && restaurantId <= 0)) {
                throw new InvalidBearerTokenException("Claims de identidad invalidas");
            }

            // El principal conserva los datos tipados que inyectan los controladores en la lógica de negocio.
            AuthenticatedUser principal = new AuthenticatedUser(userId, role, restaurantId);
            // Spring exige el prefijo ROLE_ para que las reglas hasRole de SecurityConfig coincidan.
            return UsernamePasswordAuthenticationToken.authenticated(principal, jwt,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        } catch (IllegalArgumentException exception) {
            // ID ausente o claims numéricas mal formadas se exponen uniformemente como Bearer inválido.
            throw new InvalidBearerTokenException("Claims de identidad invalidas", exception);
        }
    }
}