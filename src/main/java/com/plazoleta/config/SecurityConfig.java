package com.plazoleta.config;

import com.plazoleta.security.AuthenticatedUserJwtConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    UserDetailsService disabledUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("La autenticacion se realiza mediante Bearer token");
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, AuthenticatedUserJwtConverter jwtAuthenticationConverter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/restaurantes").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/platos").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT, "/platos/**").hasRole("PROPIETARIO")
                        .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

                @Bean
                JwtDecoder jwtDecoder(@Value("${app.auth.secret:}") String configuredSecret) {
                if (configuredSecret == null || configuredSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
                    throw new IllegalStateException("AUTH_SECRET debe tener al menos 32 bytes para JWT HS256");
                }
                SecretKeySpec secretKey = new SecretKeySpec(
                    configuredSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
                decoder.setJwtValidator(JwtValidators.createDefault());
                return decoder;
                }
}