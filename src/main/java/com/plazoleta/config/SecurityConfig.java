package com.plazoleta.config;

import com.plazoleta.security.AuthenticatedUserJwtConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Define autenticación sin estado y la matriz de roles permitidos para cada ruta HTTP. */
@Configuration
public class SecurityConfig {

    /** Desactiva la autenticación de usuario/contraseña: la aplicación solo confía en Bearer verificado. */
    @Bean
    UserDetailsService disabledUserDetailsService() {
        return username -> {
            // Impide que la configuración automática habilite usuarios locales y autenticación por contraseña.
            throw new UsernameNotFoundException("La autenticacion se realiza mediante Bearer token");
        };
    }

    /** Construye la cadena de seguridad que valida credenciales y autoriza cada operación de negocio. */
    @Bean
        SecurityFilterChain securityFilterChain(
            HttpSecurity http, AuthenticatedUserJwtConverter jwtAuthenticationConverter) throws Exception {
        return http
            // La API no usa formularios ni necesita protección CSRF basada en cookies de sesión.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // API stateless: el cliente debe presentar la identidad en cada solicitud, sin sesión de servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Si una ruta protegida llega sin principal, se responde 401 en lugar de iniciar un login web.
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) ->
                    response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        // El endpoint de salud es el único acceso público de esta lista.
                        .requestMatchers("/actuator/health").permitAll()
                        // Usuarios solo puede validar una relación de propiedad con identidad de propietario.
                        .requestMatchers(HttpMethod.GET, "/restaurantes/*/propietario/*").hasRole("PROPIETARIO")
                        // El catálogo público para clientes autenticados incluye listado y menú del restaurante.
                        .requestMatchers(HttpMethod.GET, "/restaurantes", "/restaurantes/*/platos").hasRole("CLIENTE")
                        // HU11 reserva la creación de pedidos a clientes.
                        .requestMatchers(HttpMethod.POST, "/pedidos").hasRole("CLIENTE")
                        // HU12 reserva la consulta de pedidos a empleados; el servicio limita además por restaurante firmado.
                        .requestMatchers(HttpMethod.GET, "/pedidos").hasRole("EMPLEADO")
                        // HU2 permite que solo administración registre restaurantes.
                        .requestMatchers(HttpMethod.POST, "/restaurantes").hasRole("ADMINISTRADOR")
                        // HU3, HU4 y HU7 exigen rol de propietario; el servicio confirma la propiedad del recurso.
                        .requestMatchers(HttpMethod.POST, "/platos").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.PATCH, "/platos/*/estado").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT, "/platos/**").hasRole("PROPIETARIO")
                        // Todo endpoint no listado sigue requiriendo autenticación, aunque no tenga regla específica.
                        .anyRequest().authenticated())
                    // Valida JWT HS256 sin sesión y convierte las claims firmadas al principal de la aplicación.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.auth.secret:}") String configuredSecret) {
        // HMAC-SHA256 requiere una clave de al menos 256 bits; la app falla al iniciar si la configuración es débil.
        if (configuredSecret == null || configuredSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("AUTH_SECRET debe tener al menos 32 bytes para JWT HS256");
        }
        // Los mismos bytes UTF-8 deben estar configurados en Usuarios, que emite los tokens compartidos.
        SecretKeySpec secretKey = new SecretKeySpec(
                configuredSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        // Se fija HS256 para impedir que el encabezado del token elija otro algoritmo.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // Aplica validadores estándar, incluidos los límites temporales exp y nbf.
        decoder.setJwtValidator(JwtValidators.createDefault());
        return decoder;
    }
}