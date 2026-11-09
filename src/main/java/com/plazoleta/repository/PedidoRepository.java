package com.plazoleta.repository;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.Optional;

/** Consultas JPA necesarias para impedir pedidos activos duplicados y listar trabajo por restaurante. */
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    /** Comprueba si el cliente ya tiene alguno de los estados que bloquean otro pedido. */
    boolean existsByIdClienteAndEstadoIn(Integer idCliente, Collection<EstadoPedido> estados);

    /** Filtra por ID del restaurante relacionado y estado, devolviendo resultados paginados. */
    Page<Pedido> findByRestaurante_IdAndEstado(Integer idRestaurante, EstadoPedido estado, Pageable pageable);

    /** Bloquea la fila durante la transacción para que dos solicitudes no cambien el estado a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> findByIdParaActualizar(@Param("id") Integer id);
}
