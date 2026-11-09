# Optimización del código

Revisión del Sprint 3 en busca de errores, redundancias y problemas de rendimiento. Después de cada cambio se ejecutaron todas las pruebas (`mvn clean test`), y el ciclo completo se comprobó en Postman contra MySQL.

## Cambios aplicados

### Errores corregidos

| Problema | Corrección |
| --- | --- |
| El esquema SQL de `usuario` no coincidía con la entidad: creaba `documento_de_identidad` y `fecha_de_nacimiento`, pero la aplicación usa `documento` y `fecha_nacimiento`, y faltaba `admin_slot` | `database/plazoleta_schema.sql` ahora crea las columnas que usa la entidad, con `admin_slot` único para asegurar un solo administrador |
| `fecha_nacimiento` era `TIMESTAMP`, que en MySQL no admite fechas anteriores a 1970 | Ahora es `DATETIME(6)` |
| El registro del primer administrador quedaba activado siempre, y esa ruta es pública | Se controla con `ADMIN_REGISTRATION_ENABLED`, desactivada por defecto |
| La colección de Postman apuntaba al puerto `8081` del antiguo servicio de Usuarios | Todo usa el puerto `8080` |

### Redundancias eliminadas

| Problema | Corrección |
| --- | --- |
| Paquete `dto/reponse` (mal escrito) duplicado junto a `dto/response` | Se unificaron en `dto/response` |
| Mezcla de nombres `...RequestDTO` y `...Request` | Todos los DTO usan el mismo estilo, sin sufijo `DTO` |
| Colección y entorno de Postman duplicados | Quedó una sola versión de cada uno |
| Pruebas antiguas en `legacy-tests` que Maven no ejecutaba | Eliminadas |
| Propiedades de prueba del antiguo servicio de Usuarios (`app.usuarios.*`) que el código ya no leía | Eliminadas |
| `UsuarioService.esPropietarioValido` no se usaba y repetía la lógica de `LocalUsuarioValidationAdapter` | Eliminado, junto con la consulta `existsByIdAndRol` |
| Imports sin uso en `RestauranteController` y `UsuarioController` | Eliminados |
| `spring.jpa.database-platform` era innecesaria y generaba una advertencia al arrancar | Eliminada: Hibernate detecta el dialecto de MySQL solo |
| Comentarios que describían llamadas al antiguo microservicio de Usuarios | Actualizados |

### Rendimiento

| Problema | Corrección |
| --- | --- |
| Al crear un pedido, cada plato se consultaba dos veces en la base | Se guarda en memoria la primera consulta y se reutiliza (`PedidoServiceImpl.crearConBloqueo`) |
| `spring.jpa.open-in-view` estaba activo por defecto: cada solicitud mantenía una conexión a la base hasta terminar de responder | Desactivado. Las pruebas ya corrían con este valor |

### Errores prevenidos al implementar HU13 a HU17

| Riesgo | Cómo se evitó |
| --- | --- |
| Dos solicitudes simultáneas podrían cambiar el estado de un mismo pedido dos veces | Las transiciones bloquean la fila del pedido durante la transacción (`PedidoRepository.findByIdParaActualizar`) |
| El PIN de entrega quedaría legible para quien vea la base | Se guarda solo su hash BCrypt, y se borra al entregar |
| Reglas de estado repartidas en condicionales, fáciles de romper | Patrón State: cada estado define sus transiciones en una clase |
| Búsqueda de platos comparando el menú uno por uno | Merge sort, O(n log n), y búsqueda binaria, O(log n) |

## Hallazgos pendientes

No se corrigieron porque cambiarían el comportamiento o la configuración del entorno. Se dejan registrados como deuda técnica:

- **Clave de firma aleatoria:** `JwtKeyConfig` genera una clave nueva en cada arranque, así que reiniciar invalida todos los tokens. En producción debería leerse de una variable de entorno.
- **Contraseña por defecto en la configuración:** `application.properties` trae una contraseña de MySQL como valor por defecto. Lo correcto es dejarla vacía y definirla siempre como variable.
- **Código sin uso:** `InvalidBearerTokenException` no se lanza en ningún lugar, y el parámetro `bearerToken` de `LocalOwnershipValidator` se recibe pero no se usa.
- **Búsqueda de platos en memoria:** carga todos los platos activos del restaurante antes de ordenarlos. Es adecuado para menús de tamaño normal; con miles de platos convendría filtrar en la base.
- **SMS simulado:** el PIN se escribe en la consola. Con un proveedor real, como Twilio, el mensaje no debería registrarse.

## Pruebas

| Archivo | Qué comprueba |
| --- | --- |
| `UsuarioServiceTest` | Registro de propietario, administrador, cliente y empleado, y login |
| `PedidoServiceTest`, `PedidoCicloServiceTest` | Creación de pedidos y ciclo HU13 a HU17 |
| `PlatoServiceTest`, `BusquedaPlatoServiceTest` | Platos y búsqueda por nombre |
| `RestauranteServiceTest`, `CatalogRepositoryTest` | Restaurantes y consultas del catálogo |
| `PedidoStateTest`, `AlgoritmosTest` | Patrón State, merge sort y búsqueda binaria |
| `PedidoCreationLockTest` | Bloqueo de pedidos simultáneos |
| `SingletonBeansTest` | Patrón Singleton |
| `PedidoFlujoIntegrationTest` | El ciclo completo por HTTP, de las cuentas a la trazabilidad |
| `PlazoletaApplicationTests` | Que la aplicación arranque |
