# Scope Master V1 — Plataforma iBPMS (Antigravity)

> [!IMPORTANT]
> **Documento de Gobierno del Alcance.** Cualquier cambio en lo que "entra" o "sale" de V1 DEBE registrarse aquí.  
> Para criterios de aceptación detallados, ver [`v1_user_stories.md`](./v1_user_stories.md).  
> Para visión estratégica del producto, ver [`functional_requirements.md`](./functional_requirements.md).  
> **Última actualización:** 2026-04-05

---

## Leyenda

| Columna | Significado |
|---|---|
| **MoSCoW** | MUST = obligatorio para V1, SHOULD = alto impacto pero diferible 30 días post-launch, COULD = nice-to-have |
| **Estado** | `No iniciada` / `Refinada` (analizada, CAs listos) / `En desarrollo` / `Completada` |
| **Pantalla** | Referencia a wireframe/mockup asociado. "Transversal" = aplica a todas. "—" = sin pantalla directa |

---

## Índice de Alcance V1

### ÉPICA 0: Gobernanza de Errores y Seguridad Global
*Reglas arquitectónicas universales que aplican transversalmente a todas las interacciones del iBPMS.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-000 | Resiliencia Integrada y Enmascaramiento PII Visual | MUST | Refinada | Transversal |

---

### ÉPICA 1: Orquestación y Workbenches (El Motor Core)
*Bandeja unificada de tareas, reclamación, priorización SLA y delegación.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-001 | Obtener Tareas Pendientes en el Workdesk | MUST | Refinada | Pantalla 1 |
| US-002 | Reclamar una Tarea de Grupo (Claim Task) | MUST | Refinada | Pantalla 1 |

---

### ÉPICA 2: IDE Web Pro-Code para Formularios (Vue 3, Zod & Dual-Pattern)
*Constructores de formularios iForm Maestro y Simple, simulación Zod, ejecución y envío.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-003 | Instanciar y Generar un Formulario "iForm Maestro" vs "Simple" | MUST | En desarrollo | Pantalla 7 |
| US-028 | Simulador de Contratos Zod en Memoria (In-Browser QA Sandbox) | SHOULD | No iniciada | Pantalla 7 |
| US-029 | Ejecución y Envío de Formulario (iForm Maestro o Simple) | MUST | Refinada | Pantalla 2 |
| US-039 | Formulario Genérico Base (Pantalla 7.B - El Camaleón Operativo) | MUST | No iniciada | Pantalla 7.B |

---

### ÉPICA 3: Inicio y Recepción (Triggers)
*Webhooks y listeners para iniciar procesos desde eventos externos.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-004 | Iniciar un Proceso mediante Webhook (Plugin O365 Listener) | MUST | No iniciada | — |

---

### ÉPICA 4: Diseño de Procesos (BPMN) y Estructuración de Proyectos
*Lienzo BPMN, versionado, despliegue, WBS y copiloto IA de modelado.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-005 | Desplegar y Versionar un Modelo de Proceso (BPMN) | MUST | Refinada | Pantalla 6 |
| US-006 | Diseñar la Estructura Base (WBS) de una Plantilla de Proyecto | MUST | No iniciada | Pantalla 8 |
| US-027 | Copiloto IA (Auditoría ISO 9001 y Generador Consultivo BPMN) | MUST | No iniciada | Pantalla 6 |

---

### ÉPICA 5: Modelado de Reglas de Negocio con IA (DMN)
*Generación de tablas de decisión DMN desde lenguaje natural.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-007 | Generador Cognitivo de DMN (NLP a Tablas de Decisión) | MUST | Refinada | Pantalla 6 |

---

### ÉPICA 6: Gestión Ágil y Kanban
*Tableros Kanban, proyectos ágiles (sprints) y proyectos tradicionales (Gantt).*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-008 | Mover Tarjeta en Tablero Kanban (Cambio de Estado) | MUST | No iniciada | Pantalla 3 |
| US-030 | Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban) | MUST | No iniciada | Pantalla 10 |
| US-031 | Planificación y Ejecución de Proyecto Tradicional (Gantt) | MUST | No iniciada | Pantalla 10.B |

---

### ÉPICA 7: Dashboards y Reportería Operativa (BAM)
*Salud del proceso, métricas de desempeño y calidad.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-009 | Visualizar Salud del Proceso (BAM Dashboard) | MUST | No iniciada | Pantalla 5 |
| US-018 | Métricas de Desempeño y Calidad | MUST | No iniciada | Pantalla 5 |

---

### ÉPICA 8: Generador Documental Jurídico (SGDEA)
*Generación de PDF, gestión documental con hash y auditoría SharePoint.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-010 | Generar y Descargar PDF a partir de datos del caso | SHOULD | No iniciada | Pantalla 2 |
| US-035 | Integración SharePoint y Auditoría Documental | MUST | No iniciada | — |

---

### ÉPICA 9: Inteligencia Artificial, MLOps y Buzones SAC
*Bandeja avanzada (docketing), copiloto de correos M365, feedback MLOps y gestión multi-buzón.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-011 | Filtrado Transversal en Bandeja Avanzada (Docketing) | MUST | No iniciada | Pantalla 1B |
| US-012 | Propuesta de Respuesta para Correo Entrante (con revisión humana) | MUST | No iniciada | Pantalla 2C |
| US-013 | Identificación Automática de Cliente y Enriquecimiento desde CRM | MUST | No iniciada | Pantalla 2C |
| US-014 | Sugerencia de Acciones (Tareas) Operativas | MUST | No iniciada | Pantalla 2C |
| US-015 | Feedback y Aprendizaje Supervisado (Nightly MLOps Batch) | MUST | No iniciada | — |
| US-016 | Gestión Multi-Buzón con Políticas por Buzón | MUST | No iniciada | Pantalla 16 |
| US-037 | CRUD de Conexiones de Buzones (Intake API) | MUST | No iniciada | Pantalla 16 |

---

### ÉPICA 10: Service Delivery CRM, Intelligent Intake y Portal B2C
*Conectividad CRM, intake inteligente, cards por rol, portal externo y vista 360.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-019 | Conectividad Resiliente y Modo Degradado | MUST | No iniciada | — |
| US-020 | Estrategias de Sincronización Flexible | MUST | No iniciada | — |
| US-021 | Mapeo de Variables y Tolerance (Fricción Cero) | MUST | No iniciada | Pantalla 15 |
| US-022 | Disparo 'Confirm-to-Create' por Correo (Plan A) | MUST | No iniciada | — |
| US-023 | Correlación Continua del Hilo | MUST | No iniciada | — |
| US-024 | Creación Global Restringida (Plan B) | MUST | No iniciada | Pantalla 4 |
| US-025 | Experiencia de 'Cards' Dinámicas por Rol | MUST | No iniciada | Pantalla 4 |
| US-026 | Portal del Cliente Externo (Vistas Tácticas y Estratégicas) | MUST | No iniciada | Portal B2C |
| US-040 | Embudo Inteligente de Intake (Pre-Triaje y Descarte IA) | MUST | No iniciada | Pantalla 4 |
| US-041 | Vista 360 del Cliente (Consolidación Global Externa) | MUST | No iniciada | Pantalla 4 |

---

### ÉPICA 11: Extensiones Cognitivas AI-Native — Cognitive BPMN
*RAG, generación de tareas IA y orquestación cognitiva de procesos.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-032 | Orquestación de IA y Generative Task (RAG) | MUST | No iniciada | — |

---

### ÉPICA 12: Hub Integraciones & Central Message Broker
*Catálogo de API, mapeo visual y orquestación mediante RabbitMQ.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-033 | Catálogo de API y Mapeo Visual | MUST | No iniciada | Pantalla 13 |
| US-034 | Orquestación a través de RabbitMQ | MUST | En desarrollo | — |

---

### ÉPICA 13: Seguridad, RBAC e Identidad
*Matriz RBAC, multi-rol, sincronización EntraID y gestor interno de identidades.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-036 | Matriz de Control de Acceso Basado en Roles (RBAC) | MUST | En desarrollo | Pantalla 14 |
| US-038 | Asignación Multi-Rol y Sincronización EntraID | MUST | No iniciada | Pantalla 14 |
| US-048 | Módulo Gestor Propio de Identidades (Internal IdP) | MUST | No iniciada | Pantalla 14 |

---

### ÉPICA 14: Configuraciones Globales de Nivel de Servicio — SLA
*Calendario comercial, horas hábiles, festivos y alertas preventivas de quiebre.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-043 | Configuración Global de Service Level Agreements (SLA) | MUST | No iniciada | Pantalla 19 |

---

### ÉPICA 15: Developer Portal, Settings y Límites del Sistema
*DevPortal, gobernanza IA, restricciones documentales, rendimiento, notificaciones, CIAM y gobernanza visual.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-042 | DevPortal: Generación Segura de API Keys y Extensibilidad | MUST | No iniciada | Pantalla 13 |
| US-044 | Gobernanza de Inteligencia Artificial (AI Limits & MLOps) | MUST | No iniciada | Pantalla 15 |
| US-045 | Restricciones de Dominio Ágil y Documental (System Limits) | MUST | No iniciada | Pantalla 15 |
| US-046 | Gobernanza de Rendimiento e Integraciones (Data & Perf) | MUST | No iniciada | Pantalla 15.A |
| US-049 | Motor Central de Notificaciones y Plantillas (Outbound Engine) | MUST | No iniciada | Pantalla 15 |
| US-050 | Identidad y Onboarding de Clientes Externos (CIAM / Zero-Public-Signup) | MUST | No iniciada | Portal B2C |
| US-051 | Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend) | MUST | En desarrollo | Transversal |

---

### ÉPICA 16: Persistencia Hexagonal y Patrón CQRS
*Event Sourcing, inmutabilidad de formularios, separación lectura/escritura.*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-017 | Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing) | MUST | Refinada | — |

---

### ÉPICA 17: Fábrica de Agentes IA y Gobernanza FinOps
*Motor multi-agente, fábrica de agentes, arbitraje de tokens y resiliencia BPMN.*
*(Promovidas de SHOULD a MUST por decisión PO 2026-04-05)*

| US | Nombre | MoSCoW | Estado | Pantalla |
|----|--------|--------|--------|----------|
| US-052 | Motor de Orquestación Multi-Agente IA (Arquitectura y Gobernanza de Contextos) | MUST | No iniciada | — |
| US-053 | Antigravity Command Center (Fábrica de Agentes IA y Arbitraje FinOps B2B) | MUST | No iniciada | Command Center |

---

## Resumen Estadístico

| Métrica | Valor |
|---|---|
| **Total Épicas** | 17 (Épica 0-17, sin Épica que empiece en numeración interna) |
| **Total US** | 53 |
| **MUST HAVE** | 51 |
| **SHOULD HAVE** | 2 (US-028, US-010) |
| **Refinadas** | 8 (US-000, US-001, US-002, US-005, US-007, US-017, US-029, US-039) |
| **En desarrollo** | 4 (US-003, US-034, US-036, US-051) |
| **No iniciadas** | 41 |
| **Completadas** | 0 |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|---|---|---|
| 2026-04-05 | Creación inicial del Scope Master V1 basado en diagnóstico de artefactos | PO (Antigravity) |
| 2026-04-05 | US-052 y US-053 promovidas de SHOULD a MUST. MoSCoW actualizado. | PO |
| 2026-04-05 | US-050 duplicada eliminada. Épica 13 renombrada. NFR corregido (MySQL→PostgreSQL). | PO |
