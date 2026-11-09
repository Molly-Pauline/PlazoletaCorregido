package com.plazoleta.service;

import com.plazoleta.entity.Rol;
import com.plazoleta.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class LocalUsuarioValidationAdapter implements UsuarioValidationPort {
    private final UsuarioRepository repository;
    public LocalUsuarioValidationAdapter(UsuarioRepository repository) { this.repository = repository; }
    @Override public boolean existePropietarioValido(Integer idPropietario) {
        return idPropietario != null && repository.findById(idPropietario)
                .map(usuario -> usuario.getRol() == Rol.PROPIETARIO).orElse(false);
    }
}
