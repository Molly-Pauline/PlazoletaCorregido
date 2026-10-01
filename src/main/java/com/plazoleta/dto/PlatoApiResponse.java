package com.plazoleta.dto;

public record PlatoApiResponse(boolean success, String message, PlatoResponse plato) {
}