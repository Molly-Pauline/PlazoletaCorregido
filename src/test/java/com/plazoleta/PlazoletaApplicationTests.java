
package com.plazoleta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración para verificar que
 * la aplicación Plazoleta inicia correctamente.
 *
 * No depende de AUTH_SECRET ni de
 * UsuarioTokenValidator.
 */
@SpringBootTest
class PlazoletaApplicationTests {

    /**
     * Verifica que Spring Boot pueda cargar
     * el contexto completo de la aplicación.
     */
    @Test
    void contextLoads() {
        // Si Spring Boot inicia sin errores,
        // esta prueba se considera exitosa.
    }
}
