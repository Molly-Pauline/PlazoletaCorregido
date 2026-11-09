# Plazoleta de comidas

API REST para gestionar usuarios, restaurantes, platos y pedidos, hecha con Spring Boot 3.5.12 y Java 17. Las historias HU1 a HU17 corren en **una sola aplicación**, en el puerto `8080`: el registro de cuentas, el inicio de sesión y la emisión de JWT ya no dependen de un servicio de Usuarios aparte.

Para preparar la exposición paso a paso, consulta [`docs/guia-demostracion.md`](docs/guia-demostracion.md).

## Requisitos

- JDK 17 o superior
- Maven 3.9 o superior
- MySQL 8 o compatible

## Ejecutar

1. Ejecuta [`database/plazoleta_schema.sql`](database/plazoleta_schema.sql) en MySQL Workbench. Crea la base `plazoleta_bd` y todas sus tablas.
2. Desde la carpeta que contiene `pom.xml`, corre las pruebas y arranca la aplicación:

```powershell
mvn clean test
$env:SPRING_DATASOURCE_USERNAME = "root"
$securePassword = Read-Host "MySQL password" -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
mvn spring-boot:run
```

3. Comprueba que `GET http://localhost:8080/actuator/health` responda `{"status":"UP"}`.
4. Abre la documentación de la API en `http://localhost:8080/swagger-ui.html`.

Las pruebas usan H2 en memoria, así que no necesitan MySQL. Spring no carga archivos `.env` automáticamente: las variables deben existir en la terminal que inicia la aplicación.

Si ya tienes una base anterior, el script no modifica las tablas existentes porque usa `CREATE TABLE IF NOT EXISTS`. Haz un respaldo y sigue [`docs/LEEME_CAMBIOS_HU12.md`](docs/LEEME_CAMBIOS_HU12.md). Para HU13-HU17, Hibernate agrega al arrancar lo que falte; la alternativa manual es [`database/migracion_hu17.sql`](database/migracion_hu17.sql).

## API

Salvo las rutas marcadas como públicas, todas exigen `Authorization: Bearer <token>`.

| Método | Ruta | Permiso | Historia |
| --- | --- | --- | --- |
| `GET` | `/actuator/health` | Público | Salud |
| `POST` | `/api/v1/usuarios/administradores` | Público, solo con `ADMIN_REGISTRATION_ENABLED=true` y sin administrador existente | Inicialización |
| `POST` | `/api/v1/usuarios/propietarios` (o `/registro`) | `ADMINISTRADOR` | HU1 |
| `POST` | `/restaurantes` | `ADMINISTRADOR` | HU2 |
| `POST` | `/platos` | `PROPIETARIO` del restaurante | HU3 |
| `PUT` | `/platos/{idPlato}` | `PROPIETARIO` del restaurante | HU4 |
| `POST` | `/api/v1/usuarios/login` | Público | HU5 |
| `POST` | `/api/v1/usuarios/empleados` | `PROPIETARIO` del restaurante | HU6 |
| `PATCH` | `/platos/{idPlato}/estado` | `PROPIETARIO` del restaurante | HU7 |
| `POST` | `/api/v1/usuarios/clientes` | Público | HU8 |
| `GET` | `/restaurantes?page=0&size=10` | `CLIENTE` | HU9 |
| `GET` | `/restaurantes/{id}/platos?page=0&size=10&categoria=...` | `CLIENTE` | HU10 |
| `POST` | `/pedidos` | `CLIENTE` | HU11 |
| `GET` | `/pedidos?estado=PENDIENTE&page=0&size=10` | `EMPLEADO` del restaurante | HU12 |
| `PATCH` | `/pedidos/{id}/asignar` | `EMPLEADO` del restaurante | HU13 |
| `PATCH` | `/pedidos/{id}/listo` | `EMPLEADO` del restaurante | HU14 |
| `PATCH` | `/pedidos/{id}/entregar` con `{"pin": "123456"}` | `EMPLEADO` del restaurante | HU15 |
| `PATCH` | `/pedidos/{id}/cancelar` | `CLIENTE` dueño del pedido | HU16 |
| `GET` | `/pedidos/{id}/trazabilidad` | `CLIENTE` dueño del pedido | HU17 |
| `GET` | `/restaurantes/{id}/platos/buscar?nombre=...` | `CLIENTE` | Búsqueda |

Cualquier otra ruta exige una identidad válida, porque la última regla de `SecurityConfig` es `anyRequest().authenticated()`.

Reglas de negocio destacadas:

- **HU1:** el propietario debe ser mayor de edad.
- **HU2:** el `idPropietario` debe corresponder a una cuenta con rol `PROPIETARIO`.
- **HU4:** solo se pueden cambiar `precio` y `descripcion`.
- **HU6:** `idRol` debe ser `3` (`EMPLEADO`) y el restaurante debe pertenecer al propietario autenticado.
- **HU11:** el cliente no puede crear otro pedido mientras tenga uno `PENDIENTE`, `EN_PREPARACION` o `LISTO`.
- **HU12 y HU13-HU15:** el empleado solo ve y gestiona pedidos del restaurante firmado en su token.
- **HU14:** genera un PIN de 6 dígitos y lo envía al celular del cliente. Solo se guarda su hash BCrypt.
- **HU15:** exige el pedido en `LISTO` y el PIN correcto. Un pedido `ENTREGADO` ya no cambia de estado.
- **HU16:** solo se cancela en `PENDIENTE`. En otro estado responde: "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse".
- **HU17:** cada cambio de estado, incluida la creación, queda en `pedido_historial`. Los pedidos creados antes de HU17 no tienen esos registros.

## Documentación OpenAPI (Swagger)

Con la aplicación encendida, abre `http://localhost:8080/swagger-ui.html`. Ahí aparecen todas las rutas agrupadas por módulo, con sus datos de entrada y respuestas, y se pueden probar desde el navegador:

1. Ejecuta `POST /api/v1/usuarios/login` con **Try it out** y copia el `token` de la respuesta.
2. Pulsa **Authorize**, arriba a la derecha, pega el token sin la palabra `Bearer` y confirma.
3. Prueba las rutas que permite el rol de ese usuario.

La especificación en JSON está en `http://localhost:8080/v3/api-docs`. La configuración está en `config/OpenApiConfig.java`, y cada controlador describe sus rutas con `@Operation`.

## Patrones de diseño y algoritmos

Más detalle en [`docs/patrones-de-diseno.md`](docs/patrones-de-diseno.md).

- **Patrón Singleton:** Spring crea una sola instancia de cada bean, como la clave de firma del JWT o el codificador de contraseñas, y `PedidoStates` comparte una única instancia de cada estado. Lo comprueba `SingletonBeansTest`.
- **Patrón State:** `entity/estado` define un objeto por cada estado del pedido, y cada uno decide qué transiciones admite. `Pedido` delega en ellos, así las reglas no quedan repartidas en condicionales.

```text
PENDIENTE -> EN_PREPARACION -> LISTO -> ENTREGADO
    |
    +-> CANCELADO
```

- **Merge sort** (`algoritmo/MergeSort`, O(n log n)): ordena la trazabilidad por fecha y el menú por nombre.
- **Búsqueda binaria** (`algoritmo/BusquedaBinaria`, O(log n)): ubica en el menú ordenado los platos cuyo nombre empieza por el texto buscado, sin distinguir mayúsculas ni tildes.

## SMS simulado

`SmsSimuladoService` escribe el mensaje en la consola en lugar de enviarlo, y ese mensaje incluye el PIN. Para enviar SMS reales hay que implementar `NotificacionSmsService` con un proveedor y reemplazar el simulador.

## Autenticación y permisos

### Inicio de sesión

`POST /api/v1/usuarios/login` recibe `correo` y `clave`, compara la clave con su hash BCrypt y devuelve `token`, `idUsuario`, `rol` y `expiraEnSegundos`. El token es un JWT firmado con HS256 y dura 12 horas por defecto.

La API es *stateless*: no hay sesión HTTP, así que cada petición protegida debe traer su propio token. No envíes el JWT en la URL ni lo registres en logs.

### Claims del JWT

| Claim | Uso | Requisito |
| --- | --- | --- |
| `sub` | ID del usuario autenticado | Numérico y positivo |
| `rol` | Autoridad `ROLE_<rol>` usada en las reglas de ruta | `ADMINISTRADOR`, `PROPIETARIO`, `EMPLEADO` o `CLIENTE` |
| `iat` | Fecha de emisión | La agrega el emisor |
| `exp` | Fin de la vigencia | Obligatorio |
| `idRestaurante` | Restaurante del empleado, usado en HU12 | Solo en tokens de empleado |

El contenido de un JWT está codificado, no cifrado. La firma impide alterarlo, pero cualquiera con el token puede leerlo.

### Cómo se valida una solicitud

1. `BearerTokenFilter` lee el encabezado `Authorization: Bearer`.
2. El filtro verifica firma, expiración, ID, rol y restaurante, y crea el principal `AuthenticatedUser`.
3. Las reglas de `SecurityConfig` comprueban el rol exigido por la ruta.
4. Si el permiso depende del recurso, el servicio revisa además la relación guardada. Por ejemplo, ser `PROPIETARIO` no basta para modificar cualquier plato: debe ser el dueño de ese restaurante.

Los controladores obtienen la identidad con `@AuthenticationPrincipal`, nunca de datos enviados en el JSON, en la URL o en otros encabezados.

### Clave de firma

`JwtKeyConfig` genera una clave aleatoria cada vez que arranca la aplicación y no la guarda. Por eso, **todos los tokens dejan de servir al reiniciar** y hay que iniciar sesión de nuevo. Tampoco sirve para varias instancias a la vez: para producción habría que cargar una clave fija desde un gestor de secretos.

### Respuestas de error

| Código | Cuándo aparece |
| --- | --- |
| `400` | Datos inválidos o una regla de negocio incumplida, como un propietario inexistente o un PIN incorrecto |
| `401` | Falta el token en una ruta protegida, o el token es inválido o venció |
| `403` | El rol o el dueño del recurso no corresponden, o el correo o la clave del login son incorrectos |
| `404` | El restaurante consultado en HU10 o el pedido no existe |
| `409` | El cliente ya tiene un pedido activo, la transición de estado no está permitida, o se viola un dato único en la base |

### Primer administrador

Hay dos formas de crearlo. Solo puede existir un administrador.

- **Por API:** `POST /api/v1/usuarios/administradores`, sin token. Responde `403` si el registro está deshabilitado o si ya existe un administrador.
- **Al arrancar:** configura `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` (mínimo 12 caracteres). `AdminBootstrap` lo crea si aún no existe.

El registro por API está **desactivado por defecto**. Para usarlo, define `ADMIN_REGISTRATION_ENABLED=true` antes de arrancar y no la configures en los siguientes arranques. Cuando ya existe un administrador, la ruta responde `403` aunque la variable siga activa.

## Postman

Importa [`postman/Plazoleta.HU1-HU12.postman_collection.json`](postman/Plazoleta.HU1-HU12.postman_collection.json) y [`postman/Plazoleta.HU1-HU12.local.postman_environment.json`](postman/Plazoleta.HU1-HU12.local.postman_environment.json). Antes de usarlos, escribe las claves de prueba en el entorno. La colección guarda sola los tokens y los IDs que devuelve cada respuesta. El PIN de HU15 se copia de la consola a la variable `pinPedido`.

## Configuración

| Variable | Uso | Valor por defecto |
| --- | --- | --- |
| `PORT` | Puerto de la aplicación | `8080` |
| `SPRING_DATASOURCE_URL` | Conexión a la base | `jdbc:mysql://localhost:3306/plazoleta_bd` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Clave de MySQL | Defínela siempre en tu terminal |
| `SQL_LOGGING` | Muestra el SQL de Hibernate con `true` | `false` |
| `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD` | Administrador inicial | Vacías |
| `ADMIN_REGISTRATION_ENABLED` | Registro del primer administrador por API | `false` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de Hibernate para el esquema | `update` |

No subas secretos ni archivos `.env`. En despliegue usa un gestor de secretos, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` y migraciones controladas.

## Estructura

El código Java está en `src/main/java/com/plazoleta` y se organiza en `algoritmo`, `config`, `controller`, `dto/request`, `dto/response`, `entity`, `entity/estado`, `exception`, `repository`, `security`, `service` y `service/impl`. Las pruebas están en `src/test/java/com/plazoleta`.

La carpeta `docs` reúne:

| Documento | Contenido |
| --- | --- |
| [`guia-demostracion.md`](docs/guia-demostracion.md) | Paso a paso para la demostración |
| [`arquitectura.md`](docs/arquitectura.md) | Diagrama y descripción de la arquitectura |
| [`modelo-relacional.md`](docs/modelo-relacional.md) | Diagrama y reglas de la base de datos |
| [`diagrama-clases.md`](docs/diagrama-clases.md) | Diagrama de clases |
| [`patrones-de-diseno.md`](docs/patrones-de-diseno.md) | Singleton y State |
| [`optimizacion.md`](docs/optimizacion.md) | Errores, redundancias y mejoras de rendimiento, y lista de pruebas |
| [`reporte-git.md`](docs/reporte-git.md) | Ramas del proyecto |

Las imágenes y las fuentes Mermaid de los diagramas están en `docs/diagramas`.
