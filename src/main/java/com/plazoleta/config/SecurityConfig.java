package com.plazoleta.config;

import com.plazoleta.security.BearerTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean UserDetailsService disabledUserDetailsService() {
        return username -> { throw new UsernameNotFoundException("Solo autenticacion Bearer"); };
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter filter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req,res,ex) -> {
                    res.setStatus(401);
                    res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    res.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Debes iniciar sesión para acceder a este recurso\"}");
                })
                .accessDeniedHandler((req,res,ex) -> {
                    res.setStatus(403);
                    res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    res.getWriter().write("{\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Tu rol no tiene permisos para esta operación\"}");
                }))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login", "/api/v1/usuarios/clientes", "/api/v1/usuarios/administradores").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro", "/api/v1/usuarios/propietarios").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/empleados").hasRole("PROPIETARIO")
                .requestMatchers(HttpMethod.GET, "/restaurantes", "/restaurantes/*/platos").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.POST, "/restaurantes").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/platos").hasRole("PROPIETARIO")
                .requestMatchers(HttpMethod.PUT, "/platos/**").hasRole("PROPIETARIO")
                .requestMatchers(HttpMethod.PATCH, "/platos/*/estado").hasRole("PROPIETARIO")
                .requestMatchers(HttpMethod.POST, "/pedidos").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/pedidos").hasRole("EMPLEADO")
                .requestMatchers(HttpMethod.PATCH, "/pedidos/*/asignar", "/pedidos/*/listo", "/pedidos/*/entregar").hasRole("EMPLEADO")
                .requestMatchers(HttpMethod.PATCH, "/pedidos/*/cancelar").hasRole("CLIENTE")
                .anyRequest().authenticated())
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
