# Arquitectura

El repositorio contiene el microservicio Plazoleta como aplicación Spring Boot independiente. Usuarios sigue siendo otro servicio y emite JWT; Plazoleta los verifica localmente con HS256.

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

Se valida un JWT estándar firmado con HS256. El contrato de claims es `sub` (ID de usuario numérico), `rol` (`ADMINISTRADOR`, `PROPIETARIO`, `EMPLEADO` o `CLIENTE`), `exp` (Unix epoch seconds) y opcionalmente `idRestaurante` para empleados. La biblioteca valida firma y vencimiento; el principal y autoridad se construyen únicamente desde claims verificadas. `AUTH_SECRET` debe ser idéntico en ambos servicios y contener al menos 32 bytes UTF-8; sin una clave configurada la app no inicia.