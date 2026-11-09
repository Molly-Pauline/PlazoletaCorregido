# Validaciones de usuarios

Cuentas y restaurantes viven en la misma aplicación, así que estas validaciones son llamadas a servicios locales y no existe un contrato HTTP entre servicios.

## Propietario de un restaurante (HU2)

HU2 exige que `idPropietario` exista y tenga rol `PROPIETARIO`. `LocalUsuarioValidationAdapter` lo comprueba en la tabla `usuario`. Si no se cumple, `POST /restaurantes` responde `400` y no crea el restaurante.

## Restaurante de un empleado (HU6)

`POST /api/v1/usuarios/empleados` exige rol `PROPIETARIO` e `idRol` igual a `3` (`EMPLEADO`). `LocalOwnershipValidator` confirma que el restaurante pertenece al propietario autenticado; si no es así, responde `403`. Al crear el empleado, la relación se guarda en `empleado_restaurante`.

## Identidad (HU5)

El login emite un JWT HS256 con `sub`, `rol`, `exp` y, para empleados, `idRestaurante`. El rol y el restaurante se leen de ese token verificado, nunca de un dato enviado por el cliente.
