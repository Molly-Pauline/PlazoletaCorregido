package com.plazoleta.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Component
public class HmacTokenVerifier {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "PROPIETARIO", "EMPLEADO", "CLIENTE");
    private final byte[] secret;

    public HmacTokenVerifier(@Value("${app.auth.secret:}") String configuredSecret) {
        String effectiveSecret = configuredSecret;
        if (effectiveSecret == null || effectiveSecret.length() < 16) {
            byte[] randomSecret = new byte[32];
            new SecureRandom().nextBytes(randomSecret);
            effectiveSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomSecret);
        }
        this.secret = effectiveSecret.getBytes(StandardCharsets.UTF_8);
    }

    public AuthenticatedUser verify(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidBearerTokenException();
        }

        int separator = token.lastIndexOf('.');
        if (separator <= 0 || separator == token.length() - 1) {
            throw new InvalidBearerTokenException();
        }

        String encodedBody = token.substring(0, separator);
        try {
            byte[] suppliedSignature = DECODER.decode(token.substring(separator + 1));
            byte[] body = DECODER.decode(encodedBody);
            if (!MessageDigest.isEqual(suppliedSignature, sign(encodedBody))) {
                throw new InvalidBearerTokenException();
            }

            String[] fields = new String(body, StandardCharsets.UTF_8).split(":", -1);
            if (fields.length != 3) {
                throw new InvalidBearerTokenException();
            }
            long userId = Long.parseLong(fields[0]);
            long expiresAt = Long.parseLong(fields[2]);
            if (userId <= 0 || Instant.now().getEpochSecond() >= expiresAt || !ROLES.contains(fields[1])) {
                throw new InvalidBearerTokenException();
            }
            return new AuthenticatedUser(userId, fields[1]);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBearerTokenException();
        }
    }

    private byte[] sign(String encodedBody) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return mac.doFinal(encodedBody.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo verificar la credencial", exception);
        }
    }
}