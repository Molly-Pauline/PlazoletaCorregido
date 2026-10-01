package com.plazoleta.dto;

public record RestauranteApiResponse(boolean success, String message, RestauranteResponse restaurante) {
}