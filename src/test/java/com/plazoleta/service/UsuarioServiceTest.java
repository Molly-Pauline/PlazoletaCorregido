package com.plazoleta.service;

import com.plazoleta.dto.request.ClienteRequest;
import com.plazoleta.dto.request.EmpleadoRequest;
import com.plazoleta.dto.request.LoginRequest;
import com.plazoleta.dto.request.RegistroRequest;
import com.plazoleta.dto.response.CuentaResponse;
import com.plazoleta.dto.response.LoginResponse;
import com.plazoleta.dto.response.RegistroResponse;
import com.plazoleta.entity.EmpleadoRestaurante;
import com.plazoleta.entity.Rol;
import com.plazoleta.entity.Usuario;
import com.plazoleta.exception.AdminAlreadyExistsException;
import com.plazoleta.repository.EmpleadoRestauranteRepository;
import com.plazoleta.repository.UsuarioRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.security.HmacTokenService;
import com.plazoleta.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas unitarias del servicio de registro de usuarios (HU1, HU5, HU6 y HU8) y del primer administrador. */
class UsuarioServiceTest {

    private static final String CLAVE = "ClaveSegura123";

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EmpleadoRestauranteRepository empleadoRestauranteRepository = mock(EmpleadoRestauranteRepository.class);
    private final HmacTokenService tokenService = mock(HmacTokenService.class);
    private final LocalOwnershipValidator ownershipValidator = mock(LocalOwnershipValidator.class);
    /** Codificador real con costo bajo: permite comprobar el cifrado BCrypt sin hacer lentas las pruebas. */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository,
            empleadoRestauranteRepository, passwordEncoder, tokenService, ownershipValidator);

    private final AuthenticatedUser propietario = new AuthenticatedUser(10, "PROPIETARIO");

    /** Simula la base de datos: al guardar, asigna el ID 1 y devuelve el mismo usuario. */
    @BeforeEach
    void simularGuardado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setId(1);
            return usuario;
        });
    }

    // ---------- HU1: propietario ----------

    @Test
    void registraPropietarioConRolAsignadoYClaveCifrada() {
        RegistroResponse respuesta = service.registrarUsuario(registro(Rol.PROPIETARIO, hace(30)));

        assertEquals(1, respuesta.getId());
        assertEquals("PROPIETARIO", respuesta.getRol());
        Usuario guardado = usuarioGuardado();
        assertNotEquals(CLAVE, guardado.getClave());
        assertTrue(passwordEncoder.matches(CLAVE, guardado.getClave()));
        assertNull(guardado.getAdminSlot());
    }

    @Test
    void rechazaPropietarioMenorDeEdad() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.registrarUsuario(registro(Rol.PROPIETARIO, hace(17))));

        assertEquals("El propietario debe tener al menos 18 años", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaPropietarioSinFechaDeNacimiento() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarUsuario(registro(Rol.PROPIETARIO, null)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarioRepository.existsByCorreo("persona@example.com")).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.registrarUsuario(registro(Rol.PROPIETARIO, hace(30))));

        assertEquals("El correo ya está registrado", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaDocumentoDuplicado() {
        when(usuarioRepository.existsByDocumentoDeIdentidad("1020304050")).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.registrarUsuario(registro(Rol.PROPIETARIO, hace(30))));

        assertEquals("El documento ya está registrado", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void elRegistroDePropietariosNoPermiteOtrosRoles() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarUsuario(registro(Rol.CLIENTE, hace(30))));
        assertThrows(IllegalArgumentException.class, () -> service.registrarUsuario(registro(Rol.EMPLEADO, hace(30))));

        verify(usuarioRepository, never()).save(any());
    }

    // ---------- Primer administrador ----------

    @Test
    void registraElPrimerAdministradorOcupandoElCupoUnico() {
        RegistroResponse respuesta = service.registrarUsuario(registro(Rol.ADMINISTRADOR, null));

        assertEquals("ADMINISTRADOR", respuesta.getRol());
        assertEquals("ADMINISTRADOR_UNICO", usuarioGuardado().getAdminSlot());
    }

    @Test
    void rechazaUnSegundoAdministrador() {
        when(usuarioRepository.existsByRol(Rol.ADMINISTRADOR)).thenReturn(true);

        assertThrows(AdminAlreadyExistsException.class,
                () -> service.registrarUsuario(registro(Rol.ADMINISTRADOR, null)));

        verify(usuarioRepository, never()).save(any());
    }

    // ---------- HU8: cliente ----------

    @Test
    void registraClienteConRolAsignadoPorElServidor() {
        CuentaResponse respuesta = service.registrarCliente(cliente());

        assertEquals("CLIENTE", respuesta.rol());
        assertEquals(Rol.CLIENTE, usuarioGuardado().getRol());
        assertTrue(passwordEncoder.matches(CLAVE, usuarioGuardado().getClave()));
    }

    @Test
    void rechazaClienteConCorreoDuplicado() {
        when(usuarioRepository.existsByCorreo("persona@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.registrarCliente(cliente()));

        verify(usuarioRepository, never()).save(any());
    }

    // ---------- HU6: empleado ----------

    @Test
    void registraEmpleadoYLoAsociaAlRestaurante() {
        when(ownershipValidator.propietarioEsDueno(7, 10, "Bearer token")).thenReturn(true);

        CuentaResponse respuesta = service.registrarEmpleado(empleado(Rol.EMPLEADO.getId()), propietario, "Bearer token");

        assertEquals("EMPLEADO", respuesta.rol());
        ArgumentCaptor<EmpleadoRestaurante> relacion = ArgumentCaptor.forClass(EmpleadoRestaurante.class);
        verify(empleadoRestauranteRepository).save(relacion.capture());
        assertEquals(1, relacion.getValue().getIdUsuario());
        assertEquals(7, relacion.getValue().getIdRestaurante());
    }

    @Test
    void rechazaEmpleadoConIdRolDistintoDeEmpleado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrarEmpleado(empleado(Rol.ADMINISTRADOR.getId()), propietario, "Bearer token"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaEmpleadoParaUnRestauranteAjeno() {
        when(ownershipValidator.propietarioEsDueno(7, 10, "Bearer token")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> service.registrarEmpleado(empleado(Rol.EMPLEADO.getId()), propietario, "Bearer token"));

        verify(usuarioRepository, never()).save(any());
        verify(empleadoRestauranteRepository, never()).save(any());
    }

    @Test
    void soloUnPropietarioPuedeCrearEmpleados() {
        assertThrows(SecurityException.class, () -> service.registrarEmpleado(empleado(Rol.EMPLEADO.getId()),
                new AuthenticatedUser(20, "CLIENTE"), "Bearer token"));

        verify(usuarioRepository, never()).save(any());
    }

    // ---------- HU5: login ----------

    @Test
    void loginCorrectoDevuelveTokenYRol() {
        Usuario usuario = Usuario.builder().id(3).correo("persona@example.com")
                .clave(passwordEncoder.encode(CLAVE)).rol(Rol.CLIENTE).build();
        when(usuarioRepository.findByCorreo("persona@example.com")).thenReturn(Optional.of(usuario));
        when(tokenService.issue(usuario)).thenReturn("jwt-de-prueba");
        when(tokenService.getLifetimeSeconds()).thenReturn(43200L);

        LoginResponse respuesta = service.login(new LoginRequest("persona@example.com", CLAVE));

        assertEquals("jwt-de-prueba", respuesta.token());
        assertEquals(3, respuesta.idUsuario());
        assertEquals("CLIENTE", respuesta.rol());
    }

    @Test
    void loginRechazaClaveIncorrectaYCorreoInexistenteConElMismoMensaje() {
        Usuario usuario = Usuario.builder().id(3).correo("persona@example.com")
                .clave(passwordEncoder.encode(CLAVE)).rol(Rol.CLIENTE).build();
        when(usuarioRepository.findByCorreo("persona@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo("nadie@example.com")).thenReturn(Optional.empty());

        SecurityException claveMala = assertThrows(SecurityException.class,
                () -> service.login(new LoginRequest("persona@example.com", "otraClave")));
        SecurityException sinCuenta = assertThrows(SecurityException.class,
                () -> service.login(new LoginRequest("nadie@example.com", CLAVE)));

        assertEquals(claveMala.getMessage(), sinCuenta.getMessage());
        verify(tokenService, never()).issue(any());
    }

    // ---------- Datos de prueba ----------

    private RegistroRequest registro(Rol rol, Date fechaNacimiento) {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Persona");
        request.setApellido("Prueba");
        request.setDocumentoDeIdentidad("1020304050");
        request.setCelular("+573001234567");
        request.setFechaDeNacimiento(fechaNacimiento);
        request.setCorreo("persona@example.com");
        request.setClave(CLAVE);
        request.setRol(rol);
        return request;
    }

    private ClienteRequest cliente() {
        return new ClienteRequest("Persona", "Prueba", "1020304050", "+573001234567", "persona@example.com", CLAVE);
    }

    private EmpleadoRequest empleado(int idRol) {
        return new EmpleadoRequest("Persona", "Prueba", "1020304050", "+573001234567",
                "persona@example.com", CLAVE, idRol, 7);
    }

    /** Fecha de nacimiento de alguien que cumplió los años indicados hace un mes. */
    private Date hace(int anios) {
        return Date.from(LocalDate.now().minusYears(anios).minusMonths(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Usuario usuarioGuardado() {
        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuario.capture());
        return usuario.getValue();
    }
}
