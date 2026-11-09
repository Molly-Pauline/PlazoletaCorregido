package com.plazoleta;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.estado.PedidoStates;
import com.plazoleta.security.HmacTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Evidencia del patrón Singleton: Spring entrega siempre la misma instancia de cada bean. */
@SpringBootTest
class SingletonBeansTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void springCreaUnaSolaInstanciaDeCadaBean() {
        assertSame(contexto.getBean(SecretKey.class), contexto.getBean(SecretKey.class));
        assertSame(contexto.getBean(PasswordEncoder.class), contexto.getBean(PasswordEncoder.class));
        assertSame(contexto.getBean(HmacTokenService.class), contexto.getBean(HmacTokenService.class));
        assertTrue(contexto.isSingleton("hmacTokenService"));
    }

    @Test
    void cadaEstadoDelPedidoTieneUnaUnicaInstanciaCompartida() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            assertSame(PedidoStates.de(estado), PedidoStates.de(estado));
        }
    }
}
