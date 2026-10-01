# Contrato pendiente con Usuarios

HU2 exige comprobar que `idPropietario` existe y su rol es `PROPIETARIO`. El servicio Usuarios disponible hoy expone el registro (`POST /api/v1/usuarios/registro`), pero no una consulta para esta validación. Plazoleta no considera suficiente que el ID sea positivo.

## Endpoint requerido

`GET /api/v1/usuarios/{id}/validacion-propietario`

- `200` con cuerpo JSON `true`: el usuario existe y su rol es `PROPIETARIO`.
- `200` con cuerpo JSON `false` o `404`: no es un propietario válido.
- Errores de red/servidor: Plazoleta responde `503` y no crea el restaurante.

La ruta y el host se configuran con `USUARIOS_OWNER_VALIDATION_PATH` y `USUARIOS_BASE_URL`. Hasta que Usuarios implemente el contrato, las pruebas de Plazoleta sustituyen el puerto con un mock. No se debe resolver esta integración leyendo directamente la base de datos privada de Usuarios.