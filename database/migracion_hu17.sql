-- Solo para una base plazoleta_bd creada antes de HU13-HU17. Haz un respaldo antes de ejecutarlo.
-- Con ddl-auto=update, Hibernate crea estos elementos al arrancar: este script es la alternativa manual.
-- Si la columna ya existe, MySQL rechaza el ALTER TABLE: en ese caso omite esa instruccion.
USE plazoleta_bd;

ALTER TABLE pedidos ADD COLUMN pin_hash VARCHAR(60) NULL;

CREATE TABLE IF NOT EXISTS pedido_historial (
    id INT NOT NULL AUTO_INCREMENT,
    id_pedido INT NOT NULL,
    estado_anterior VARCHAR(24) NULL,
    estado_nuevo VARCHAR(24) NOT NULL,
    id_usuario INT NOT NULL,
    fecha TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_pedido_historial PRIMARY KEY (id),
    CONSTRAINT fk_pedido_historial_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedidos (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    INDEX idx_pedido_historial_pedido (id_pedido)
) ENGINE = InnoDB;
