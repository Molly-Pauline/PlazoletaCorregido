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
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import com.plazoleta.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone las operaciones HTTP de cuentas y delega las reglas de negocio al servicio. */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    @Value("${app.bootstrap.admin-registration-enabled:false}")
    private boolean adminRegistrationEnabled;

    /** Crea un propietario; la configuracion de seguridad exige antes un administrador autenticado. */
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
    @PostMapping("/clientes")
    public ResponseEntity<CuentaResponse> registrarCliente(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarCliente(request));
    }

    /** Crea un empleado usando el usuario autenticado y el Bearer original para comprobar pertenencia. */
    @PostMapping("/empleados")
    public ResponseEntity<CuentaResponse> registrarEmpleado(@Valid @RequestBody EmpleadoRequest request,
            @AuthenticationPrincipal AuthenticatedUser propietario, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarEmpleado(
                request, propietario, httpRequest.getHeader("Authorization")));
    }

    /** Verifica las credenciales y devuelve el token compartido emitido por el servicio. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

}
