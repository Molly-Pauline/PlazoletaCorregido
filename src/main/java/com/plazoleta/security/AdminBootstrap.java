package com.plazoleta.security;

import com.plazoleta.entity.Rol;
import com.plazoleta.entity.Usuario;
import com.plazoleta.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea el administrador inicial desde configuracion externa, sin incluir credenciales en el codigo. */
@Configuration
public class AdminBootstrap {

    /** Se ejecuta al iniciar Spring y no reemplaza una cuenta administrativa ya existente. */
    @Bean
    ApplicationRunner createInitialAdministrator(UsuarioRepository repository, PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-email:}") String email, @Value("${app.bootstrap.admin-password:}") String password) {
        return args -> {
            // Permite desactivar el alta inicial dejando ambos valores sin configurar.
            if (email.isBlank() && password.isBlank()) return;
            // Exige configuracion conjunta y longitud minima antes de guardar una credencial privilegiada.
            if (email.isBlank() || password.length() < 12) {
                throw new IllegalStateException("Configure ADMIN_EMAIL y ADMIN_PASSWORD de al menos 12 caracteres juntos");
            }
            // Una cuenta administrativa existente cierra el aprovisionamiento y evita crear administradores adicionales.
            if (!repository.existsByRol(Rol.ADMINISTRADOR) && !repository.existsByCorreo(email)) {
                repository.save(Usuario.builder()
                        .nombre("Administrador")
                        .apellido("Plataforma")
                        .correo(email)
                        .clave(passwordEncoder.encode(password))
                        .rol(Rol.ADMINISTRADOR)
                        .adminSlot("ADMINISTRADOR_UNICO")
                        .build());
            }
        };
    }
}
