# Guía de demostración de Plazoleta

Esta guía está escrita para poder seguirla sin saber programar. Léela una vez antes de presentar. En la exposición puedes leer en voz alta las frases marcadas como **Di**. Los textos entre `<...>` son espacios para reemplazar por tus propios datos; no escribas los símbolos `<` y `>`.

> **Idea principal:** Plazoleta comprueba el correo y la contraseña y entrega un JWT. Después recibe ese JWT en cada operación, revisa que sea válido y deja hacer solo las acciones que corresponden al rol firmado.

## 1. Qué vas a mostrar

En esta demostración vas a enseñar:

1. Que la aplicación está encendida.
2. Que una solicitud protegida necesita un token válido.
3. Que cada rol puede hacer solo sus propias operaciones.
4. Que el propietario administra los platos de su restaurante y crea a sus empleados.
5. Que el cliente puede hacer un pedido y el empleado solo consulta los pedidos de su restaurante.
6. Que el pedido recorre su ciclo completo: preparación, listo con PIN por SMS, entrega y trazabilidad.
7. Que la identidad y el rol vienen del JWT, no de campos que se puedan cambiar en Postman.
8. El patrón de diseño State y los algoritmos de ordenamiento y búsqueda.

**Di:**

> “Voy a mostrar una API que gestiona usuarios, restaurantes, platos y pedidos. Al iniciar sesión, cada persona recibe un JWT firmado, y la aplicación usa el rol de ese token para decidir qué operaciones puede realizar.”

## 2. Palabras que necesitas conocer

- **Aplicación:** el programa que recibe solicitudes y responde. Aquí hay una sola, que incluye cuentas, restaurantes, platos y pedidos.
- **Servidor:** un programa que se queda encendido y espera solicitudes. Plazoleta se ejecuta en un servidor local, en el puerto `8080`.
- **Endpoint o ruta:** una dirección para pedir una acción o información, por ejemplo `/restaurantes`.
- **Postman:** el programa que usaremos para enviar solicitudes y mirar las respuestas sin construir una página web.
- **JWT:** una credencial digital firmada. Piensa en ella como una tarjeta temporal que dice quién es la persona y qué rol tiene. No es una contraseña y su contenido no está cifrado.
- **Bearer token:** la forma de adjuntar esa credencial a una solicitud: `Authorization: Bearer <token>`.
- **Claim:** un dato escrito dentro del JWT, como el ID (`sub`), el rol (`rol`) o la fecha de vencimiento (`exp`).
- **Rol:** el tipo de usuario: `ADMINISTRADOR`, `PROPIETARIO`, `CLIENTE` o `EMPLEADO`.
- **Base de datos:** el lugar donde se guardan usuarios, restaurantes, platos y pedidos.

## 3. Lo que debes tener preparado antes de presentar

Haz esta preparación antes de que la profesora llegue. No es buena idea instalar o configurar cosas por primera vez durante la exposición.

Asegúrate de tener:

- El proyecto Plazoleta abierto en tu editor.
- Java JDK 17 o superior.
- Maven 3.9 o superior disponible en PowerShell.
- MySQL y MySQL Workbench funcionando.
- Postman instalado.
- Una contraseña de prueba de al menos 8 caracteres para cada rol. Si vas a crear el administrador al arrancar, la suya debe tener al menos 12.

No necesitas iniciar ningún otro servicio ni crear las cuentas por adelantado: la colección de Postman crea las de propietario, empleado y cliente durante el recorrido.

No escribas contraseñas ni JWT en el README, en el código, en una captura pública ni en un commit.

## 4. Preparar la base de datos

Haz esto antes de encender Plazoleta.

1. Abre **MySQL Workbench**.
2. En la pantalla inicial, abre la conexión local de MySQL que usas para el proyecto.
3. Abre el archivo `database/plazoleta_schema.sql` y ejecútalo con el botón del rayo.
4. En el panel de esquemas, busca `plazoleta_bd`. Pulsa el botón de actualizar si no aparece inmediatamente.
5. Comprueba que aparecen las tablas `usuario`, `empleado_restaurante`, `restaurantes`, `platos`, `pedidos`, `pedido_detalles` y `pedido_historial`.

El script crea las tablas si no existen; no borra información anterior ni actualiza automáticamente tablas viejas. Si tu base viene de una versión anterior, sigue `docs/LEEME_CAMBIOS_HU12.md` antes de presentar. **No borres la base durante la exposición.**

**Di:**

> “La base guarda los usuarios y los datos del negocio. Las contraseñas se guardan cifradas con BCrypt, nunca en texto plano.”

## 5. Encender la aplicación

1. Abre una terminal de PowerShell en la carpeta del proyecto, la que contiene `pom.xml`.
2. Comprueba Java. Debe aparecer la versión 17 o una superior:

```powershell
java -version
```

3. Ejecuta las pruebas. No necesitan MySQL, porque usan una base temporal en memoria. Hazlo antes del siguiente paso:

```powershell
mvn clean test
```

Debe terminar con `BUILD SUCCESS`. Si falla, no presentes todavía: copia el error y resuélvelo primero.

4. Configura en esa misma terminal los datos de MySQL. Las variables solo duran mientras la terminal esté abierta:

```powershell
$env:SPRING_DATASOURCE_USERNAME = "root"
$secureDbPassword = Read-Host "Escribe la clave local de MySQL" -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new("", $secureDbPassword).Password
```

5. Si aún no existe un administrador, activa su registro. Hazlo solo esa primera vez:

```powershell
$env:ADMIN_REGISTRATION_ENABLED = "true"
```

6. Inicia la aplicación:

```powershell
mvn spring-boot:run
```

7. Espera hasta ver un mensaje que diga que la aplicación ha iniciado. Deja esta terminal abierta: si la cierras, la aplicación se detiene.

> **Importante:** cada vez que reinicias la aplicación se genera una nueva clave de firma y todos los tokens anteriores dejan de servir. Si reinicias, vuelve a ejecutar los inicios de sesión. Las cuentas y los datos sí se conservan.

**Di:**

> “La aplicación ya está encendida en el puerto 8080. Ahí mismo se encarga del inicio de sesión, los restaurantes, los platos y los pedidos.”

## 6. Preparar Postman

Hazlo antes de enviar la primera solicitud.

1. Abre Postman.
2. Pulsa **Import** (Importar) y selecciona `postman/Plazoleta.HU1-HU12.postman_collection.json`.
3. Repite **Import** y selecciona `postman/Plazoleta.HU1-HU12.local.postman_environment.json`. Los archivos conservan ese nombre, pero ya incluyen hasta HU17.
4. En la parte superior derecha, abre el selector de entornos y elige **Plazoleta HU1-HU17 Local**.
5. Abre el entorno con el botón de editar o el icono del ojo.
6. Verifica estas direcciones:
   - `plazoletaBaseUrl`: `http://localhost:8080`
   - `usuariosBaseUrl`: `http://localhost:8080/api/v1/usuarios`
7. Escribe tus contraseñas de prueba en `propietarioClave`, `empleadoClave` y `clienteClave`.
8. Completa los datos del administrador:
   - **Si aún no existe:** escribe su contraseña en `adminNuevoClave`.
   - **Si ya existe:** escribe su correo en `adminCorreo` y su contraseña en `adminClave`.
9. Pulsa **Save** (Guardar) y cierra la edición del entorno.

> **Alternativa sin Postman:** con la aplicación encendida, abre `http://localhost:8080/swagger-ui.html` en el navegador. Ahí puedes enviar las mismas solicitudes: inicia sesión con **Try it out** en `POST /api/v1/usuarios/login`, copia el token y pégalo en **Authorize**. Para cambiar de rol, repite el login con otro usuario y vuelve a pulsar **Authorize**.

En la colección verás cuatro grupos: **0 - Preparación**, **HU1-HU6 - Cuentas y restaurante**, **HU8-HU12 - Cliente, menu y pedidos** y **HU13-HU17 - Ciclo del pedido y busqueda**. La solicitud **HU7** está suelta al final.

## 7. Cómo funcionan los tokens en Postman

No tienes que copiar ni pegar tokens o IDs. Cada solicitud de login guarda el JWT en la variable del rol, y las solicitudes de creación guardan los IDs nuevos:

| Rol | Solicitud que lo guarda | Variable |
| --- | --- | --- |
| Administrador | **HU5 - Login administrador existente** | `tokenAdmin` |
| Propietario | **HU5 - Login propietario** | `tokenPropietario` |
| Cliente | **HU5 - Login cliente** | `tokenCliente` |
| Empleado | **HU5 - Login empleado** | `tokenEmpleado` |

Cada solicitud protegida ya está configurada con el token del rol que le corresponde. Para ver cuál usa, abre su pestaña **Authorization**.

Sigue el orden de esta guía, porque cada paso usa datos guardados por los anteriores. Algunas solicitudes generan un correo y un documento nuevos cada vez que las envías. Si repites **HU1**, se crea otro propietario y debes repetir también su login y los pasos siguientes.

## 8. Demostración principal

Sigue este orden. Crea el pedido **antes** de deshabilitar el plato, porque Plazoleta no permite hacer pedidos de platos inactivos.

### Paso 1: comprobar que Plazoleta responde

1. Abre **Salud - Plazoleta** y pulsa **Send**.
2. Busca una respuesta `200 OK` con este contenido:

```json
{"status":"UP"}
```

**Di:**

> “Esta es la comprobación de salud. El resultado UP indica que la aplicación está funcionando. Esta ruta es pública y no necesita un token.”

Si no aparece `200`, no continúes: revisa la sección de problemas al final.

### Paso 2: preparar al administrador

1. Si no existe un administrador, envía **Inicialización - Crear primer administrador**. Espera `201 Created`. El entorno guarda su correo y su contraseña.
2. Envía **HU5 - Login administrador existente**. Espera `200 OK`.

Si la inicialización responde `403`, puede que ya exista un administrador: usa sus datos en `adminCorreo` y `adminClave`. Si no existe, detén la aplicación, activa `ADMIN_REGISTRATION_ENABLED` como en la sección 5 y vuelve a iniciarla.

**Di:**

> “Solo puede existir un administrador. Al iniciar sesión recibe un token firmado que demuestra quién es y qué rol tiene.”

### Paso 3: crear el propietario (HU1)

1. Abre **HU1 - Crear propietario (requiere administrador)** y pulsa **Send**. Espera `201 Created`.
2. Envía **HU5 - Login propietario**. Espera `200 OK`.

**Di:**

> “Solo el administrador puede crear propietarios. El rol lo asigna el servidor, y el propietario debe ser mayor de edad.”

### Paso 4: crear un restaurante (HU2)

1. Abre **HU2 - Crear restaurante (administrador)**.
2. Revisa en **Body** los campos `nombre`, `nit`, `direccion`, `telefono`, `urlLogo` e `idPropietario`. Postman completa el NIT y el ID del propietario.
3. Pulsa **Send**. Espera `201 Created`. El ID del restaurante queda guardado.

**Di:**

> “Solo un administrador puede crear restaurantes. Además, la aplicación confirma que el ID indicado pertenece a una cuenta con rol de propietario.”

### Paso 5: crear un plato (HU3)

1. Abre **HU3 - Crear plato (propietario)** y pulsa **Send**. Espera `201 Created`. El ID del plato queda guardado.

**Di:**

> “El propietario puede crear un plato solo en un restaurante del que realmente es dueño. La aplicación comprueba esa relación en la base de datos; no se fía solo del número que venía en la solicitud.”

### Paso 6: modificar el plato (HU4)

1. Abre **HU4 - Modificar plato propio**. En **Body** solo aparecen `precio` y `descripcion`.
2. Pulsa **Send**. Espera `200 OK`.
3. Mira el resultado: cambian el precio y la descripción, y lo demás se conserva.

**Di:**

> “HU4 permite cambiar solo el precio y la descripción. El nombre, la categoría y la imagen se conservan.”

### Paso 7: crear un empleado (HU6)

1. Abre **HU6 - Crear empleado (propietario)**. En **Body** verás `idRol` con valor `3`, que corresponde a `EMPLEADO`, y el `idRestaurante`.
2. Pulsa **Send**. Espera `201 Created`.

**Di:**

> “El propietario solo puede crear empleados para su propio restaurante. La aplicación guarda esa relación, y por eso el token del empleado llevará firmado su restaurante.”

### Paso 8: crear el cliente y consultar el catálogo (HU8, HU9 y HU10)

1. Envía **HU8 - Crear cuenta cliente**. Espera `201 Created`. Esta ruta es pública.
2. Envía **HU5 - Login cliente**. Espera `200 OK`.
3. Envía **HU9 - Listar restaurantes paginados (cliente)**. Espera `200 OK` y revisa la página de restaurantes.
4. Envía **HU10 - Consultar menu por categoria (cliente)**. Debes ver el plato que creaste.

**Di:**

> “Cualquier persona puede registrarse como cliente. El cliente consulta el catálogo: el menú se filtra por restaurante, muestra platos activos y puede filtrar por categoría.”

### Paso 9: demostrar que una ruta protegida no acepta solicitudes anónimas

1. En **HU9 - Listar restaurantes paginados (cliente)**, abre la pestaña **Authorization**.
2. Cambia temporalmente el tipo a **No Auth** (Sin autenticación) y pulsa **Send**.
3. Espera `401 Unauthorized`.
4. Vuelve a poner el tipo **Bearer Token** con el valor `{{tokenCliente}}`.

**Di:**

> “La lista de restaurantes no es pública. Sin una credencial válida, la aplicación responde 401 y no entrega los datos.”

### Paso 10: crear un pedido (HU11)

Haz este paso antes de deshabilitar el plato.

1. Abre **HU11 - Crear pedido (cliente)**. La solicitud pide 2 unidades. Con el precio de `30000`, el total esperado es `60000`.
2. Pulsa **Send** y espera `201 Created`.
3. En la respuesta, comprueba que el estado inicial es `PENDIENTE`.

**Di:**

> “El cliente no envía su propio ID ni decide el precio. La aplicación toma el cliente del JWT y calcula el total con el precio guardado en el plato.”

**Importante:** HU11 impide crear otro pedido mientras el cliente tenga uno `PENDIENTE`, `EN_PREPARACION` o `LISTO`. Si repites la solicitud, responderá `409 Conflict`; eso demuestra la regla, no que el servidor se haya caído.

### Paso 11: consultar los pedidos como empleado (HU12)

1. Envía **HU5 - Login empleado**. Espera `200 OK`.
2. Envía **HU12 - Listar pedidos por estado (empleado)**. Espera `200 OK` y busca el pedido recién creado.

**Di:**

> “El empleado consulta los pedidos de su restaurante. El ID del restaurante viene firmado dentro de su token, así que no puede cambiarlo para ver los pedidos de otro local.”

### Paso 12: asignarse el pedido (HU13)

1. Envía **HU13 - Asignarse el pedido (empleado)**. Espera `200 OK`, con estado `EN_PREPARACION` y el `idEmpleado` lleno.
2. Envía **HU16 - Cancelar pedido en preparacion (debe rechazarse)**. Espera `409 Conflict` con el mensaje "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse".

**Di:**

> “El empleado se asigna el pedido y el cliente ya puede ver que está en preparación. A partir de aquí, el cliente ya no puede cancelarlo.”

### Paso 13: marcar el pedido como listo (HU14)

1. Envía **HU14 - Marcar pedido listo y enviar PIN (empleado)**. Espera `200 OK` con estado `LISTO`.
2. Mira la terminal de la aplicación y busca una línea que empieza por `[SMS SIMULADO]`. Ahí está el PIN de 6 dígitos.
3. Copia el PIN en la variable `pinPedido` del entorno y pulsa **Save**.

**Di:**

> “Cuando el pedido está listo, el cliente recibe un PIN por SMS. En esta demostración el envío es simulado y aparece en la consola; para enviarlo de verdad bastaría con conectar un proveedor. En la base solo se guarda el PIN cifrado.”

### Paso 14: entregar el pedido (HU15)

1. Envía **HU15 - Entregar pedido con PIN (empleado)**. Espera `200 OK` con estado `ENTREGADO`.
2. Si quieres mostrar la regla, antes de ese envío cambia temporalmente `pinPedido` por otro número de 6 dígitos: responderá `400`.

**Di:**

> “Solo un pedido listo se puede entregar, y el empleado debe digitar el PIN que recibió el cliente. Después de entregado, el pedido ya no cambia de estado.”

### Paso 15: consultar la trazabilidad (HU17)

1. Envía **HU17 - Consultar trazabilidad (cliente)**. Espera `200 OK`.
2. Muestra la lista: va de la creación a la entrega, con la fecha y el usuario de cada cambio.

**Di:**

> “Cada cambio de estado queda registrado, y cada cliente solo puede consultar el historial de sus propios pedidos. Así puede ver la rapidez del servicio.”

### Paso 16: cancelar un pedido pendiente (HU16)

1. Envía **HU11 - Crear otro pedido para cancelar (cliente)**. Espera `201 Created`.
2. Envía **HU16 - Cancelar pedido pendiente (cliente)**. Espera `200 OK` con estado `CANCELADO`.

**Di:**

> “Mientras el pedido siga pendiente, el cliente puede retractarse.”

### Paso 17: buscar un plato por nombre

1. Envía **Busqueda - Buscar plato por nombre (cliente)**, que busca `hamb`. Espera `200 OK` con el plato creado.

**Di:**

> “Esta búsqueda usa los dos algoritmos: merge sort ordena el menú por nombre, y la búsqueda binaria encuentra los platos que empiezan por ese texto, sin distinguir mayúsculas ni tildes.”

### Paso 18: deshabilitar el plato (HU7)

1. Abre **HU7 - Habilitar o deshabilitar plato (propietario)**. Deja `activo` en `false` y pulsa **Send**.
2. Espera `200 OK` y mira que la respuesta informe `activo: false`.
3. Vuelve a enviar **HU10 - Consultar menu por categoria (cliente)**. El plato ya no debe aparecer.

**Di:**

> “Deshabilitar no borra el plato. Lo oculta del menú y evita que se use en pedidos nuevos, pero conserva sus datos.”

Si quieres dejarlo activo, repite HU7 con `"activo": true`.

## 9. Demostrar que los roles sí se controlan

Haz esta prueba después del recorrido principal.

1. Abre **HU2 - Crear restaurante (administrador)** y su pestaña **Authorization**.
2. Cambia temporalmente el token a `{{tokenCliente}}` y pulsa **Send**.
3. La respuesta esperada es `403 Forbidden`, porque un cliente no es administrador.
4. Vuelve a poner `{{tokenAdmin}}`.

**Di:**

> “Un token válido identifica al usuario, pero no le da todos los permisos. La ruta exige el rol administrador y rechaza al cliente con 403.”

## 10. Qué significan las respuestas

| Respuesta | Cómo explicarla durante la demo |
| --- | --- |
| `200 OK` | La solicitud se procesó correctamente. |
| `201 Created` | Se creó un recurso, como una cuenta, un restaurante, un plato o un pedido. |
| `400 Bad Request` | Algún dato no cumple las reglas, por ejemplo un precio no positivo, un NIT repetido o un PIN incorrecto. |
| `401 Unauthorized` | Falta el JWT, es inválido o ya venció. |
| `403 Forbidden` | El rol no puede hacer esa acción, el usuario no es dueño del recurso, o el correo o la contraseña del login son incorrectos. |
| `404 Not Found` | El restaurante o el pedido consultado no existe. |
| `409 Conflict` | La solicitud choca con el estado actual; por ejemplo, el cliente ya tiene un pedido activo o el pedido no admite ese cambio de estado. |

No digas que un error esperado es un fallo del proyecto. Explica qué regla provocó esa respuesta.

## 11. Qué está conectado con qué

```text
Postman
  |  HTTP + Bearer JWT
  v
Plazoleta (localhost:8080)
  |  cuentas, login, restaurantes, platos y pedidos
  v
MySQL (plazoleta_bd)
```

- Postman envía todas las solicitudes a la misma aplicación.
- El login comprueba el correo y la contraseña, y emite el JWT.
- En las demás solicitudes, el JWT va en el encabezado `Authorization`.
- La aplicación verifica el token, aplica permisos según el rol y revisa en la base quién es dueño de cada recurso.

### Patrón de diseño y algoritmos

- **Patrón State (`entity/estado`):** cada estado del pedido es un objeto que decide qué transiciones admite.
- **Merge sort (`algoritmo/MergeSort`):** ordena la trazabilidad por fecha y el menú por nombre.
- **Búsqueda binaria (`algoritmo/BusquedaBinaria`):** encuentra platos por el inicio de su nombre.

**Di:**

> “Usé el patrón State porque el pedido cambia de comportamiento según su estado: por ejemplo, solo el estado pendiente permite cancelar. Cada regla queda en una clase, y agregar un estado no obliga a tocar las demás. Merge sort garantiza un tiempo de n log n, y la búsqueda binaria reduce cada comparación a la mitad de la lista.”

## 12. Problemas comunes y qué hacer

### Postman muestra “Could not get any response”

Plazoleta puede estar apagada. Revisa la terminal donde ejecutaste `mvn spring-boot:run` y verifica en el entorno que `plazoletaBaseUrl` y `usuariosBaseUrl` usen el puerto `8080`.

### Postman devuelve `401`

El token no está guardado, es inválido o venció. Si reiniciaste la aplicación, los tokens anteriores ya no sirven: vuelve a enviar el login del rol que estás usando. Un token vigente de otro rol no da `401`, sino `403`.

### Postman devuelve `403`

Revisa estas causas:

- **En un login:** el correo o la contraseña son incorrectos. Comprueba las variables del entorno.
- **En la inicialización del administrador:** ya existe un administrador o no activaste `ADMIN_REGISTRATION_ENABLED` antes de arrancar.
- **En otra solicitud:** el rol no corresponde, o el propietario no es dueño de ese restaurante. Si repetiste **HU1**, repite también los pasos siguientes.

### Una solicitud de cuenta responde `400`

Revisa que hayas escrito las contraseñas en el entorno y que tengan al menos 8 caracteres. También puede ser un correo o un documento ya registrado.

### Crear restaurante responde `400`

Comprueba que `idPropietario` sea de una cuenta real con rol `PROPIETARIO` y que el NIT no exista ya.

### Maven indica que el puerto 8080 ya está ocupado

Puede haber otra instancia de Plazoleta funcionando. Detenla desde su terminal con `Ctrl+C` antes de volver a iniciar.

### El pedido devuelve `409`

El cliente ya tiene un pedido en curso. Envía otra vez **HU8** y **HU5 - Login cliente** para usar un cliente nuevo. No borres filas manualmente durante la exposición.

### HU15 responde `400`

El PIN no coincide o no tiene 6 dígitos. Copia en `pinPedido` el último PIN de la línea `[SMS SIMULADO]` de la consola. Si repetiste HU14 sobre otro pedido, usa el de ese pedido.

### HU13, HU14 o HU15 responden `409`

El pedido no está en el estado que esa acción necesita. El orden es HU13, HU14 y HU15. Si cancelaste el pedido o ya lo entregaste, crea uno nuevo.

### Un plato no aparece en HU10 o no se puede pedir

Comprueba que el plato siga activo. HU7 con `activo: false` lo quita del menú y lo bloquea para pedidos nuevos.

## 13. Cierre de la presentación

**Di:**

> “En resumen, la aplicación comprueba las credenciales, firma el JWT y lo verifica en cada solicitud. Con esa identidad revisa los permisos tanto en la ruta como en el servicio, y para los recursos propios también verifica la relación guardada en la base. Así, conocer una URL o cambiar un ID en Postman no basta para obtener permisos.”

Después de terminar, detén la aplicación con `Ctrl+C` en su terminal y cierra Postman. No enseñes ni guardes contraseñas ni JWT reales en capturas.
