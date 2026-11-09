package com.plazoleta.service;

import com.plazoleta.dto.response.RegistroResponse;
import com.plazoleta.dto.response.CuentaResponse;
import com.plazoleta.dto.response.LoginResponse;
import com.plazoleta.dto.request.ClienteRequest;
import com.plazoleta.dto.request.EmpleadoRequest;
import com.plazoleta.dto.request.LoginRequest;
import com.plazoleta.dto.request.RegistroRequest;
import com.plazoleta.security.AuthenticatedUser;

/** Contrato de casos de uso de cuentas, separado de la exposicion HTTP y de la persistencia. */
public interface UsuarioService {
    /** Registra un propietario validando unicidad, edad minima y credenciales. */
    RegistroResponse registrarUsuario(RegistroRequest registroRequest);

    /** Registra un cliente y asigna el rol desde el servidor, no desde la solicitud. */
    CuentaResponse registrarCliente(ClienteRequest request);

    /** Registra un empleado solo si el principal es propietario y posee el restaurante. */
    CuentaResponse registrarEmpleado(EmpleadoRequest request, AuthenticatedUser propietario, String bearerToken);

    /** Valida credenciales y emite el token de acceso compartido. */
    LoginResponse login(LoginRequest request);

    /** Consulta internamente si un identificador pertenece a un propietario valido. */
    boolean esPropietarioValido(Integer idUsuario);
}
