-- liquibase formatted sql

-- changeset antigravity:33-create-kanban-schema
-- comment: Ecosistema relacional para el tablero Kanban. Requerimiento: Kanban opera sin Camunda (J-04: Resiliencia/Degradación)

CREATE TABLE ibpms_kanban_board (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    wip_limit INTEGER,
    order_index INTEGER NOT NULL
);

CREATE TABLE ibpms_kanban_item (
    id VARCHAR(50) PRIMARY KEY,
    board_id VARCHAR(50) NOT NULL REFERENCES ibpms_kanban_board(id),
    title VARCHAR(200) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    assignee VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    sla_hours INTEGER
);
