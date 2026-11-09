package com.plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para entregar un pedido (HU15).
 * @param pin PIN de seguridad de seis dígitos que recibió el cliente por SMS
 */
public record EntregaPedidoRequest(
        @NotBlank @Pattern(regexp = "\\d{6}", message = "El PIN debe tener 6 digitos") String pin) {
}
