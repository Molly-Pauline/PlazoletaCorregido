# Plazoleta unificada (puerto 8080)

Se integraron Usuarios y Plazoleta bajo `com.plazoleta` y se sustituyeron las llamadas HTTP internas por servicios locales. **Esta entrega no se ha compilado ni probado contra tu MySQL**; valida todo antes de usarla.

## Requisitos
- Java 17 o superior y Maven
- MySQL y respaldo previo de las bases de datos originales
- Una base `plazoleta_bd` con las tablas/datos necesarios; si Usuarios tenía otra base, **migra sus datos primero**. No ejecutes `ddl-auto=update` sin respaldo.

## Variables de entorno (PowerShell)
```powershell
$env:SPRING_DATASOURCE_PASSWORD = '<clave mysql>'
# Solo para crear el primer administrador por POST (por defecto está desactivado):
$env:ADMIN_REGISTRATION_ENABLED = 'true'
```
No guardes secretos en Git ni copies literalmente los valores entre `<...>`.

## Inicio
```powershell
mvn clean test
mvn spring-boot:run
```
Verifica `GET http://localhost:8080/actuator/health`.

## Primer administrador
`POST http://localhost:8080/api/v1/usuarios/administradores` con Authorization: No Auth, Content-Type: application/json y los campos exigidos por `RegistroRequest` (rol ADMINISTRADOR). Solo funciona cuando `ADMIN_REGISTRATION_ENABLED=true` y aún no existe administrador. Tras crearlo, la ruta responde `403` aunque la variable siga activa; aun así, no la configures en los siguientes arranques.

Alternativa sin endpoint: configura `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` (mínimo 12 caracteres) antes de iniciar; el `AdminBootstrap` existente crea el primer administrador si no existe.

## Consideraciones
- El proyecto Usuarios original se conserva por separado en tu ZIP fuente; no lo ejecutes en 8081.
- La ruta interna de validación entre servicios se eliminó.
- Revisa la seguridad, DTO, las migraciones SQL y pruebas funcionales antes de desplegar.


## Cambio de IDs a Integer
Los identificadores de las entidades y DTO se unificaron como Integer. La suma de precios y la duracion de tokens conservan operaciones long por seguridad numerica. El esquema SQL para bases nuevas usa INT. Si ya existen tablas BIGINT, realizar respaldo y migracion controlada antes de modificar columnas: el script CREATE TABLE IF NOT EXISTS no migra datos ni altera tablas existentes.
