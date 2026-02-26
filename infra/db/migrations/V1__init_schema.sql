-- V1__init_schema.sql
-- Initial schema for iBPMS Core (generated for Flyway/Liquibase)

CREATE TABLE expediente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL,
    cliente_id BIGINT,
    CONSTRAINT fk_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tarea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediente_id BIGINT NOT NULL,
    descripcion TEXT,
    asignado_a VARCHAR(100),
    estado VARCHAR(20) NOT NULL,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    CONSTRAINT fk_expediente FOREIGN KEY (expediente_id) REFERENCES expediente(id)
);

-- Add any additional tables required by the core domain here.
