package com.plazoleta.repository;

import com.plazoleta.domain.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    boolean existsByNit(String nit);
}