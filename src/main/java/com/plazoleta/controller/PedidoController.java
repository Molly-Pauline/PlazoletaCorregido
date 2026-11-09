package com.plazoleta.controller;

import com.plazoleta.dto.request.EntregaPedidoRequest;
import com.plazoleta.dto.request.PedidoRequest;
import com.plazoleta.dto.response.PedidoApiResponse;
import com.plazoleta.dto.response.PedidoResponse;
import com.plazoleta.dto.response.TrazabilidadResponse;
import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expone los endpoints HTTP de creación y consulta de pedidos. */
@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Creación, ciclo de vida y trazabilidad de pedidos (HU11 a HU17)")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    /** Servicio donde se aplican las reglas de negocio y autorización por recurso. */
    private final PedidoService pedidoService;

    /** Inyecta el servicio de pedidos. */
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Recibe un pedido validado, toma al cliente del principal autenticado y responde con estado HTTP 201.
     * @param request restaurante y líneas de platos solicitadas
     * @param user cliente identificado por el JWT, nunca por un ID recibido en el cuerpo
     * @return envoltorio con el pedido creado y su estado inicial
     */
    @Operation(summary = "HU11 - Crear pedido", description = "Requiere rol CLIENTE. Queda PENDIENTE; solo se permite un pedido en curso por cliente.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoApiResponse crear(@Valid @RequestBody PedidoRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        // @Valid ejecuta las restricciones declaradas en el DTO antes de llamar al servicio.
        return new PedidoApiResponse(true, "Pedido creado", pedidoService.crear(request, user));
    }

    /** Consulta una página de pedidos por estado; el servicio deriva el restaurante del empleado autenticado.
     * @param estado etapa del ciclo de vida que se desea consultar
     * @param page índice de página, empezando en cero
     * @param size cantidad solicitada, acotada por el servicio
     * @param user empleado autenticado con el restaurante firmado en el JWT
     * @return página de pedidos del restaurante asociado a ese empleado
     */
    @Operation(summary = "HU12 - Listar pedidos por estado", description = "Requiere rol EMPLEADO. Solo muestra pedidos de su restaurante.")
    @GetMapping
    public Page<PedidoResponse> listar(@RequestParam EstadoPedido estado,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return pedidoService.listarPorEstado(estado, page, size, user);
    }

    /** HU13: el empleado se asigna el pedido y este pasa a preparación. */
    @Operation(summary = "HU13 - Asignarse el pedido", description = "Requiere rol EMPLEADO. Pasa de PENDIENTE a EN_PREPARACION.")
    @PatchMapping("/{idPedido}/asignar")
    public PedidoApiResponse asignar(@PathVariable Integer idPedido, @AuthenticationPrincipal AuthenticatedUser user) {
        return new PedidoApiResponse(true, "Pedido asignado", pedidoService.asignar(idPedido, user));
    }

    /** HU14: el pedido pasa a listo y el cliente recibe su PIN por SMS. */
    @Operation(summary = "HU14 - Marcar pedido listo", description = "Requiere rol EMPLEADO. Pasa a LISTO y envía el PIN al cliente por SMS.")
    @PatchMapping("/{idPedido}/listo")
    public PedidoApiResponse marcarListo(@PathVariable Integer idPedido, @AuthenticationPrincipal AuthenticatedUser user) {
        return new PedidoApiResponse(true, "Pedido listo; PIN enviado al cliente", pedidoService.marcarListo(idPedido, user));
    }

    /** HU15: el empleado digita el PIN para cerrar el pedido. */
    @Operation(summary = "HU15 - Entregar pedido", description = "Requiere rol EMPLEADO. Exige estado LISTO y el PIN correcto.")
    @PatchMapping("/{idPedido}/entregar")
    public PedidoApiResponse entregar(@PathVariable Integer idPedido, @Valid @RequestBody EntregaPedidoRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return new PedidoApiResponse(true, "Pedido entregado", pedidoService.entregar(idPedido, request.pin(), user));
    }

    /** HU16: el cliente cancela su pedido mientras siga pendiente. */
    @Operation(summary = "HU16 - Cancelar pedido", description = "Requiere rol CLIENTE dueño del pedido. Solo en estado PENDIENTE.")
    @PatchMapping("/{idPedido}/cancelar")
    public PedidoApiResponse cancelar(@PathVariable Integer idPedido, @AuthenticationPrincipal AuthenticatedUser user) {
        return new PedidoApiResponse(true, "Pedido cancelado", pedidoService.cancelar(idPedido, user));
    }

    /** HU17: el cliente consulta los cambios de estado de su pedido. */
    @Operation(summary = "HU17 - Consultar trazabilidad", description = "Requiere rol CLIENTE dueño del pedido. Cambios de estado en orden cronológico.")
    @GetMapping("/{idPedido}/trazabilidad")
    public List<TrazabilidadResponse> consultarTrazabilidad(@PathVariable Integer idPedido,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        return pedidoService.consultarTrazabilidad(idPedido, user);
    }
}
