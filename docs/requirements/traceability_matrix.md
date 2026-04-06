# Matriz de Trazabilidad US ↔ Pantalla — iBPMS V1
**Última actualización:** 2026-04-05

> [!NOTE]
> Este documento mapea bidireccionalmente cada Pantalla con las US que la generan, y cada US con las Pantallas que afecta. Permite detectar pantallas huérfanas (sin US) y US sin representación visual.

---

## 1. Pantalla → US (¿Qué US alimentan esta pantalla?)

| Pantalla | Nombre | US que la generan | Estado |
|---|---|---|---|
| **Pantalla 1** | Workdesk (Bandeja de Tareas) | US-001, US-002 | En desarrollo |
| **Pantalla 1B** | Bandeja Avanzada (Docketing SAC) | US-011 | No iniciada |
| **Pantalla 2** | Ejecución de Formulario | US-029 | Refinada |
| **Pantalla 2C** | Copiloto IA de Correos | US-012, US-013, US-014 | No iniciada |
| **Pantalla 3** | Tablero Kanban | US-008 | No iniciada |
| **Pantalla 4** | Service Delivery CRM | US-024, US-025, US-040, US-041 | No iniciada |
| **Pantalla 5** | Dashboard BAM | US-009, US-018 | No iniciada |
| **Pantalla 6** | Modelador BPMN | US-005, US-027 | Refinada (US-005) |
| **Pantalla 7** | iForm IDE (Constructor de Formularios) | US-003, US-028 | En desarrollo (US-003) |
| **Pantalla 7.B** | Formulario Genérico Base | US-039 | No iniciada |
| **Pantalla 8** | Diseñador de Plantillas WBS | US-006 | No iniciada |
| **Pantalla 10** | Proyecto Ágil (Sprints/Kanban) | US-030 | No iniciada |
| **Pantalla 10.B** | Proyecto Tradicional (Gantt) | US-031 | No iniciada |
| **Pantalla 13** | Developer Portal | US-033, US-042 | No iniciada |
| **Pantalla 14** | RBAC Manager | US-036, US-038, US-048 | En desarrollo (US-036) |
| **Pantalla 15** | Settings de Gobernanza IA y Dominio | US-044, US-045, US-049 | No iniciada |
| **Pantalla 15.A** | Settings de Rendimiento e Integraciones | US-046 | No iniciada |
| **Pantalla 16** | Gestión de Buzones SAC | US-016, US-037 | No iniciada |
| **Pantalla 19** | Configuración SLA | US-043 | No iniciada |
| **Portal B2C** | Portal de Clientes Externos | US-026, US-050 | No iniciada |
| **Command Center** | Antigravity Command Center (Agentes IA) | US-053 | No iniciada |

---

## 2. US → Pantalla (¿Qué pantallas afecta cada US?)

| US | Nombre | Pantalla(s) |
|----|--------|------------|
| US-000 | Resiliencia Integrada y PII | Transversal (todas) |
| US-001 | Workdesk Tareas | Pantalla 1 |
| US-002 | Claim Task | Pantalla 1 |
| US-003 | iForm IDE | Pantalla 7 |
| US-004 | Webhook O365 | — (Backend only) |
| US-005 | Desplegar BPMN | Pantalla 6 |
| US-006 | WBS Plantilla Proyecto | Pantalla 8 |
| US-007 | Generador DMN | Pantalla 6 |
| US-008 | Kanban | Pantalla 3 |
| US-009 | BAM Dashboard | Pantalla 5 |
| US-010 | Generador PDF | Pantalla 2 (descarga) |
| US-011 | Docketing SAC | Pantalla 1B |
| US-012 | Propuesta Respuesta Correo | Pantalla 2C |
| US-013 | Enriquecimiento CRM | Pantalla 2C |
| US-014 | Sugerencia Acciones | Pantalla 2C |
| US-015 | MLOps Feedback | — (Backend batch) |
| US-016 | Multi-Buzón | Pantalla 16 |
| US-017 | CQRS / Event Sourcing | — (Backend only) |
| US-018 | Métricas de Desempeño | Pantalla 5 |
| US-019 | Conectividad CRM | — (Backend only) |
| US-020 | Sincronización Flexible | — (Backend only) |
| US-021 | Mapeo de Variables | Pantalla 15 |
| US-022 | Confirm-to-Create (Plan A) | — (Correo → Backend) |
| US-023 | Correlación del Hilo | — (Backend only) |
| US-024 | Creación Global (Plan B) | Pantalla 4 |
| US-025 | Cards Dinámicas por Rol | Pantalla 4 |
| US-026 | Portal Cliente Externo | Portal B2C |
| US-027 | Copiloto IA BPMN | Pantalla 6 |
| US-028 | Simulador Zod | Pantalla 7 |
| US-029 | Envío Formulario | Pantalla 2 |
| US-030 | Proyecto Ágil | Pantalla 10 |
| US-031 | Proyecto Tradicional (Gantt) | Pantalla 10.B |
| US-032 | RAG / Cognitive BPMN | — (Backend + BPMN) |
| US-033 | Catálogo API | Pantalla 13 |
| US-034 | RabbitMQ | — (Backend only) |
| US-035 | SharePoint / SGDEA | — (Backend only) |
| US-036 | RBAC | Pantalla 14 |
| US-037 | CRUD Buzones | Pantalla 16 |
| US-038 | Multi-Rol EntraID | Pantalla 14 |
| US-039 | Formulario Genérico Base | Pantalla 7.B |
| US-040 | Embudo Intake | Pantalla 4 |
| US-041 | Vista 360 Cliente | Pantalla 4 |
| US-042 | DevPortal API Keys | Pantalla 13 |
| US-043 | Config SLA | Pantalla 19 |
| US-044 | Gobernanza IA | Pantalla 15 |
| US-045 | Restricciones Dominio | Pantalla 15 |
| US-046 | Gobernanza Rendimiento | Pantalla 15.A |
| US-048 | IdP Interno | Pantalla 14 |
| US-049 | Motor Notificaciones | Pantalla 15 |
| US-050 | CIAM Clientes Externos | Portal B2C |
| US-051 | Gobernanza Visual RBAC | Transversal (Router/Sidebar) |
| US-052 | Motor Multi-Agente IA | — (Backend + Config) |
| US-053 | Command Center FinOps | Command Center |

---

## 3. Análisis: Pantallas sin US y US sin Pantalla

### Pantallas referenciadas sin US directa
| Pantalla | Observación |
|---|---|
| **Pantalla 9** | No existe US que la genere. Posible omisión o pantalla descartada. |
| **Pantalla 11** | No identificada en el backlog actual. Verificar si fue descartada. |
| **Pantalla 12** | No identificada en el backlog actual. Verificar si fue descartada. |
| **Pantalla 17** | No identificada en el backlog actual. Verificar si fue descartada. |
| **Pantalla 18** | No identificada en el backlog actual. Verificar si fue descartada. |

### US sin representación visual (Backend-only / Transversales)
| US | Razón |
|---|---|
| US-004 | Webhook listener. No tiene UI propia; dispara procesos que aparecen en Pantalla 1. |
| US-015 | Job batch nocturno. No requiere pantalla. |
| US-017 | Capa de persistencia CQRS. Afecta APIs, no UI directamente. |
| US-019, US-020, US-022, US-023 | Lógica CRM/Intake backend. Los resultados aparecen en Pantalla 4. |
| US-032 | RAG vive dentro del motor BPMN. No tiene pantalla propia. |
| US-034 | Infraestructura de mensajería. |
| US-035 | Integración SharePoint. |
| US-052 | Motor de agentes IA. Config via archivos, no via UI propia en V1. |
