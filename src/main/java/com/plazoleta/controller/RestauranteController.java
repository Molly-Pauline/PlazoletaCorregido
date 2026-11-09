package com.plazoleta.controller;

import com.plazoleta.entity.Restaurante;
import com.plazoleta.exception.DomainException;
import com.plazoleta.dto.response.RestauranteApiResponse;
import com.plazoleta.dto.request.RestauranteRequest;
import com.plazoleta.dto.response.RestauranteResponse;
import com.plazoleta.dto.response.RestauranteListadoResponse;
import com.plazoleta.dto.response.PlatoResponse;
import com.plazoleta.service.RestauranteService;
import com.plazoleta.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.plazoleta.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Publica operaciones de restaurantes y consulta del menú de platos. */
@RestController
@RequestMapping("/restaurantes")
public class RestauranteController {

    /** Reglas de creación, listado y comprobación de propiedad de restaurantes. */
    private final RestauranteService restauranteService;
    /** Consulta el menú de platos que pertenece a un restaurante. */
    private final PlatoService platoService;

    /** Inyecta los servicios de dominio y el verificador interno. */
    public RestauranteController(RestauranteService restauranteService, PlatoService platoService) {
        this.restauranteService = restauranteService;
        this.platoService = platoService;
    }

    /** Registra un restaurante después de validar datos y propietario en el servicio.
     * @param request datos del restaurante y referencia del propietario por validar
     * @return envoltorio de éxito con los datos públicos del restaurante creado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestauranteApiResponse crear(@Valid @RequestBody RestauranteRequest request) {
        Restaurante restaurante = restauranteService.crear(request);
        // Se transforma la entidad persistida a una respuesta estable para el contrato HTTP.
        return new RestauranteApiResponse(true, "Restaurante creado", RestauranteResponse.from(restaurante));
    }

    /** Lista restaurantes por páginas; la ordenación ascendente se define en el servicio.
     * @param page índice de página, empezando en cero
     * @param size cantidad solicitada, limitada por el servicio
     * @return página con la proyección pública reducida de cada restaurante
     */
    @GetMapping
    public Page<RestauranteListadoResponse> listar(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return restauranteService.listar(page, size);
    }

    /** Devuelve el menú paginado de un restaurante, con filtro opcional por categoría.
     * @param idRestaurante restaurante cuyo menú se consulta
     * @param categoria filtro opcional que se compara sin distinguir mayúsculas
     * @param page índice de página, empezando en cero
     * @param size cantidad de platos por página
     * @return página de platos activos que cumplen el filtro
     */
    @GetMapping("/{idRestaurante}/platos")
    public Page<PlatoResponse> listarMenu(@PathVariable Integer idRestaurante,
                                         @RequestParam(required = false) String categoria,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return platoService.listarMenu(idRestaurante, categoria, page, size);
    }

    /** Busca en el menú los platos cuyo nombre empieza por el texto indicado.
     * @param idRestaurante restaurante cuyo menú se consulta
     * @param nombre texto inicial del nombre, sin distinguir mayúsculas ni tildes
     * @return platos activos que coinciden, ordenados por nombre
     */
    @GetMapping("/{idRestaurante}/platos/buscar")
    public List<PlatoResponse> buscarEnMenu(@PathVariable Integer idRestaurante,
                                            @RequestParam(required = false) String nombre) {
        return platoService.buscarEnMenu(idRestaurante, nombre);
    }

}
