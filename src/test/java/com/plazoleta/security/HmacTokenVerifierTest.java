package com.plazoleta.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HmacTokenVerifierTest {

    private static final String SECRET = "local-test-secret-with-32-chars";
    private final HmacTokenVerifier verifier = new HmacTokenVerifier(SECRET);

    @Test
    void acceptsValidTokenAndReturnsSignedIdentity() {
        AuthenticatedUser user = verifier.verify(token("42:PROPIETARIO:" + (Instant.now().getEpochSecond() + 60)));

        assertEquals(new AuthenticatedUser(42L, "PROPIETARIO"), user);
    }

    @Test
    void rejectsTokenWithModifiedSignature() {
        String validToken = token("42:PROPIETARIO:" + (Instant.now().getEpochSecond() + 60));

        assertThrows(InvalidBearerTokenException.class,
                () -> verifier.verify(validToken.substring(0, validToken.length() - 1) + "A"));
    }

    @Test
    void rejectsExpiredToken() {
        assertThrows(InvalidBearerTokenException.class,
                () -> verifier.verify(token("42:PROPIETARIO:" + (Instant.now().getEpochSecond() - 1))));
    }

    private String token(String body) {
        String encodedBody = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(body.getBytes(StandardCharsets.UTF_8));
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(encodedBody.getBytes(StandardCharsets.UTF_8)));
            return encodedBody + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}