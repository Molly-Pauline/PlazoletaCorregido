package com.plazoleta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Registro de trazabilidad de HU17: guarda cada cambio de estado de un pedido. */
@Entity
@Table(name = "pedido_historial")
public class HistorialPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    /** Estado previo; es nulo en el primer registro, cuando se crea el pedido. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 24)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 24)
    private EstadoPedido estadoNuevo;

    /** Cliente o empleado que hizo el cambio. */
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Constructor vacío reservado para JPA. */
    protected HistorialPedido() {
    }

    public HistorialPedido(Integer idPedido, EstadoPedido estadoAnterior, EstadoPedido estadoNuevo,
                           Integer idUsuario, LocalDateTime fecha) {
        this.idPedido = idPedido;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.idUsuario = idUsuario;
        this.fecha = fecha;
    }

    public Integer getId() { return id; }
    public Integer getIdPedido() { return idPedido; }
    public EstadoPedido getEstadoAnterior() { return estadoAnterior; }
    public EstadoPedido getEstadoNuevo() { return estadoNuevo; }
    public Integer getIdUsuario() { return idUsuario; }
    public LocalDateTime getFecha() { return fecha; }
}
