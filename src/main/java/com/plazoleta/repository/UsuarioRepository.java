package com.plazoleta.repository;

import com.plazoleta.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Consultas persistentes de usuarios; Spring Data deriva SQL a partir de los nombres de metodo. */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    /** Comprueba la unicidad del correo antes de intentar crear una cuenta. */
    boolean existsByCorreo(String correo);
    /** Comprueba la unicidad del documento de identidad. */
    boolean existsByDocumentoDeIdentidad(String documentoDeIdentidad);
    /** Recupera una cuenta por correo para verificar su clave durante login. */
    Optional<Usuario> findByCorreo(String correo);
    /** Comprueba si ya existe un usuario con el rol indicado. */
    boolean existsByRol(com.plazoleta.entity.Rol rol);
}
