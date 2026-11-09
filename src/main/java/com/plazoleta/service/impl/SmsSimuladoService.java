package com.plazoleta.service.impl;

import com.plazoleta.service.NotificacionSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simulador de SMS para desarrollo y demostración: escribe el mensaje en la consola en lugar de enviarlo.
 * El mensaje incluye el PIN, así que debe reemplazarse por un proveedor real antes de ir a producción.
 */
@Service
public class SmsSimuladoService implements NotificacionSmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsSimuladoService.class);

    @Override
    public void enviar(String celular, String mensaje) {
        LOG.info("[SMS SIMULADO] Para {}: {}", celular, mensaje);
    }
}
