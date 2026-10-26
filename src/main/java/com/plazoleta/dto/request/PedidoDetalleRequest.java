package com.plazoleta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Define una línea del pedido y evita identificadores o cantidades nulos/no positivos. */
public record PedidoDetalleRequest(
	// El ID se resuelve en la base para obtener el plato y su precio real.
	@NotNull @Positive Long idPlato,
	// La cantidad debe ser positiva y determina el subtotal de esta línea.
	@NotNull @Positive Integer cantidad) {
}