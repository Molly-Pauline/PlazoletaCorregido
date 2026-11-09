package com.plazoleta.service;

import com.plazoleta.dto.request.PedidoRequest;
import com.plazoleta.dto.response.PedidoResponse;
import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.security.AuthenticatedUser;
import org.springframework.data.domain.Page;

/** Contrato de creación y consulta paginada de pedidos. */
public interface PedidoService {
    /** Crea un pedido para el cliente autenticado, aplicando reglas de activo y pertenencia de platos. */
    PedidoResponse crear(PedidoRequest request, AuthenticatedUser user);

    /** Lista pedidos del restaurante firmado en la identidad de un empleado por estado. */
    Page<PedidoResponse> listarPorEstado(EstadoPedido estado, int page, int size, AuthenticatedUser empleado);

    /** HU13: asigna el pedido al empleado y lo pasa a preparación. */
    PedidoResponse asignar(Integer idPedido, AuthenticatedUser empleado);

    /** HU14: marca el pedido como listo y envía el PIN al cliente por SMS. */
    PedidoResponse marcarListo(Integer idPedido, AuthenticatedUser empleado);

}
