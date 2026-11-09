package com.plazoleta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Valida una vez por solicitud el token Bearer y establece el principal reconocido por Spring Security. */
@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    /** Lista cerrada de roles que el filtro admite en un token correctamente firmado. */
    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "PROPIETARIO", "EMPLEADO", "CLIENTE");
    private final JwtDecoder decoder;
    /** Reutiliza exactamente la clave configurada para el emisor de tokens compartidos. */
    public BearerTokenFilter(SecretKey jwtSigningKey) {
        this.decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/api/v1/usuarios/administradores");
    }

    /** Deja pasar solicitudes publicas sin token y rechaza tokens presentes que sean invalidos. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // Las rutas publicas siguen su curso; la politica HTTP decide luego si requieren autenticacion.
            chain.doFilter(request, response);
            return;
        }
        AuthenticatedUser user;
        try {
            user = verify(authorization.substring(7));
        } catch (RuntimeException exception) {
            // Nunca conserva una autenticacion previa si el token recibido no supera la verificacion.
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"success\":false,\"message\":\"Token invalido\"}");
            return;
        }
        // La autoridad ROLE_ permite que hasRole(...) aplique las reglas declaradas en SecurityConfig.
        var authentication = new UsernamePasswordAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.rol())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    /** Verifica estructura, firma, expiracion y dominio de claims antes de crear el principal. */
    private AuthenticatedUser verify(String token) {
        Jwt jwt = decoder.decode(token);
        int id = Integer.parseInt(jwt.getSubject());
        String role = jwt.getClaimAsString("rol");
        if (id <= 0 || jwt.getExpiresAt() == null || role == null || !ROLES.contains(role)) {
            throw new IllegalArgumentException();
        }
        Object restaurantClaim = jwt.getClaim("idRestaurante");
        Integer restaurantId = restaurantClaim == null ? null : Integer.valueOf(restaurantClaim.toString());
        if (restaurantId != null && restaurantId <= 0) throw new IllegalArgumentException();
        return new AuthenticatedUser(id, role, restaurantId);
    }
}
