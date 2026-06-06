# Backlog Futuro V3 — Hoja de Ruta Post-Profilaxis 🗺️

> **Versión:** 3.0 (Reescritura total — V2 destruida por inconsistencia factual)
> **Fecha base:** 2026-04-18
> **Autor:** Arquitecto Líder SW
> **Premisa:** Este documento parte EXCLUSIVAMENTE del estado real verificado del repositorio, no de proyecciones especulativas.

---

## 1. Estado Real del Proyecto (Línea Base Verificada)

### 1.1 Sprints Ejecutados y Cerrados

| Sprint | Título | Objetivo Real | Estado |
|:------:|--------|---------------|:------:|
| **S0** | Infraestructura Agéntica | Framework ACP, Playwright, Docker, Smoke Test | ✅ Cerrado |
| **Puente** | Deuda US-001/US-002 | WebSockets, Claiming, integración botones | ✅ Cerrado |
| **S1** | Test Pyramid | Hardening unitario/API de 11 US pre-existentes (Code Freeze) | ✅ Cerrado |
| **S2** | Playwright UAT | E2E guiado por modelos UAT (Code Freeze) | ✅ Cerrado |
| **S3** | Feature Factory V2 | Construcción US-004, US-017, US-030 + Certificación QA | ✅ Cerrado |
| **S4** | Profilaxis & Saneamiento | Fencing GAPs + Refactor FormEvent ADR-001 | ✅ Cerrado |

### 1.2 Inventario Real de Historias de Usuario Construidas

| US | Título | Épica | Sprint Construido | Estado |
|----|--------|:-----:|:-----------------:|:------:|
| US-000 | Resiliencia PII & Error Handling | A | Pre-existente | ✅ Operativa |
| US-001 | Workdesk (Tareas Pendientes) | A | Pre + Puente | ✅ Operativa |
| US-002 | Claim Task (Reclamar Tarea) | A | Pre + Puente | ✅ Operativa |
| US-003 | iForm Maestro (Instanciar Formulario) | B | Pre-existente | ✅ Operativa |
| US-004 | Webhook Intake (O365 Listener) | A | S3 | ✅ Operativa |
| US-005 | BPMN Modeler & Deploy | B | Pre-existente | ✅ Operativa |
| US-007 | DMN Viewer | B | Pre-existente | ✅ Operativa |
| US-008 | Kanban (Mover Tarjeta) | A | Pre-existente | ✅ Operativa |
| US-017 | CQRS Event Sourcing | A | S3 | 🔨 Refactoring (ADR-001) |
| US-028 | Simulador Zod (QA Sandbox) | B | Pre-existente | ✅ Operativa |
| US-029 | Ejecución y Envío de Formulario | B | Pre-existente | ✅ Operativa |
| US-030 | Hub Ágil (Proyectos/Sprints/Kanban) | A | S3 | ✅ Operativa |
| US-034 | RabbitMQ Orquestación | F | Pre-existente | ✅ Operativa |
| US-036 | RBAC & EntraID | E | Pre-existente | ✅ Operativa |
| US-038 | Multi-Rol & Sync EntraID | E | Pre-existente | ✅ Operativa |
| US-039 | Formulario Genérico Base | B | Pre-existente | ✅ Operativa |
| US-043 | SLA Global Config | E | Pre-existente | ✅ Operativa |
| US-048 | Internal IdP | E | Pre-existente | ✅ Operativa |

**Total verificado: ~18 US operativas** (sujeto a confirmación por auditoría de cobertura de CAs).

### 1.3 Deuda Técnica Controlada (Scaffolding Activo — Cercadas con Fencing)

| US | Título | Épica | Tipo de Fencing Aplicado | Sprint Origen |
|----|--------|:-----:|--------------------------|:-------------:|
| US-011 | Docketing SAC (Bandeja Correos) | C | `UnsupportedOperationException` en MailboxPollingCron | S4 |
| US-021 | Mapeo Variables CRM | D | Sidebar comentado (Pantalla 11 oculta) | S4 |
| US-035 | SharePoint & Firma Digital | F | `UnsupportedOperationException` en SharePointAdapterService | S4 |
| US-045 | Restricciones de Dominio | F | `@Operation(hidden=true)` + HTTP 501 en AllowedDomainAdminController | S4 |

**Estas 4 US NO están funcionales.** Su código perimetral existe solo como andamiaje estructural esterilizado.

### 1.4 Casos de Uso UAT Escritos

| Archivo | Cobertura |
|---------|-----------|
| `casos_uso_uat_j02.md` | Journey J-02 (Diseñador BPM) |
| `casos_uso_uat_j04.md` | Journey J-04 (Operario MVP) |
| `casos_uso_uat_us001_sprint1.md` | US-001 Workdesk |
| `casos_uso_uat_us002.md` | US-002 Claiming |
| `casos_uso_uat_us029.md` | US-029 Form Submit |

**Total UAT escritos: 5.** El resto de las US carecen de escenarios UAT formalizados.

---

## 2. Inventario Completo de User Stories por Épica (Fuente: `/docs/requirements/epics/`)

### Épica A — Motor Core & Orquestación (8 US)
| US | Título | Estado |
|----|--------|:------:|
| US-000 | Resiliencia PII & Error Handling | ✅ |
| US-001 | Workdesk | ✅ |
| US-002 | Claim Task | ✅ |
| US-004 | Webhook Intake | ✅ |
| US-008 | Kanban (Mover Tarjeta) | ✅ |
| US-017 | CQRS Event Sourcing | 🔨 Refactor |
| US-030 | Hub Ágil | ✅ |
| US-031 | Gantt (Proyecto Tradicional) | ⬜ Pendiente |

### Épica B — Formularios & BPMN (6 US)
| US | Título | Estado |
|----|--------|:------:|
| US-003 | iForm Maestro | ✅ |
| US-005 | BPMN Modeler & Deploy | ✅ |
| US-006 | WBS Plantilla Proyecto | ⬜ Pendiente |
| US-007 | Generador DMN (NLP) | ✅ (Viewer; generación IA pendiente) |
| US-028 | Simulador Zod | ✅ |
| US-029 | Formulario Submit | ✅ |
| US-039 | Formulario Genérico Base | ✅ |

### Épica C — IA/MLOps & SAC (6 US)
| US | Título | Estado |
|----|--------|:------:|
| US-011 | Docketing SAC | 🟡 Scaffolding (Fenced) |
| US-012 | Propuesta Respuesta IA | ⬜ Pendiente |
| US-013 | Enriquecimiento CRM (ONS) | ⬜ Pendiente |
| US-014 | Sugerencia Acciones IA | ⬜ Pendiente |
| US-015 | Feedback MLOps Batch | ⬜ Pendiente |
| US-016 | Multi-buzón con Políticas | ⬜ Pendiente |
| US-037 | CRUD Conexiones Buzones | ⬜ Pendiente |

### Épica D — CRM, Intake & Portal (9 US)
| US | Título | Estado |
|----|--------|:------:|
| US-019 | Conectividad Resiliente | ⬜ Pendiente |
| US-020 | Sincronización Flexible | ⬜ Pendiente |
| US-021 | Mapeo Variables CRM | 🟡 Scaffolding (Fenced) |
| US-022 | Confirm-to-Create Email | ⬜ Pendiente |
| US-023 | Correlación Hilo Email | ⬜ Pendiente |
| US-024 | Creación Global Restringida | ⬜ Pendiente |
| US-025 | Cards Dinámicas por Rol | ⬜ Pendiente |
| US-026 | Portal Cliente Externo | ⬜ Pendiente |
| US-040 | Embudo Intake (Pre-Triaje IA) | ⬜ Pendiente |
| US-041 | Vista 360 Cliente | ⬜ Pendiente |

### Épica E — Seguridad, Identidad & Config (7 US)
| US | Título | Estado |
|----|--------|:------:|
| US-036 | RBAC | ✅ |
| US-038 | Multi-Rol EntraID | ✅ |
| US-042 | DevPortal API Keys | ⬜ Pendiente |
| US-043 | SLA Config Global | ✅ |
| US-045 | Restricciones Dominio | 🟡 Scaffolding (Fenced) |
| US-048 | Internal IdP | ✅ |
| US-050 | CIAM Onboarding Externo | ⬜ Pendiente |
| US-051 | Gobernanza Visual RBAC Frontend | ⬜ Pendiente |

### Épica F — Dashboards & Integraciones (7 US)
| US | Título | Estado |
|----|--------|:------:|
| US-009 | BAM Dashboard | ⬜ Pendiente |
| US-010 | Generador PDF | ⬜ Pendiente |
| US-018 | Métricas Desempeño | ⬜ Pendiente |
| US-033 | Catálogo API & Mapeo Visual | ⬜ Pendiente |
| US-034 | RabbitMQ Orquestación | ✅ |
| US-035 | SharePoint & Firma | 🟡 Scaffolding (Fenced) |
| US-044 | Gobernanza IA (FinOps) | ⬜ Pendiente |
| US-046 | Gobernanza Rendimiento | ⬜ Pendiente |
| US-049 | Motor Notificaciones | ⬜ Pendiente |

### Épica G — IA Cognitiva, Agentes & RAG (5 US)
| US | Título | Estado |
|----|--------|:------:|
| US-027 | Copiloto IA (ISO 9001 / BPMN) | ⬜ Pendiente |
| US-032 | Generative Task (RAG) | ⬜ Pendiente |
| US-052 | Motor Multi-Agente IA | ⬜ Pendiente |
| US-053 | Antigravity Command Center | ⬜ Pendiente |
| US-054 | LLM Plugin Engine | ⬜ Pendiente |
| US-056 | Memory Core Engine | ⬜ Pendiente |
| US-057 | Knowledge Base RAG | ⬜ Pendiente |

---

## 3. Resumen Cuantitativo

| Métrica | Valor |
|---------|:-----:|
| **US totales documentadas** | ~55 |
| **US operativas (código funcional + tests)** | ~18 |
| **US en scaffolding (cercadas/fenced)** | 4 |
| **US pendientes (sin código)** | ~33 |
| **UATs escritos** | 5 |
| **E2E Specs Playwright estimados** | ~20-22 |
| **Velocidad real demostrada** | 3 US/sprint (S3 fue el pico) |

---

## 4. Criterios para la Priorización de Sprints Futuros

Antes de asignar US a sprints futuros, el PO/Jefe de Equipo debe considerar:

1. **Dependencias técnicas reales:** Muchas US de Épica C (IA/SAC) y Épica D (CRM/Portal) requieren credenciales MS Graph, Azure Key Vault y SDK de servicios externos que no están disponibles actualmente.
2. **Madurez de UATs:** Solo 5 de ~55 US tienen UATs escritos. Cualquier sprint futuro debe incluir la redacción de UATs como pre-requisito obligatorio (Día 0 del PO).
3. **Capacidad real vs. aspiracional:** La velocidad demostrada es de 3 US/sprint con 1 agente Backend + 1 Frontend + 1 QA. Proyectar 7-10 US/sprint sin evidencia es irresponsable.
4. **Épica G es de largo plazo:** Las US-052 a US-057 (Motor Multi-Agente, RAG, Memory Core) son de complejidad extrema y requieren un sprint dedicado de investigación antes de cualquier estimación.

---

## 5. Propuesta de Agrupación por Olas (Sin Fechas — Sujeto a Refinamiento)

### Ola 1: Integración Core (Post-Profilaxis)
**Objetivo:** Activar las integraciones con sistemas Microsoft y completar el ciclo de vida del caso.
- US-011 (Docketing SAC — quitar fencing + MS Graph real)
- US-016 (Multi-buzón con políticas)
- US-037 (CRUD Buzones)
- US-022 (Confirm-to-Create Email)
- US-023 (Correlación Hilo)
- **Pre-requisito bloqueante:** Credenciales OAuth2 de MS Graph disponibles.

### Ola 2: CRM & Portal Externo
**Objetivo:** Habilitar la interacción con clientes externos y sistemas CRM.
- US-021 (Mapeo Variables CRM — quitar fencing)
- US-019 (Conectividad Resiliente)
- US-020 (Sincronización Flexible)
- US-050 (CIAM Onboarding)
- US-026 (Portal Cliente Externo)
- US-041 (Vista 360 Cliente)
- **Pre-requisito bloqueante:** ADR sobre arquitectura del Portal (SPA separada vs micro-frontend).

### Ola 3: Documentación & Firma Digital
**Objetivo:** Completar la gestión documental con SharePoint y generación de PDFs.
- US-035 (SharePoint — quitar fencing + Graph API real)
- US-010 (Generador PDF)
- US-045 (Restricciones Dominio — quitar fencing)
- **Pre-requisito bloqueante:** ADR sobre PKI/Firma Digital (certificados X.509 vs. servicio externo).

### Ola 4: Proyectos Tradicionales & UI Avanzada
**Objetivo:** Completar las metodologías de gestión de proyectos.
- US-031 (Gantt / Proyecto Tradicional)
- US-006 (WBS Plantilla)
- US-025 (Cards Dinámicas)
- US-009 (BAM Dashboard)
- US-018 (Métricas Desempeño)

### Ola 5: IA Cognitiva & Agentes
**Objetivo:** Construir la capa cognitiva del sistema.
- US-012 (Propuesta Respuesta IA)
- US-013 (Enriquecimiento CRM desde IA)
- US-014 (Sugerencia Acciones IA)
- US-027 (Copiloto IA ISO 9001)
- US-040 (Embudo Intake con Pre-Triaje IA)
- **Pre-requisito bloqueante:** Sprint de investigación sobre modelo LLM (Azure OpenAI vs Gemini vs local).

### Ola 6: Plataforma IA Avanzada (Horizonte Largo)
**Objetivo:** Motor de agentes autónomos y RAG empresarial.
- US-032 (Generative Task RAG)
- US-052 (Motor Multi-Agente)
- US-053 (Antigravity Command Center)
- US-054 (LLM Plugin Engine)
- US-056 (Memory Core Engine)
- US-057 (Knowledge Base RAG)
- US-015 (MLOps Batch)
- **Pre-requisito bloqueante:** Investigación RAG + definición de stack vectorial (Pinecone, pgvector, Milvus).

### Transversales (Cualquier Ola)
- US-042 (DevPortal API Keys)
- US-044 (FinOps IA)
- US-046 (Gobernanza Rendimiento)
- US-049 (Motor Notificaciones)
- US-051 (Gobernanza Visual RBAC Frontend)
- US-033 (Catálogo API)
- US-024 (Creación Global Restringida)

---

## 6. Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-10 | V2 creada (especulativa, sin base factual) | Arquitecto Lead |
| 2026-04-18 | **V2 DESTRUIDA.** V3 creada desde inventario real del repositorio post-profilaxis S4 | Arquitecto Lead |
