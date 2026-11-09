package com.plazoleta.repository;

import com.plazoleta.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso JPA a platos y consulta del menú filtrado por restaurante, disponibilidad y categoría. */
public interface PlatoRepository extends JpaRepository<Plato, Integer> {
	// Si categoria es null se omite ese filtro; lower() hace insensible a mayúsculas la comparación.
	// La paginación se traduce a límites y conteo en base de datos, sin cargar todo el menú en memoria.
    @Query("select p from Plato p where p.restaurante.id = :idRestaurante and p.activo = true " +
	    "and (:categoria is null or lower(p.categoria) = lower(:categoria))")
    Page<Plato> listarMenu(@Param("idRestaurante") Integer idRestaurante,
			   @Param("categoria") String categoria, Pageable pageable);

    /** Devuelve todos los platos visibles de un restaurante para buscarlos por nombre. */
    List<Plato> findByRestaurante_IdAndActivoTrue(Integer idRestaurante);
}

