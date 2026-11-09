# Patrones de diseño

El proyecto aplica dos patrones: **Singleton** y **State**.

## 1. Singleton

**Qué es:** garantiza que una clase tenga una sola instancia en toda la aplicación, y que todos la compartan.

**Dónde está:**

- **Beans de Spring.** Todo lo que lleva `@Service`, `@Component`, `@Configuration` o `@Bean` es, por defecto, un singleton: Spring crea una sola instancia y la inyecta en todos los lugares que la piden. Spring se encarga del patrón, así que las clases no necesitan un método `getInstance()`.
- **La clave de firma del JWT** (`config/JwtKeyConfig`) es el caso donde más importa. Si hubiera dos claves, un token firmado por `HmacTokenService` no podría verificarse en `BearerTokenFilter`. Como el bean `SecretKey` es único, emisor y verificador usan siempre la misma.
- **Otros ejemplos:** `PasswordEncoder` (`config/SecurityConfig`), `HmacTokenService` y todos los servicios.
- **Estados del pedido** (`entity/estado/PedidoStates`): guarda una única instancia de cada estado en un mapa estático y siempre devuelve esa misma. Los estados no tienen datos propios, así que crear uno nuevo en cada cambio sería un desperdicio.

**Evidencia:** la prueba [`SingletonBeansTest`](../src/test/java/com/plazoleta/SingletonBeansTest.java) pide varias veces cada bean al contexto de Spring y comprueba que siempre es el mismo objeto (`assertSame`), y lo mismo con los estados del pedido.

## 2. State

**Qué es:** cuando un objeto se comporta distinto según su estado, cada estado se programa en su propia clase. El objeto delega en la clase de su estado actual, en lugar de llenar sus métodos de `if` o `switch`.

**Por qué aquí:** el pedido cambia de comportamiento según su estado. Por ejemplo, solo se puede cancelar mientras está `PENDIENTE` (HU16), y solo se puede entregar cuando está `LISTO` (HU15).

```text
PENDIENTE --asignar--> EN_PREPARACION --marcarListo--> LISTO --entregar--> ENTREGADO
    |
    +--cancelar--> CANCELADO
```

**Dónde está:** el paquete `entity/estado`.

| Clase | Rol en el patrón |
| --- | --- |
| `PedidoState` | Interfaz. Por defecto rechaza todas las transiciones con un error `409` |
| `PendienteState` | Permite `asignar` y `cancelar` |
| `EnPreparacionState` | Permite `marcarListo` |
| `ListoState` | Permite `entregar` |
| `EntregadoState`, `CanceladoState` | Estados finales: no permiten nada |
| `PedidoStates` | Devuelve el objeto que corresponde a cada valor de `EstadoPedido` |
| `Pedido` | El contexto: sus métodos `asignarEmpleado`, `marcarListo`, `entregar` y `cancelar` preguntan al estado actual |

**Ejemplo:** cuando el cliente cancela, `Pedido.cancelar()` ejecuta `PedidoStates.de(estado).cancelar()`. Si el estado es `PENDIENTE`, devuelve `CANCELADO`. En cualquier otro, la implementación por defecto lanza el mensaje de HU16: "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse".

**Ventajas:**

- Cada regla está en un solo lugar, y es fácil ver qué permite cada estado.
- Agregar un estado nuevo significa crear una clase, sin modificar las demás.
- Las reglas se prueban solas: [`PedidoStateTest`](../src/test/java/com/plazoleta/entity/estado/PedidoStateTest.java) comprueba todas las transiciones permitidas y prohibidas.

## Otros diseños que aparecen en el código

No son los patrones que pide la entrega, pero conviene reconocerlos:

- **Strategy con inyección de dependencias:** `NotificacionSmsService` define cómo enviar un SMS, y `SmsSimuladoService` es una implementación. Para usar Twilio bastaría con otra implementación, sin tocar `PedidoServiceImpl`.
- **Builder:** `Usuario` y `RegistroResponse` se construyen con `Usuario.builder()...build()`, generado por Lombok.
- **Repository:** las interfaces de `repository` aíslan el acceso a la base de datos.
