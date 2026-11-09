-- Esquema MySQL 8 para las entidades JPA actuales de Usuarios y Plazoleta.
-- La aplicacion unificada utiliza la base plazoleta_bd.
-- Ejecutar en una base nueva o vacia. Este script no borra ni migra tablas existentes.

CREATE DATABASE IF NOT EXISTS plazoleta_bd
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE plazoleta_bd;

-- Usuarios usa Integer para su ID; el rol se persiste como texto (EnumType.STRING).
CREATE TABLE IF NOT EXISTS usuario (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NULL,
    documento VARCHAR(255) NULL,
    celular VARCHAR(255) NULL,
    fecha_nacimiento DATETIME(6) NULL,
    correo VARCHAR(255) NOT NULL,
    clave VARCHAR(255) NULL,
    rol VARCHAR(255) NULL,
    -- Solo el administrador tiene valor; la restriccion UNIQUE impide un segundo administrador.
    admin_slot VARCHAR(255) NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_documento UNIQUE (documento),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT uq_usuario_admin_slot UNIQUE (admin_slot),
    INDEX idx_usuario_rol (rol)
) ENGINE = InnoDB;

-- id_propietario referencia logicamente al usuario propietario dentro de la misma aplicacion.
CREATE TABLE IF NOT EXISTS restaurantes (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    nit VARCHAR(20) NOT NULL,
    direccion VARCHAR(240) NOT NULL,
    telefono VARCHAR(13) NOT NULL,
    url_logo VARCHAR(500) NOT NULL,
    id_propietario INT NOT NULL,
    CONSTRAINT pk_restaurantes PRIMARY KEY (id),
    CONSTRAINT uq_restaurantes_nit UNIQUE (nit),
    INDEX idx_restaurantes_nombre (nombre),
    INDEX idx_restaurantes_propietario (id_propietario)
) ENGINE = InnoDB;

-- La categoria es texto en la entidad Plato; no se usa una tabla categoria.
CREATE TABLE IF NOT EXISTS platos (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    precio INT NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    url_imagen VARCHAR(500) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_restaurante INT NOT NULL,
    CONSTRAINT pk_platos PRIMARY KEY (id),
    CONSTRAINT chk_platos_precio CHECK (precio > 0),
    CONSTRAINT fk_platos_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurantes (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_platos_menu (id_restaurante, activo, categoria)
) ENGINE = InnoDB;

-- estado se almacena con el nombre del enum, por ejemplo PENDIENTE o LISTO.
-- id_cliente e id_empleado referencian usuarios de la misma aplicacion.
CREATE TABLE IF NOT EXISTS pedidos (
    id INT NOT NULL AUTO_INCREMENT,
    id_cliente INT NOT NULL,
    id_empleado INT NULL,
    id_restaurante INT NOT NULL,
    estado VARCHAR(24) NOT NULL,
    fecha_creacion TIMESTAMP(6) NOT NULL,
    total INT NOT NULL,
    pin_hash VARCHAR(60) NULL,
    CONSTRAINT pk_pedidos PRIMARY KEY (id),
    CONSTRAINT chk_pedidos_total CHECK (total >= 0),
    CONSTRAINT fk_pedidos_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurantes (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_pedidos_cliente_estado (id_cliente, estado),
    INDEX idx_pedidos_restaurante_estado (id_restaurante, estado)
) ENGINE = InnoDB;

-- precio_unitario conserva el precio historico aunque el plato cambie despues.
CREATE TABLE IF NOT EXISTS pedido_detalles (
    id INT NOT NULL AUTO_INCREMENT,
    id_pedido INT NOT NULL,
    id_plato INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario INT NOT NULL,
    CONSTRAINT pk_pedido_detalles PRIMARY KEY (id),
    CONSTRAINT uq_pedido_detalles_pedido_plato UNIQUE (id_pedido, id_plato),
    CONSTRAINT chk_pedido_detalles_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_pedido_detalles_precio CHECK (precio_unitario > 0),
    CONSTRAINT fk_pedido_detalles_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedidos (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_pedido_detalles_plato
        FOREIGN KEY (id_plato) REFERENCES platos (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_pedido_detalles_plato (id_plato)
) ENGINE = InnoDB;

-- Vincula cada empleado con su restaurante.
CREATE TABLE IF NOT EXISTS empleado_restaurante (
    id_usuario INT NOT NULL,
    id_restaurante INT NOT NULL,
    CONSTRAINT pk_empleado_restaurante PRIMARY KEY (id_usuario),
    CONSTRAINT fk_empleado_restaurante_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurantes (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Trazabilidad de HU17: un registro por cada cambio de estado de un pedido.
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

-- No hay tablas rol ni categoria: el codigo utiliza un enum de roles y categoria como texto.