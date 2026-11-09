package com.plazoleta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/** Entidad JPA que persiste identidad, credenciales y rol de cada cuenta del servicio. */
@Entity
@Table(name="usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {
    /** Clave primaria generada por la base de datos. */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    /** El nombre es obligatorio a nivel de persistencia. */
    @Column(nullable=false)
    private String nombre;
    private String apellido;
    /** El documento es unico para evitar duplicar la identidad de una persona. */
   @Column(name = "documento", unique = true)
    private String documentoDeIdentidad;
    private String celular;
    @Column(name = "fecha_nacimiento")
    private Date fechaDeNacimiento;
    /** El correo identifica unicamente la cuenta y se usa para iniciar sesion. */
    @Column(nullable=false, unique=true)
    private String correo;
    /** La clave contiene el hash BCrypt, no la clave original. */
    private String clave;
    /** Se almacena el nombre del enum para que el dato persistido sea legible y estable. */
    @Enumerated(EnumType.STRING)
    private Rol rol;
    /** Valor reservado no nulo para imponer en base de datos un único administrador. */
    @Column(name = "admin_slot", unique = true)
    private String adminSlot;
}
