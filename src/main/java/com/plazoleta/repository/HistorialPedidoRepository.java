package com.plazoleta.repository;

import com.plazoleta.entity.HistorialPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a los registros de trazabilidad de los pedidos. */
public interface HistorialPedidoRepository extends JpaRepository<HistorialPedido, Integer> {
    /** Recupera todos los cambios de un pedido; el servicio se encarga de ordenarlos. */
    List<HistorialPedido> findByIdPedido(Integer idPedido);
}
