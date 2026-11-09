package com.plazoleta.security;

import com.plazoleta.entity.Usuario;
import com.plazoleta.entity.Rol;
import com.plazoleta.repository.EmpleadoRestauranteRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import javax.crypto.SecretKey;

/** Emite JWT HS256 compatibles con Plazoleta. */
@Component
public class HmacTokenService {

    private final JwtEncoder encoder;
    private final EmpleadoRestauranteRepository empleadoRestauranteRepository;
    private final long lifetimeSeconds;

    /** Obtiene la clave externa y el tiempo de vida sin incluir sus valores en el codigo fuente. */
    public HmacTokenService(SecretKey jwtSigningKey, EmpleadoRestauranteRepository empleadoRestauranteRepository,
                            @Value("${app.auth.token-lifetime-seconds:43200}") long lifetimeSeconds) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
        this.empleadoRestauranteRepository = empleadoRestauranteRepository;
        this.lifetimeSeconds = lifetimeSeconds;
    }

    /** Firma identidad, rol, expiracion y restaurante opcional usando el contrato JWT compartido. */
    public String issue(Usuario usuario) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(usuario.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(lifetimeSeconds))
                .claim("rol", usuario.getRol().name());
        if (usuario.getRol() == Rol.EMPLEADO) {
            Integer restauranteId = empleadoRestauranteRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalStateException("Empleado sin restaurante asociado"))
                    .getIdRestaurante();
            claims.claim("idRestaurante", restauranteId);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    /** Informa la misma duracion que se incorpora a la respuesta de login. */
    public long getLifetimeSeconds() {
        return lifetimeSeconds;
    }

}
