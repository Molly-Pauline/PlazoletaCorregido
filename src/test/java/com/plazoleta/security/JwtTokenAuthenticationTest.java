package com.plazoleta.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Prueba el contrato JWT HS256 que comparten el emisor Usuarios y este servicio. */
class JwtTokenAuthenticationTest {

    private static final String SECRET = "local-test-secret-with-32-byte-key";
    private final JwtDecoder decoder = createDecoder();
    private final AuthenticatedUserJwtConverter converter = new AuthenticatedUserJwtConverter();

    /** Un token válido produce el principal autenticado y la autoridad de rol que espera Spring Security. */
    @Test
    void acceptsValidTokenAndReturnsSignedIdentity() {
        Jwt jwt = decoder.decode(token(42, "PROPIETARIO", null, Instant.now().getEpochSecond() + 60));
        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertEquals(new AuthenticatedUser(42L, "PROPIETARIO"), authentication.getPrincipal());
        assertEquals("ROLE_PROPIETARIO", authentication.getAuthorities().iterator().next().getAuthority());
    }

    /** La asociación de restaurante de un empleado se toma únicamente de la claim firmada. */
    @Test
    void acceptsEmployeeTokenWithSignedRestaurantId() {
        Jwt jwt = decoder.decode(token(42, "EMPLEADO", 9L, Instant.now().getEpochSecond() + 60));

        assertEquals(new AuthenticatedUser(42L, "EMPLEADO", 9L), converter.convert(jwt).getPrincipal());
    }

    /** Un cambio en la firma invalida el token, aunque el encabezado y el payload sean legibles. */
    @Test
    void rejectsTokenWithModifiedSignature() {
        String validToken = token(42, "PROPIETARIO", null, Instant.now().getEpochSecond() + 60);
        int signatureStart = validToken.lastIndexOf('.') + 1;
        int characterToChange = signatureStart + 10;
        char original = validToken.charAt(characterToChange);
        char replacement = original == 'A' ? 'B' : 'A';
        String tamperedToken = validToken.substring(0, characterToChange) + replacement
            + validToken.substring(characterToChange + 1);

        assertThrows(JwtException.class, () -> decoder.decode(tamperedToken));
    }

    /** El validador temporal estándar rechaza tokens expirados, tolerando solo pequeñas diferencias de reloj. */
    @Test
    void rejectsExpiredToken() {
        String expired = token(42, "PROPIETARIO", null, Instant.now().getEpochSecond() - 120);

        assertThrows(JwtException.class, () -> decoder.decode(expired));
    }

    private JwtDecoder createDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefault());
        return jwtDecoder;
    }

    /** Emula el emisor externo para crear tokens con las claims acordadas en el contrato. */
    private String token(long userId, String role, Long restaurantId, long expiresAt) {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String restaurantClaim = restaurantId == null ? "" : ",\"idRestaurante\":" + restaurantId;
        String payload = encode("{\"sub\":\"" + userId + "\",\"rol\":\"" + role
                + "\",\"exp\":" + expiresAt + restaurantClaim + "}");
        String signingInput = header + "." + payload;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)));
            return signingInput + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}