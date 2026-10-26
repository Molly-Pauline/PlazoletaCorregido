package com.plazoleta.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas del contrato JWT HS256, claims de identidad y rechazo de tokens alterados o vencidos. */
class JwtTokenAuthenticationTest {

    private static final String SECRET = "local-test-secret-with-32-byte-key";
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    private final AuthenticatedUserJwtConverter converter = new AuthenticatedUserJwtConverter();

    /** Confirma que un JWT firmado genera el usuario de dominio y la autoridad ROLE_ esperada. */
    @Test
    void acceptsValidTokenAndReturnsSignedIdentity() {
        Jwt jwt = decoder.decode(token(42, "PROPIETARIO", null, Instant.now().getEpochSecond() + 60));
        AbstractAuthenticationToken authentication = converter.convert(jwt);
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        assertEquals(new AuthenticatedUser(42L, "PROPIETARIO"), user);
        assertEquals("ROLE_PROPIETARIO", authentication.getAuthorities().iterator().next().getAuthority());
    }

    /** Verifica que la claim opcional del empleado quede disponible para restringir HU12. */
    @Test
    void acceptsEmployeeTokenWithSignedRestaurantId() {
        Jwt jwt = decoder.decode(token(42, "EMPLEADO", 9L, Instant.now().getEpochSecond() + 60));
        AuthenticatedUser user = (AuthenticatedUser) converter.convert(jwt).getPrincipal();
        assertEquals(new AuthenticatedUser(42L, "EMPLEADO", 9L), user);
    }

    /** Una modificación de la firma debe invalidar el token aunque sus claims sigan legibles. */
    @Test
    void rejectsTokenWithModifiedSignature() {
        String validToken = token(42, "PROPIETARIO", null, Instant.now().getEpochSecond() + 60);
        String tamperedToken = validToken.substring(0, validToken.length() - 1)
                + (validToken.endsWith("A") ? "B" : "A");
        assertThrows(JwtException.class, () -> decoder.decode(tamperedToken));
    }

    /** Un token vencido fuera de la tolerancia de reloj estándar debe rechazarse. */
    @Test
    void rejectsExpiredToken() {
        String expired = token(42, "PROPIETARIO", null, Instant.now().getEpochSecond() - 120);
        assertThrows(JwtException.class, () -> decoder.decode(expired));
    }

    /** Construye un JWT HS256 de prueba con el mismo contrato de claims que debe emitir Usuarios. */
    private String token(long userId, String role, Long restaurantId, long expiresAt) {
        // El encabezado fija explícitamente el algoritmo admitido por el decoder.
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        // Solo se incluye la asociación cuando el sujeto tiene un restaurante vinculado.
        String restaurantClaim = restaurantId == null ? "" : ",\"idRestaurante\":" + restaurantId;
        // sub, rol y exp son las claims compartidas para identificar, autorizar y expirar la sesión.
        String payload = encode("{\"sub\":\"" + userId + "\",\"rol\":\"" + role
                + "\",\"exp\":" + expiresAt + restaurantClaim + "}");
        // JWT firma exactamente la concatenación codificada de encabezado y payload.
        String signingInput = header + "." + payload;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return signingInput + "." + Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    /** Codifica cada segmento JWT como Base64 URL sin padding. */
    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
