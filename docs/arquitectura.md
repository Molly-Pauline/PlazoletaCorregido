# Arquitectura

El repositorio contiene el microservicio Plazoleta como aplicación Spring Boot independiente. Usuarios sigue siendo otro servicio y emite credenciales firmadas; Plazoleta verifica esas credenciales localmente con HMAC-SHA256.

## Capas

- `controller`: API REST y conversión de contratos HTTP.
- `dto`: entrada y salida; la validación HTTP vive en los DTO de entrada.
- `service`: reglas de HU2-HU4 y control de pertenencia.
- `domain`: entidades JPA `Restaurante` y `Plato`.
- `repository`: persistencia mediante Spring Data JPA.
- `security`: verificación de token Bearer y creación del principal autenticado.
- `config`: autorización de rutas y configuración de seguridad.
- `exception`: traducción de errores de negocio y validación a respuestas HTTP.

## Persistencia

H2 en memoria es el valor por defecto para desarrollo local y pruebas. PostgreSQL se activa con `SPRING_DATASOURCE_URL` y credenciales externas. `Restaurante` guarda `idPropietario` como referencia al servicio Usuarios; `Plato` tiene una relación muchos-a-uno con `Restaurante`. Las migraciones de producción deben sustituir `ddl-auto=update`.

## Seguridad

Se verifica el formato compartido con Usuarios: `base64url(idUsuario:ROL:vencimientoEpochSegundos).base64url(HMAC-SHA256)`. La firma se compara en tiempo constante, se exige vencimiento vigente y el rol procede únicamente del contenido firmado. `AUTH_SECRET` debe ser idéntico en ambos servicios; si falta, la app genera un secreto efímero y los tokens de Usuarios no serán aceptados.