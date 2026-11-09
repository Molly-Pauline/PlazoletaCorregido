package com.plazoleta.dto.response;

import lombok.*;

/** Datos publicos devueltos al registrar un propietario; omite deliberadamente la clave almacenada. */
@Getter
@Setter
@Builder
public class RegistroResponse {
    /** Identificador asignado por la base de datos despues de guardar la cuenta. */
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String documentoDeIdentidad;
    private String celular;
    private String rol;
}
