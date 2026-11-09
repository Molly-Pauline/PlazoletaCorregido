# Ajustes del modelo hasta HU-12

- `usuario.documento` se mantiene mapeado desde `Usuario.documentoDeIdentidad`.
- `usuario.idRestaurante` ya no se mapea: la nueva entidad `empleado_restaurante` vincula `id_usuario` con `id_restaurante`.
- Al crear empleados se guarda la relación en una transacción; al iniciar sesión, el JWT del empleado incorpora el restaurante consultado desde esa relación, para filtrar pedidos en HU-12.
- El registro de propietario (`POST /api/v1/usuarios/propietarios`) ya no recibe `rol`: se asigna PROPIETARIO en el servidor. Se exige fechaDeNacimiento (HU-1).
- El registro de administrador ya no recibe `rol`; el cliente tampoco. El registro de empleados conserva `idRol` por requisito HU-6, pero solo acepta EMPLEADO.
- El manejador de errores informa JSON inválido y violaciones de unicidad; SecurityConfig responde JSON a 401 y 403.

## Migración de MySQL existente (IMPORTANTE)

1. **Hacer backup** de la base de datos antes de actualizar.
2. Iniciar una vez con `spring.jpa.hibernate.ddl-auto=update` para que Hibernate cree `empleado_restaurante`.
3. Ejecutar `SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario';` y verificar el nombre exacto de la columna antigua (`idRestaurante`, `IdRestaurante`, etc.).
4. Copiar las asociaciones de empleados existentes a `empleado_restaurante` usando como guía `database/migracion_hu12.sql`. **No borrar la columna antigua antes de migrar.**
5. Comprobar que cada empleado tiene una relación, reiniciar y volver a iniciar sesión con las cuentas de empleado para renovar JWT.
6. Comparar los datos de `documento` y `DocIdentidad` antes de eliminar la columna duplicada. `ddl-auto=update` **no elimina** columnas antiguas.
7. Eliminar las columnas sobrantes solo después de verificar y respaldar datos.

## Pruebas

Ejecutar `mvn clean test` desde la carpeta que contiene `pom.xml`. Este ZIP no ha sido compilado en este entorno porque Maven no está instalado. No utilizar directamente en producción sin verificar pruebas y migración.
