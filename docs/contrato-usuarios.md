# Contrato de integración con Usuarios

HU2 exige comprobar que `idPropietario` existe y su rol es `PROPIETARIO`. El servicio Usuarios implementa el endpoint de validación descrito abajo. HU6 valida propiedad del restaurante mediante una consulta autenticada desde Usuarios hacia Plazoleta.

## Endpoint requerido

`GET /api/v1/usuarios/{id}/validacion-propietario`

- `200` con cuerpo JSON `true`: el usuario existe y su rol es `PROPIETARIO`.
- `200` con cuerpo JSON `false` o `404`: no es un propietario válido.
- Errores de red/servidor: Plazoleta responde `503` y no crea el restaurante.

Plazoleta configura host y ruta mediante `USUARIOS_BASE_URL` y `USUARIOS_OWNER_VALIDATION_PATH`. Las pruebas unitarias sustituyen el puerto con un mock. Ningún servicio lee directamente la base de datos privada del otro.

## Validar restaurante para crear empleado (HU6)

Usuarios reenvía el Bearer del propietario a `GET /restaurantes/{idRestaurante}/propietario/{idPropietario}` en Plazoleta. Plazoleta exige rol `PROPIETARIO`, compara el propietario del token con el identificador solicitado y confirma la relación persistida. Solo con respuesta `true` Usuarios crea la cuenta del empleado asociada al restaurante.

HU5 debe emitir JWT estándar firmado con HS256. El contrato compartido incluye `sub` (ID de usuario numérico), `rol`, `exp` (Unix epoch seconds) y, para empleados, `idRestaurante`. Usuarios y Plazoleta deben usar el mismo `AUTH_SECRET` de al menos 32 bytes UTF-8. Plazoleta valida firma y expiración antes de construir la identidad; usa el `idRestaurante` firmado para limitar HU12 y no acepta un ID de restaurante desde el query string.