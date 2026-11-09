package com.plazoleta.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.plazoleta.entity.Rol;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/** Entrada heredada para registrar propietarios, con validaciones de formato delegadas a Bean Validation. */
@Getter
@Setter
public class RegistroRequest {
    /** El nombre y el apellido se exigen para identificar a la persona. */
    @NotBlank
    private String nombre;
    /** El documento es obligatorio y solo admite caracteres numericos. */
    @NotBlank
    private String apellido;
    /** El telefono admite opcionalmente un + inicial y no puede superar trece caracteres. */
    @NotBlank
    @Pattern(regexp = "^[0-9]+$")
    private String documentoDeIdentidad;
    @NotBlank
    @Size(max = 13)
    @Pattern(regexp = "\\+?\\d+")
    private String celular;
    /** La fecha se comprueba en el servicio porque la edad depende de la fecha actual. */
    private Date fechaDeNacimiento;
    /** Se exige una direccion no vacia con formato de correo. */
    @NotBlank
    @Email
    private String correo;
    /** La clave no puede estar vacia; el servicio nunca la guarda en texto plano. */
    @NotBlank
    private String clave;
    /** Solo una cuenta ADMINISTRADOR puede elegir entre los roles administrativos permitidos por el servicio. */
    @NotNull
    private Rol rol;
}
