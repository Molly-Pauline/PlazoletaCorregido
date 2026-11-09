package com.plazoleta.service;

/** Envía mensajes SMS; permite cambiar de proveedor sin tocar las reglas de los pedidos. */
public interface NotificacionSmsService {
    /** Envía el mensaje al celular indicado. */
    void enviar(String celular, String mensaje);
}
