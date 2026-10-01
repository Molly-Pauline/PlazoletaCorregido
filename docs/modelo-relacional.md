# Modelo relacional

```text
Restaurante(
  id PK,
  nombre,
  nit UNIQUE,
  direccion,
  telefono,
  url_logo,
  id_propietario
)

Plato(
  id PK,
  nombre,
  precio,
  descripcion,
  url_imagen,
  categoria,
  activo,
  id_restaurante FK -> Restaurante.id
)
```

Un restaurante conserva el identificador del propietario en Usuarios. Un restaurante contiene cero o más platos; cada plato pertenece a un único restaurante. `activo` se fuerza a `true` al crear un plato y no forma parte del DTO de actualización HU4.