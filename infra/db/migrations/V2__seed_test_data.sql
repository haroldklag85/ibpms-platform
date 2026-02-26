-- V2__seed_test_data.sql
-- Inserción de 5 Clientes, 5 Expedientes y 5 Tareas para el Sprint 2

-- 1. Insertar Clientes
INSERT INTO cliente (id, nombre, tipo) VALUES 
(1, 'Empresa Alpha S.A.', 'CORPORATIVO'),
(2, 'Juan Pérez', 'INDIVIDUAL'),
(3, 'Global Logistics LLC', 'CORPORATIVO'),
(4, 'María García', 'INDIVIDUAL'),
(5, 'Tech Innovators Inc.', 'CORPORATIVO');

-- 2. Insertar Expedientes
INSERT INTO expediente (id, numero, descripcion, estado, cliente_id) VALUES 
(1, 'EXP-2026-0001', 'Solicitud de Crédito Corporativo', 'ACTIVE', 1),
(2, 'EXP-2026-0002', 'Reclamo por Cobro Indebido', 'ACTIVE', 2),
(3, 'EXP-2026-0003', 'Renovación de Línea de Crédito', 'ACTIVE', 3),
(4, 'EXP-2026-0004', 'Apertura de Cuenta Corriente', 'ACTIVE', 4),
(5, 'EXP-2026-0005', 'Auditoría de Cumplimiento', 'ACTIVE', 5);

-- 3. Insertar Tareas
INSERT INTO tarea (id, expediente_id, descripcion, asignado_a, estado, fecha_inicio, fecha_fin) VALUES 
(1, 1, 'Revisar Solicitud', 'analista_1', 'PENDING', CURRENT_TIMESTAMP, NULL),
(2, 2, 'Analizar Evidencias', 'soporte_1', 'PENDING', CURRENT_TIMESTAMP, NULL),
(3, 3, 'Aprobar Crédito', 'gerente_1', 'PENDING', CURRENT_TIMESTAMP, NULL),
(4, 4, 'Verificar Identidad (KYC)', NULL, 'PENDING', CURRENT_TIMESTAMP, NULL),
(5, 5, 'Recolectar Documentos', 'auditor_1', 'PENDING', CURRENT_TIMESTAMP, NULL);
