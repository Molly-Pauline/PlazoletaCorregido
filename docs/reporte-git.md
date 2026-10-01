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
| `release/sprint-1` | Candidato de release del sprint. |

Cada feature y la rama de release se crea desde `develop`. Las ramas se mantienen locales; no se configura remoto ni se publican cambios automáticamente. HU1 y la emisión del token de HU5 pertenecen al repositorio Usuarios, separado de este microservicio.