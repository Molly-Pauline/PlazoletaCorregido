# Contrato pendiente con Usuarios

HU2 exige comprobar que `idPropietario` existe y su rol es `PROPIETARIO`. El servicio Usuarios disponible hoy expone el registro (`POST /api/v1/usuarios/registro`), pero no una consulta para esta validación. Plazoleta no considera suficiente que el ID sea positivo.

## Endpoint requerido

`GET /api/v1/usuarios/{id}/validacion-propietario`

- `200` con cuerpo JSON `true`: el usuario existe y su rol es `PROPIETARIO`.
- `200` con cuerpo JSON `false` o `404`: no es un propietario válido.
- Errores de red/servidor: Plazoleta responde `503` y no crea el restaurante.

La ruta y el host se configuran con `USUARIOS_OWNER_VALIDATION_PATH` y `USUARIOS_BASE_URL`. Hasta que Usuarios implemente el contrato, las pruebas de Plazoleta sustituyen el puerto con un mock. No se debe resolver esta integración leyendo directamente la base de datos privada de Usuarios.

## Autenticación compartida (HU5)

Usuarios debe emitir JWT estándar firmado con HS256. El contrato compartido incluye `sub` (ID de usuario numérico), `rol`, `exp` (Unix epoch seconds) y, para empleados, `idRestaurante`. Usuarios y Plazoleta deben usar el mismo `AUTH_SECRET` de al menos 32 bytes UTF-8. Plazoleta valida firma y expiración antes de construir la identidad autenticada y aplicar los permisos del rol.