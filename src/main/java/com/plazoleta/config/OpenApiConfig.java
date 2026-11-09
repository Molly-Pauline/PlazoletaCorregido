package com.plazoleta.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Datos generales de la documentación OpenAPI y esquema de seguridad JWT.
 * La documentación se ve en /swagger-ui.html y la especificación en /v3/api-docs.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Plazoleta de comidas API",
        version = "1.0.0",
        description = "Gestión de usuarios, restaurantes, platos y pedidos (HU1 a HU17). "
                + "Para las rutas protegidas, inicia sesión en POST /api/v1/usuarios/login, "
                + "copia el token y pégalo en el botón Authorize."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
