package com.plazoleta.exception;

/** Señala que el único cupo de administrador de la plataforma ya está ocupado. */
public class AdminAlreadyExistsException extends RuntimeException {
    public AdminAlreadyExistsException() {
        super("Ya existe un administrador; no se pueden crear más cuentas con ese rol");
    }
}
