# Plazoleta de comidas

Microservicio REST para restaurantes y platos, migrado a Spring Boot 3.5.5 con Java 17. Este repositorio corresponde al Repo B; el microservicio de Usuarios continúa en su repositorio separado.

## Requisitos

- JDK 17
- Maven 3.9 o Maven Wrapper

## Ejecutar

```powershell
mvn test
mvn spring-boot:run
```

Sin configuración adicional, la aplicación usa H2 en memoria y escucha en `http://localhost:8080`. Para PostgreSQL, configura las variables de `.env.example` en el entorno del proceso. Spring no carga `.env` automáticamente; el archivo es solo una plantilla y `.env` está excluido de Git.

## API

Todas las rutas de negocio reciben `Authorization: Bearer <token>` como JWT HS256 emitido por Usuarios. Plazoleta valida firma y expiración y obtiene `sub`, `rol` y opcionalmente `idRestaurante` solo desde claims firmadas; no confía en `X-Rol` ni `X-User-Id`.

| Método | Ruta | Permiso | Historia |
| --- | --- | --- | --- |
| `POST` | `/restaurantes` | `ADMINISTRADOR` | HU2 |
| `POST` | `/platos` | `PROPIETARIO` del restaurante | HU3 |
| `PUT` | `/platos/{idPlato}` | `PROPIETARIO` del restaurante | HU4 |
| `GET` | `/actuator/health` | Público | Salud |

HU4 solo admite `precio` y `descripcion`; los demás datos del plato se conservan. HU2 comprueba que el usuario asociado exista y tenga rol `PROPIETARIO` mediante el servicio Usuarios. Ese endpoint aún no existe en el repositorio de Usuarios; el contrato requerido está en [`docs/contrato-usuarios.md`](docs/contrato-usuarios.md). Hasta que se implemente, no se aceptarán propietarios sin verificar.

## Configuración

- `AUTH_SECRET`: secreto compartido con Usuarios para JWT HS256; usa el mismo valor de al menos 32 bytes UTF-8 en ambos servicios.
- `USUARIOS_BASE_URL`: URL base del servicio Usuarios (por defecto `http://localhost:8081`).
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: conexión opcional a PostgreSQL; el valor por defecto es H2 en memoria.
- `DDL_AUTO`: estrategia Hibernate, por defecto `update` para desarrollo.

No confirmes secretos ni archivos `.env`. En despliegue usa un gestor de secretos y `DDL_AUTO=validate` junto con migraciones controladas.

## Ramas locales

El reporte del Sprint 1 requiere `main`, `develop`, `release/sprint-1` y las ramas `feature/HU-01-crear-propietario`, `feature/HU-02-crear-restaurante`, `feature/HU-03-crear-plato`, `feature/HU-04-modificar-plato` y `feature/HU-05-autenticacion`. HU1 y HU5 pertenecen funcionalmente a Usuarios; sus ramas se conservan aquí para reflejar el flujo de trabajo solicitado. El detalle está en [`docs/reporte-git.md`](docs/reporte-git.md).

## Estructura

El código Java está en `src/main/java/com/plazoleta` y se organiza en `config`, `controller`, `domain`, `dto`, `exception`, `repository`, `security` y `service`. Las pruebas están en `src/test/java/com/plazoleta`.