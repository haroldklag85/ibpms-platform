# Solicitud de Aprobación de Plan: BACKEND - US-030 (Monitoreo BPMN)

## 🎯 Objetivo
Implementar los endpoints de telemetría y monitoreo (BAM) del motor BPMN para listar instancias activas, completadas e incidentes, extrayendo métricas reales de Camunda mediante `HistoryService` y `RuntimeService`.

## 🏗️ Arquitectura y Diseño (Hexagonal)
Se respetará estrictamente la arquitectura hexagonal y la regla de **ZERO MOCKS**, conectando directamente a la API integrada de Camunda.

### 1. Dominio (Domain)
- **Modelos:** `ProcessInstanceMetric` (id, processDefinitionKey, state, startTime, endTime), `ProcessIncidentMetric` (id, processInstanceId, incidentType, incidentMessage, createTime).
- **Puertos de Entrada (Inbound Ports / Use Cases):**
  - `GetProcessInstancesUseCase`: Interfaz para obtener instancias de proceso paginadas por estado (ACTIVE, COMPLETED, SUSPENDED).
  - `GetProcessIncidentsUseCase`: Interfaz para listar incidentes del motor.
- **Puertos de Salida (Outbound Ports):**
  - `ProcessTelemetryPort`: Interfaz para consultar instancias y sus estados.
  - `ProcessIncidentPort`: Interfaz para consultar incidentes activos o históricos.

### 2. Casos de Uso (Application Service)
- `GetProcessInstancesService`: Implementa `GetProcessInstancesUseCase`, llamando a `ProcessTelemetryPort`.
- `GetProcessIncidentsService`: Implementa `GetProcessIncidentsUseCase`, llamando a `ProcessIncidentPort`.

### 3. Adaptadores (Adapters)
- **Inbound Adapters (REST Controllers):**
  - `BpmTelemetryController`: Expone endpoints REST, e.g., `GET /api/v1/bpm/telemetry/instances` y `GET /api/v1/bpm/telemetry/incidents`.
- **Outbound Adapters (Camunda Engine):**
  - `CamundaTelemetryAdapter`: Implementa `ProcessTelemetryPort` usando `HistoryService` (para historial y completadas) y `RuntimeService` (para activas) de Camunda.
  - `CamundaIncidentAdapter`: Implementa `ProcessIncidentPort` usando `RuntimeService` y `HistoryService` de Camunda.

## 🚦 Secuencia de Ejecución
1. Crear clases de Dominio (Modelos de métricas) y Puertos (Inbound y Outbound).
2. Implementar Casos de Uso (`GetProcessInstancesService`, `GetProcessIncidentsService`).
3. Desarrollar Adaptadores Outbound (`CamundaTelemetryAdapter`, `CamundaIncidentAdapter`) usando la API de Java de Camunda.
4. Desarrollar el Controlador REST Inbound (`BpmTelemetryController`) y DTOs requeridos.
5. Ejecutar la auto-compilación estricta y prueba de arranque según la habilidad `backend_sre_compilation_audit`.
6. Registrar el impacto en `CHANGELOG_NO_TECNICO.md`.

---
**Nota para el Arquitecto:** Plan listo. Esperando aprobación formal para pasar a modo `EXECUTION`.
