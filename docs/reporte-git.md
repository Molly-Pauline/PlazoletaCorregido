# Ramas requeridas del Sprint 1

El repositorio local debe conservar estas ramas:

| Rama | Propósito |
| --- | --- |
| `main` | Entregas estables; recibe `release/sprint-1`. |
| `develop` | Integración del sprint. |
| `feature/HU-01-crear-propietario` | HU1, perteneciente al servicio Usuarios. |
| `feature/HU-02-crear-restaurante` | HU2, creación y validación de restaurante. |
| `feature/HU-03-crear-plato` | HU3, creación de plato. |
| `feature/HU-04-modificar-plato` | HU4, actualización restringida de plato propio. |
| `feature/HU-05-autenticacion` | HU5, emisión de token en Usuarios y validación compartida. |
| `feature/HU-07-habilitar-deshabilitar-plato` | HU7, cambiar disponibilidad del plato propio. |
| `feature/HU-09-listar-restaurantes` | HU9, listado alfabético paginado. |
| `feature/HU-10-listar-platos-restaurante` | HU10, menú paginado filtrable por categoría. |
| `feature/HU-11-realizar-pedido` | HU11, crear pedido de un restaurante. |
| `feature/HU-12-listar-pedidos-empleado` | HU12, filtro de pedidos del restaurante del empleado. |
| `feature/HU-13-asignar-pedido` | HU13, asignación del empleado y paso a preparación. |
| `feature/HU-14-notificar-pedido-listo` | HU14, pedido listo y PIN por SMS. |
| `feature/HU-15-entregar-pedido` | HU15, entrega con PIN. |
| `feature/HU-16-cancelar-pedido` | HU16, cancelación de pedidos pendientes. |
| `feature/HU-17-trazabilidad-pedido` | HU17, registro y consulta de cambios de estado. |
| `release/sprint-1` | Candidato de release del sprint. |

Cada feature y la rama de release se crea desde `develop`. Las ramas se mantienen locales; no se configura remoto ni se publican cambios automáticamente. HU1, HU5, HU6 y HU8 pertenecen al repositorio Usuarios; HU2-HU4 y HU7-HU12 pertenecen a Plazoleta. HU13-HU18 están fuera del alcance actual.