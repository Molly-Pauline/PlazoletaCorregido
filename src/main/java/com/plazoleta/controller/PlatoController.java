package com.plazoleta.controller;

import com.plazoleta.entity.Plato;
import com.plazoleta.dto.response.PlatoApiResponse;
import com.plazoleta.dto.request.PlatoRequest;
import com.plazoleta.dto.request.PlatoEstadoRequest;
import com.plazoleta.dto.response.PlatoResponse;
import com.plazoleta.dto.request.PlatoUpdateRequest;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PlatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adapta las operaciones HTTP de platos a los servicios de negocio. */
@RestController
@RequestMapping("/platos")
@Tag(name = "Platos", description = "Gestión de platos por el propietario (HU3, HU4 y HU7)")
@SecurityRequirement(name = "bearerAuth")
public class PlatoController {

    /** Servicio que aplica pertenencia del propietario y reglas HU3, HU4 y HU7. */
    private final PlatoService platoService;

    /** Recibe el servicio que ejecuta las reglas de platos. */
    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    /** Crea un plato del restaurante autorizado y responde con HTTP 201.
     * @param request datos validados del nuevo plato y restaurante destino
     * @param user principal autenticado cuyo rol y propiedad confirma el servicio
     * @return respuesta con el DTO público del plato creado
     */
    @Operation(summary = "HU3 - Crear plato", description = "Requiere rol PROPIETARIO del restaurante.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatoApiResponse crear(@Valid @RequestBody PlatoRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        // @Valid hace cumplir las restricciones de PlatoRequest antes de iniciar la operación de negocio.
        Plato plato = platoService.crear(request, user);
        // La respuesta usa un DTO para no exponer directamente la entidad JPA.
        return new PlatoApiResponse(true, "Plato creado", PlatoResponse.from(plato));
    }

    /** Modifica únicamente los campos aceptados por PlatoUpdateRequest (HU4).
     * @param idPlato plato existente que se actualizará
     * @param request nuevos precio y descripción
     * @param user propietario autenticado que debe controlar el restaurante del plato
     * @return respuesta con el plato actualizado
     */
    @Operation(summary = "HU4 - Modificar plato", description = "Requiere rol PROPIETARIO. Solo cambia precio y descripción.")
    @PutMapping("/{idPlato}")
    public PlatoApiResponse modificar(@PathVariable Integer idPlato, @Valid @RequestBody PlatoUpdateRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        // El principal contiene la identidad autenticada que el servicio compara con el dueño del restaurante.
        Plato plato = platoService.modificar(idPlato, request, user);
        return new PlatoApiResponse(true, "Plato modificado", PlatoResponse.from(plato));
    }

    /** Cambia el indicador de disponibilidad del plato, sin alterar sus demás datos (HU7).
     * @param idPlato plato existente cuya disponibilidad se modifica
     * @param request nuevo valor activo/inactivo
     * @param user propietario autenticado que debe controlar el restaurante del plato
     * @return respuesta con el estado actualizado del plato
     */
    @Operation(summary = "HU7 - Habilitar o deshabilitar plato", description = "Requiere rol PROPIETARIO.")
    @PatchMapping("/{idPlato}/estado")
    public PlatoApiResponse cambiarEstado(@PathVariable Integer idPlato, @Valid @RequestBody PlatoEstadoRequest request,
                                          @AuthenticationPrincipal AuthenticatedUser user) {
        Plato plato = platoService.cambiarEstado(idPlato, request, user);
        return new PlatoApiResponse(true, "Estado del plato actualizado", PlatoResponse.from(plato));
    }
}
