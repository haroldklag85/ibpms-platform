# 📑 Índice Maestro de User Stories — iBPMS V1

> **Última actualización:** 2026-04-18 (Auditoría Integral Sección 1.2) | **Total US:** 56 | **Archivos:** 7  
> **Coverage centralizada:** [`.agentic-sync/coverage_matrix.md`](../../.agentic-sync/coverage_matrix.md)  
> **Protocolo de lectura:** Los agentes DEBEN leer este índice primero, luego el archivo de épica específico. PROHIBIDO leer `v1_user_stories.md` directamente.

---

## Instrucciones para Agentes

1. **Leer este archivo** para identificar en qué épica está la US que necesitas.
2. **Abrir SOLO el archivo de épica** correspondiente (links abajo).
3. **Consultar la coverage** en `.agentic-sync/coverage_matrix.md` para conocer el estado de implementación.
4. **NUNCA leer** `v1_user_stories.md` (deprecado — causa timeout).

---

## Épica A — Motor Core, Orquestación & Persistencia
**Archivo:** [`epic_A_motor_core.md`](./epics/epic_A_motor_core.md) | **8 US** | ~100 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-000 | Resiliencia Integrada y Enmascaramiento PII Visual | ✅ Completada |
| US-001 | Obtener Tareas Pendientes en el Workdesk | ✅ Completada (30/30 CAs — 100%) |
| US-002 | Reclamar una Tarea de Grupo (Claim Task) | 🔨 En construcción (~9%) ⚠️ CRÍTICO: BD no persiste, assignee hardcodeado |
| US-004 | Iniciar un Proceso mediante Webhook (Plugin O365 Listener) | 🔨 En construcción (~71%) ⚠️ CA-6 sin RabbitMQ consumer, EmailWebhookController bypasea pipeline |
| US-008 | Mover Tarjeta en Tablero Kanban (Cambio de Estado) | 🔨 Scaffolding (~10%) — KanbanView con mock data hardcodeado, sin state machine |
| US-030 | Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban) | 🔨 En construcción (~85%) — Hub Ágil sustancial; CA-2 WBS bloqueado por US-006 |
| US-031 | Planificación y Ejecución de Proyecto Tradicional (Gantt) | ❌ Pendiente |
| US-017 | Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing) | 🔨 Refactoring ADR-001 (FormEventEntity extraída; FormEvent POJO pendiente de purificación) |

---

## Épica B — IDE Formularios, Diseño BPMN & Reglas DMN
**Archivo:** [`epic_B_formularios_bpmn.md`](./epics/epic_B_formularios_bpmn.md) | **7 US** | ~194 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-003 | Instanciar y Generar un Formulario "iForm Maestro" vs "Simple" | ✅ Completada |
| US-028 | Simulador de Contratos Zod en Memoria (In-Browser QA Sandbox) | ✅ Completada |
| US-029 | Ejecución y Envío de Formulario (iForm Maestro o Simple) | 🔨 En construcción (~55%) — Núcleo ACID sólido; FormBffCoreService mockeado; 26 CAs remediación sin verificar |
| US-039 | Formulario Genérico Base (Pantalla 7.B - El Camaleón Operativo) | ✅ Completada |
| US-005 | Desplegar y Versionar un Modelo de Proceso (BPMN) | ✅ Completada |
| US-006 | Diseñar la Estructura Base (WBS) de una Plantilla de Proyecto | ❌ Pendiente |
| US-007 | Generador Cognitivo de DMN (NLP a Tablas de Decisión) | 🔨 En construcción (~48%) ✅ IDOR remediado (Zero-Trust isolation) |

---

## Épica C — IA Operativa, MLOps & Buzones SAC
**Archivo:** [`epic_C_ia_mlops_sac.md`](./epics/epic_C_ia_mlops_sac.md) | **7 US** | ~30 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-011 | Filtrado Transversal en Bandeja Avanzada (Docketing) | ❌ Pendiente |
| US-012 | Propuesta de Respuesta para Correo Entrante | ❌ Pendiente |
| US-013 | Identificación Automática de Cliente y Enriquecimiento CRM | ❌ Pendiente |
| US-014 | Sugerencia de Acciones (Tareas) Operativas | ❌ Pendiente |
| US-015 | Feedback y Aprendizaje Supervisado (MLOps Batch) | ❌ Pendiente |
| US-016 | Gestión Multi-Buzón con Políticas por Buzón | ❌ Pendiente |
| US-037 | CRUD de Conexiones de Buzones (Intake API) | ❌ Pendiente |

---

## Épica D — CRM, Service Delivery, Intake Inteligente & Portal B2C
**Archivo:** [`epic_D_crm_intake_portal.md`](./epics/epic_D_crm_intake_portal.md) | **10 US** | ~59 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-019 | Conectividad Resiliente y Modo Degradado | ❌ Pendiente |
| US-020 | Estrategias de Sincronización Flexible | ❌ Pendiente |
| US-021 | Mapeo de Variables y Tolerance (Fricción Cero) | ❌ Pendiente |
| US-022 | Disparo 'Confirm-to-Create' por Correo (Plan A) | ❌ Pendiente |
| US-023 | Correlación Continua del Hilo | ❌ Pendiente |
| US-024 | Creación Global Restringida (Plan B) | ❌ Pendiente |
| US-025 | Experiencia de 'Cards' Dinámicas por Rol | ❌ Pendiente |
| US-026 | Portal del Cliente Externo (Vistas Tácticas y Estratégicas) | ❌ Pendiente |
| US-040 | Embudo Inteligente de Intake (Pre-Triaje y Descarte IA) | ❌ Pendiente |
| US-041 | Vista 360 del Cliente (Consolidación Global Externa) | ❌ Pendiente |

---

## Épica E — Seguridad, RBAC, Identidad & Configuración Global
**Archivo:** [`epic_E_seguridad_identidad_config.md`](./epics/epic_E_seguridad_identidad_config.md) | **7 US** | ~51 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-036 | Matriz de Control de Acceso Basado en Roles (RBAC) | ✅ Completada |
| US-038 | Asignación Multi-Rol y Sincronización EntraID | ✅ Completada |
| US-048 | Módulo Gestor Propio de Identidades (Internal IdP) | ✅ Completada |
| US-043 | Configuración Global de Service Level Agreements (SLA) | ✅ Completada (deuda CA-6) |
| US-042 | DevPortal: Generación Segura de API Keys y Extensibilidad | ❌ Pendiente |
| US-050 | Identidad y Onboarding de Clientes Externos (CIAM) | ❌ Pendiente |
| US-051 | Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend) | ❌ Pendiente |

---

## Épica F — Dashboards, Documentos, Integraciones & Gobernanza del Sistema
**Archivo:** [`epic_F_dashboards_integraciones.md`](./epics/epic_F_dashboards_integraciones.md) | **10 US** | ~60 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-009 | Visualizar Salud del Proceso (BAM Dashboard) | ❌ Pendiente |
| US-018 | Métricas de Desempeño y Calidad | ❌ Pendiente |
| US-010 | Generar y Descargar PDF a partir de datos del caso | ❌ Pendiente |
| US-035 | Integración SharePoint y Auditoría Documental | ❌ Pendiente |
| US-033 | Catálogo de API y Mapeo Visual | ❌ Pendiente |
| US-034 | Orquestación a través de RabbitMQ | ✅ Completada |
| US-044 | Gobernanza de Inteligencia Artificial (AI Limits & MLOps) | ❌ Pendiente |
| US-045 | Restricciones de Dominio Ágil y Documental (System Limits) | ❌ Pendiente |
| US-046 | Gobernanza de Rendimiento e Integraciones (Data & Perf) | ❌ Pendiente |
| US-049 | Motor Central de Notificaciones y Plantillas (Outbound Engine) | ❌ Pendiente |

---

## Épica G — IA Cognitiva, Agentes, LLM Engine & RAG Dual
**Archivo:** [`epic_G_ia_cognitiva_agentes_rag.md`](./epics/epic_G_ia_cognitiva_agentes_rag.md) | **7 US** | ~110 KB

| US | Nombre | Estado |
|----|--------|--------|
| US-027 | Copiloto IA (Auditoría ISO 9001 y Generador Consultivo BPMN) | ❌ Pendiente |
| US-032 | Orquestación de IA y Generative Task (RAG) | ❌ Pendiente |
| US-052 | Motor de Orquestación Multi-Agente IA | ❌ Pendiente |
| US-053 | Antigravity Command Center (Fábrica de Agentes IA) | ❌ Pendiente |
| US-054 | LLM Plugin Engine | ❌ Pendiente |
| US-056 | Memory Core Engine (RAG Conversacional) | ❌ Pendiente |
| US-057 | Knowledge Base Engine (RAG Documental) | ❌ Pendiente |

---

## Resumen de Distribución

| Épica | US | Tamaño | Completadas | En Construcción | Scaffolding | Pendientes |
|-------|:--:|-------:|:-----------:|:---------------:|:-----------:|:----------:|
| A — Motor Core | 8 | ~100 KB | 2 | 3 | 1 | 2 |
| B — Formularios/BPMN | 7 | ~194 KB | 4 | 2 | 0 | 1 |
| C — IA/MLOps/SAC | 7 | ~30 KB | 0 | 0 | 0 | 7 |
| D — CRM/Intake/Portal | 10 | ~59 KB | 0 | 0 | 0 | 10 |
| E — Seguridad/Config | 7 | ~51 KB | 4 | 0 | 0 | 3 |
| F — Dashboards/Integ. | 10 | ~60 KB | 1 | 0 | 0 | 9 |
| G — IA Cognitiva/RAG | 7 | ~110 KB | 0 | 0 | 0 | 7 |
| **TOTAL** | **56** | **~604 KB** | **11** | **5** | **1** | **39** |

> **Nota (2026-04-18):** Tabla actualizada post Auditoría Integral Sección 1.2. US-001 reclasificada a Completada (100%). US-002, US-004, US-029, US-030 detectadas como En Construcción parcial. US-008 como Scaffolding. US-007 con IDOR crítico remediado. Ver detalles en `.agentic-sync/coverage_matrix.md`.
