package com.plazoleta.entity;

import jakarta.persistence.*;

/** Vincula un empleado con su restaurante sin agregar columnas a usuario. */
@Entity
@Table(name = "empleado_restaurante", uniqueConstraints =
        @UniqueConstraint(name = "uk_empleado_restaurante_usuario", columnNames = "id_usuario"))
public class EmpleadoRestaurante {
    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_restaurante", nullable = false)
    private Integer idRestaurante;

    protected EmpleadoRestaurante() {}

    public EmpleadoRestaurante(Integer idUsuario, Integer idRestaurante) {
        this.idUsuario = idUsuario;
        this.idRestaurante = idRestaurante;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public Integer getIdRestaurante() { return idRestaurante; }
}
