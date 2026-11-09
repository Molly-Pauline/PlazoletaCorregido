package com.plazoleta.controller;

import com.plazoleta.dto.request.AdministradorRequest;
import com.plazoleta.dto.response.RegistroResponse;
import com.plazoleta.dto.response.CuentaResponse;
import com.plazoleta.dto.response.LoginResponse;
import com.plazoleta.dto.request.ClienteRequest;
import com.plazoleta.dto.request.EmpleadoRequest;
import com.plazoleta.dto.request.LoginRequest;
import com.plazoleta.dto.request.RegistroRequest;
import com.plazoleta.dto.request.PropietarioRequest;
import com.plazoleta.entity.Rol;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import com.plazoleta.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone las operaciones HTTP de cuentas y delega las reglas de negocio al servicio. */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Registro de cuentas e inicio de sesión (HU1, HU5, HU6 y HU8)")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    @Value("${app.bootstrap.admin-registration-enabled:false}")
    private boolean adminRegistrationEnabled;

    /** Crea un propietario; la configuracion de seguridad exige antes un administrador autenticado. */
    @Operation(summary = "HU1 - Crear propietario", description = "Requiere rol ADMINISTRADOR. El propietario debe ser mayor de edad.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping({"/registro", "/propietarios"})
    public ResponseEntity<RegistroResponse> registrarUsuario(@Valid @RequestBody PropietarioRequest request){
        RegistroRequest registro = new RegistroRequest();
        registro.setNombre(request.nombre());
        registro.setApellido(request.apellido());
        registro.setDocumentoDeIdentidad(request.documentoDeIdentidad());
        registro.setCelular(request.celular());
        registro.setFechaDeNacimiento(request.fechaDeNacimiento());
        registro.setCorreo(request.correo());
        registro.setClave(request.clave());
        registro.setRol(Rol.PROPIETARIO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrarUsuario(registro));
    }

    
@Operation(summary = "Crear el primer administrador", description = "Público. Solo funciona si ADMIN_REGISTRATION_ENABLED=true y aún no existe un administrador.")
@PostMapping("/administradores")
public ResponseEntity<RegistroResponse> registrarAdministrador(
        @Valid @RequestBody AdministradorRequest request) {

    if (!adminRegistrationEnabled ||
            usuarioRepository.existsByRol(Rol.ADMINISTRADOR)) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El registro inicial está deshabilitado o ya existe un administrador"
        );
    }

    RegistroRequest registro = new RegistroRequest();
    registro.setNombre(request.nombre());
    registro.setApellido(request.apellido());
    registro.setDocumentoDeIdentidad(request.documentoDeIdentidad());
    registro.setCelular(request.celular());
    registro.setCorreo(request.correo());
    registro.setClave(request.clave());

    // El backend asigna el rol automáticamente.
    registro.setRol(Rol.ADMINISTRADOR);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(usuarioService.registrarUsuario(registro));
}


    /** Permite el alta publica de clientes; @Valid ejecuta las restricciones del DTO antes del servicio. */
    @Operation(summary = "HU8 - Crear cuenta de cliente", description = "Público. El rol CLIENTE lo asigna el servidor.")
    @PostMapping("/clientes")
    public ResponseEntity<CuentaResponse> registrarCliente(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarCliente(request));
    }

    /** Crea un empleado usando el usuario autenticado y el Bearer original para comprobar pertenencia. */
    @Operation(summary = "HU6 - Crear empleado", description = "Requiere rol PROPIETARIO del restaurante. idRol debe ser 3 (EMPLEADO).")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/empleados")
    public ResponseEntity<CuentaResponse> registrarEmpleado(@Valid @RequestBody EmpleadoRequest request,
            @AuthenticationPrincipal AuthenticatedUser propietario, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarEmpleado(
                request, propietario, httpRequest.getHeader("Authorization")));
    }

    /** Verifica las credenciales y devuelve el token compartido emitido por el servicio. */
    @Operation(summary = "HU5 - Iniciar sesión", description = "Público. Devuelve el JWT que se usa en las demás rutas.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

}
