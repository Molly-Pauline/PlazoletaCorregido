package com.plazoleta.entity;

/** Roles del dominio; cada numero conserva el identificador esperado por el contrato entre servicios. */
public enum Rol {
    ADMINISTRADOR(1),
    CLIENTE(4),
    EMPLEADO(3),
    PROPIETARIO(2);

    /** Identificador numerico estable usado en solicitudes de creacion de empleados. */
    private final int id;

    /** Asocia el rol con su identificador externo. */
    Rol(int id) {
        this.id = id;
    }

    /** Devuelve el identificador que se valida frente al valor recibido por la API. */
    public int getId() {
        return id;
    }
}
