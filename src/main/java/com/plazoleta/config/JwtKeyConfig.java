package com.plazoleta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/** Clave temporal generada al arrancar. Los tokens anteriores dejan de servir tras reiniciar. */
@Configuration
public class JwtKeyConfig {
    @Bean
    public SecretKey jwtSigningKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
