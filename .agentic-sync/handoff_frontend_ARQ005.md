# 🖥️ Handoff Frontend — ARQ-005 (US-005 Core Deploy Pipeline)

## 1. Metadatos y SSOT
- **Iteración:** Remediación Arquitectónica Post-Auditoría US-005
- **Rama Git:** `sprint-6`
- **Hallazgos Origen:** `audit_arquitectura_US005.md`

## 2. Acciones Requeridas

> [!NOTE]
> **CERO ACCIONES PARA FRONTEND.**
>
> La remediación ARQ-005 es un refactor puramente backend (creación de Puertos y Adaptadores para desacoplar servicios de aplicación de entidades JPA y Camunda Model API).
>
> Los contratos REST del endpoint `/api/v1/design/processes/*` permanecen **idénticos**:
> - Los request/response bodies no cambian.
> - Los HTTP status codes no cambian.
> - Las rutas no cambian.
>
> El componente `BpmnDesigner.vue` no requiere modificaciones.

## 3. Motivo de No-Acción

La remediación reorganiza las capas internas del backend (Application → Infrastructure) sin alterar la interfaz pública de la API REST. El Frontend consume exclusivamente los contratos HTTP, que permanecen inalterados.

Este handoff se emite únicamente con fines de **trazabilidad y comunicación** para que el equipo Frontend esté informado del refactor en curso.
