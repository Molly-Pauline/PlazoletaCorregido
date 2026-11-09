package com.plazoleta.service.impl;

import com.plazoleta.dto.response.RegistroResponse;
import com.plazoleta.dto.response.CuentaResponse;
import com.plazoleta.dto.response.LoginResponse;
import com.plazoleta.dto.request.ClienteRequest;
import com.plazoleta.dto.request.EmpleadoRequest;
import com.plazoleta.dto.request.LoginRequest;
import com.plazoleta.dto.request.RegistroRequest;
import com.plazoleta.entity.Rol;
import com.plazoleta.entity.Usuario;
import com.plazoleta.entity.EmpleadoRestaurante;
import com.plazoleta.repository.EmpleadoRestauranteRepository;
import com.plazoleta.repository.UsuarioRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.security.HmacTokenService;
import com.plazoleta.service.LocalOwnershipValidator;
import com.plazoleta.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Period;

/** Implementa las reglas de cuentas y coordina persistencia, hashing, tokens y validaciones entre servicios. */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    /** Persistencia de cuentas. */
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRestauranteRepository empleadoRestauranteRepository;
    /** Codificador usado tanto para guardar claves como para verificarlas en login. */
    private final PasswordEncoder passwordEncoder;
    /** Emisor del formato de token que entiende el servicio consumidor. */
    private final HmacTokenService tokenService;
    /** Adaptador de comprobacion externa de pertenencia a restaurante. */
    private final LocalOwnershipValidator ownershipClient;

    /** Ejecuta el alta completa como transaccion: los errores impiden confirmar cambios parciales. */
    @Override
    @Transactional
    public RegistroResponse registrarUsuario(RegistroRequest registroRequest) {
        Rol rolSolicitado = registroRequest.getRol();
        if (rolSolicitado != Rol.ADMINISTRADOR && rolSolicitado != Rol.PROPIETARIO) {
            throw new IllegalArgumentException(
                    "Este registro solo permite los roles ADMINISTRADOR o PROPIETARIO");
        }
        if (rolSolicitado == Rol.ADMINISTRADOR && usuarioRepository.existsByRol(Rol.ADMINISTRADOR)) {
            throw new com.plazoleta.exception.AdminAlreadyExistsException();
        }
        // Se valida unicidad antes de crear la entidad para responder con un error de dominio claro.
        if (usuarioRepository.existsByCorreo(registroRequest.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        validarDocumentoDisponible(registroRequest.getDocumentoDeIdentidad());
        if (rolSolicitado == Rol.PROPIETARIO) {
            if (registroRequest.getFechaDeNacimiento() == null) {
                throw new IllegalArgumentException("La fecha de nacimiento es obligatoria para crear un propietario");
            }
            LocalDate fechaNacimiento = registroRequest.getFechaDeNacimiento().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
                throw new IllegalArgumentException("El propietario debe tener al menos 18 años");
            }
        }
        // El endpoint solo admite roles administrativos y BCrypt evita persistir la clave en texto plano.
        Usuario usuario = Usuario.builder()
                .nombre(registroRequest.getNombre())
                .apellido(registroRequest.getApellido())
                .documentoDeIdentidad(registroRequest.getDocumentoDeIdentidad())
                .celular(registroRequest.getCelular())
                .fechaDeNacimiento(registroRequest.getFechaDeNacimiento())
                .correo(registroRequest.getCorreo())
                .clave(passwordEncoder.encode(registroRequest.getClave()))
                .rol(rolSolicitado)
                .adminSlot(rolSolicitado == Rol.ADMINISTRADOR ? "ADMINISTRADOR_UNICO" : null)
                .build();

        // La transaccion guarda la cuenta; el id generado se usa para construir la respuesta.
        Usuario usuarioGuardado =  usuarioRepository.save(usuario);

        return RegistroResponse.builder()
            .id(usuarioGuardado.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .documentoDeIdentidad(usuario.getDocumentoDeIdentidad())
                .celular(usuario.getCelular())
                .rol(usuarioGuardado.getRol().name())
                .build();
    }

    /** Registra un cliente en transaccion, fijando el rol y codificando su clave antes de guardar. */
    @Override
    @Transactional
    public CuentaResponse registrarCliente(ClienteRequest request) {
        // Ambas comprobaciones protegen las restricciones unicas y ofrecen errores comprensibles.
        validarCorreoDisponible(request.correo());
        validarDocumentoDisponible(request.documentoDeIdentidad());
        // El cliente no puede solicitar un rol privilegiado: siempre se asigna CLIENTE en el servidor.
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .documentoDeIdentidad(request.documentoDeIdentidad())
                .celular(request.celular())
                .correo(request.correo())
                .clave(passwordEncoder.encode(request.clave()))
                .rol(Rol.CLIENTE)
                .build());
        return respuestaCuenta(usuario);
    }

    /** Crea un empleado solo despues de autenticar el rol y confirmar la pertenencia al restaurante. */
    @Override
    @Transactional
    public CuentaResponse registrarEmpleado(EmpleadoRequest request, AuthenticatedUser propietario,
                                                String bearerToken) {
        // Un objeto principal ausente o un rol distinto bloquea la operacion antes de cualquier escritura.
        if (propietario == null || !"PROPIETARIO".equals(propietario.rol())) {
            throw new SecurityException("Solo un propietario autenticado puede crear empleados");
        }
        // No se permite que el solicitante cambie el rol final manipulando el id recibido en el DTO.
        if (request.idRol() != Rol.EMPLEADO.getId()) {
            throw new IllegalArgumentException("idRol no corresponde al rol EMPLEADO");
        }
        // La propiedad se confirma en el otro servicio con el usuario autenticado y el token de la solicitud.
        if (!ownershipClient.propietarioEsDueno(request.idRestaurante(), propietario.idUsuario(), bearerToken)) {
            throw new SecurityException("El restaurante no pertenece al propietario autenticado");
        }
        // Solo despues de autorizar se validan duplicados y se persiste al nuevo empleado.
        validarCorreoDisponible(request.correo());
        validarDocumentoDisponible(request.documentoDeIdentidad());
        // El rol y restaurante se fijan con los valores ya comprobados por esta capa.
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .documentoDeIdentidad(request.documentoDeIdentidad())
                .celular(request.celular())
                .correo(request.correo())
                .clave(passwordEncoder.encode(request.clave()))
                .rol(Rol.EMPLEADO)
                .build());
        empleadoRestauranteRepository.save(new EmpleadoRestaurante(usuario.getId(), request.idRestaurante()));
        return respuestaCuenta(usuario);
    }

    /** Busca la cuenta y compara el secreto recibido con BCrypt antes de emitir un token. */
    @Override
    public LoginResponse login(LoginRequest request) {
        // Se devuelve el mismo error ante correo inexistente o clave incorrecta para no revelar que cuenta existe.
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .filter(candidate -> passwordEncoder.matches(request.clave(), candidate.getClave()))
                .orElseThrow(() -> new SecurityException("Credenciales invalidas"));
        // La respuesta incluye identidad y duracion, mientras el token contiene claims firmados por el servicio.
        return new LoginResponse(tokenService.issue(usuario), usuario.getId(), usuario.getRol().name(),
                tokenService.getLifetimeSeconds());
    }

    /** Evita conversiones fuera del rango Integer antes de consultar la clave primaria del repositorio. */
    @Override
    public boolean esPropietarioValido(Integer idUsuario) {
        // Null, cero, negativos y valores no representables no pueden identificar a un propietario valido.
        if (idUsuario == null || idUsuario <= 0) return false;
        return usuarioRepository.existsByIdAndRol(idUsuario, Rol.PROPIETARIO);
    }

    /** Rechaza un correo ya registrado para mantener la unicidad del identificador de login. */
    private void validarCorreoDisponible(String correo) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
    }

    /** Rechaza un documento ya asociado a otra cuenta. */
    private void validarDocumentoDisponible(String documento) {
        if (usuarioRepository.existsByDocumentoDeIdentidad(documento)) {
            throw new IllegalArgumentException("El documento ya está registrado");
        }
    }

    /** Construye una vista de cuenta sin incluir el hash de la clave. */
    private CuentaResponse respuestaCuenta(Usuario usuario) {
        return new CuentaResponse(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                usuario.getCorreo(), usuario.getRol().name());
    }


}
