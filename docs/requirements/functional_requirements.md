# Requerimientos Funcionales y Estrategia Exponencial (PRD)
**Producto:** Plataforma iBPMS (Intelligent Business Process Management System) → Agentic Workflows  
**Última actualización:** 2026-04-05  
**Complemento detallado:** [`v1_user_stories.md`](./v1_user_stories.md) (53 US con Criterios de Aceptación Gherkin)  
**Gobierno del alcance:** [`scope_master_v1.md`](./scope_master_v1.md) (Matriz MoSCoW + Estado por US)

---

## 1. Objetivo Estratégico
Desarrollar una plataforma integral de gestión de procesos y proyectos que permita a las organizaciones acelerar su transformación digital, migrando procesos manuales a modelos digitales de forma ágil, estructurada y medible.
**Esto permite a la compañía adaptarse rápidamente a los constantes cambios del entorno económico y a los nuevos retos detonados por la Inteligencia Artificial (IA).** El foco principal es habilitar la modelación rápida de procesos, incorporando reglas de negocio, validaciones, métricas de desempeño y control operativo.

**North Star Metric:** Mediremos el **"Time-to-Value (TTV) de Orquestación"**: El tiempo promedio que tarda un usuario de negocio desde que tiene una necesidad hasta que la regla, proceso o subproceso está operando en producción, sin tocar código.

---

## 2. Product Discovery y Pain Points

**Contexto:** Los procesos operativos críticos varían significativamente según el cliente y su industria, siendo este dominio el gran "océano azul" donde radica el mayor valor de automatización de nuestra solución. Típicamente, estos procesos (aprobaciones, altas, contratos) están fragmentados y los usuarios interactúan con múltiples sistemas (Office 365, ERP, correos), generando cuellos de botella y pérdida de información.

**Pain Points Identificados:**
1. **Dispersión de Tareas:** Falta de un punto único de entrada (Unified Inbox).
2. **Falta de Trazabilidad:** Nula visibilidad del estado "end-to-end" ni SLAs.
3. **Reglas Rígidas:** Lógica de enrutamiento requiere intervención de TI.
4. **Captura Manual:** Leer correos y descargar adjuntos consume horas hombre.

**Segmentos a Disrumpir:**
*   **Para IT:** Plataforma centralizada y auditable (Hexagonal), delegando la creación a negocio.
*   **Para Negocio:** Autonomía para construir aplicaciones operativas complejas en minutos usando lenguaje natural.

---

## 3. Roadmap Estratégico (V1 a V2: Pivote AI-Centric)

| Fase / Capa | MVP Táctico Extendido (V1 - 5 a 6 Meses TTM) | Innovación Disruptiva (V2 AI-Native) |
| :--- | :--- | :--- |
| **Motor Central** | Orquestador Táctico Limpio (Camunda) + **Motor Multi-Agente IA** (US-052/US-053) para orquestación de fuerza laboral artificial dentro de procesos BPMN. | **AI-Centric Engine (Multi-Agent System):** Reemplazo absoluto del BPMN rígido. Sistema multi-agente que enruta dinámicamente basado en "Intento" (Intent) y resolución de objetivos en tiempo real. |
| **UX / Frontend** | Workbenches Web (Vue 3/Vite), Formularios Dinámicos (iForm Maestro + Simple), Tableros Kanban y **Antigravity Command Center** (Fábrica de Agentes). | **Zero-UI (Headless) & Conversational:** Ejecución directa vía chatbots, Slack/Teams y correos interactivos. |
| **Lógica / Reglas**| Tablas DMN manuales y **Traductor de IA (LLM API)** que convierte lenguaje natural a reglas DMN ejecutables (US-007). | **Razonamiento en Tiempo Real:** La IA detecta excepciones no programadas y sugiere/opera soluciones *on-the-fly*. |
| **Analítica & Mejora** | **Dashboards BAM & Salud Operativa** (US-009, US-018) desplegados sobre infraestructura de BI analítico (Grafana/PowerBI). | **Consultor Digital AI (ML):** Aprendizaje continuo. La plataforma actúa como consultor, detectando ineficiencias ocultas y proponiendo rediseños basados en datos reales de operación. |
| **Auditoría & Control**| Bitácoras pasivas de lectura/escritura + **Event Sourcing inmutable** (US-017, CQRS) + **Gobernanza Visual RBAC** (US-051). | **Auditor AI (Plug-in Compliance):** Auditoría activa y continua (ISO 9001) sobre procesos gestionados. |
| **Arquitectura** | Arquitectura Hexagonal estricta con **PostgreSQL** (JSONB), Monolito Vue 3/Vite, Spring Boot y Azure (IaaS/PaaS). | Agentic RAG y Graph DB. Los procesos, automatizaciones y funcionalidades nacen a partir de las capacidades de la IA. |

---

## 4. Requerimientos Core del Dominio (V1)

> [!NOTE]
> Cada requerimiento está implementado como una o más Historias de Usuario detalladas en [`v1_user_stories.md`](./v1_user_stories.md). La referencia (US-XXX) permite trazabilidad directa.

### 4.1 Orquestación y Experiencia del Usuario
*   **FR-01 — Bandeja Unificada de Tareas (Workdesk)** *(US-001)*: Lista consolidada de tareas pendientes (BPMN + Kanban + Gantt) con priorización SLA, semáforos en tiempo real, búsqueda híbrida y delegación visual.
*   **FR-02 — Reclamación de Tareas (Claim)** *(US-002)*: Asignación individual desde cola de grupo con auto-claim, anti-acaparamiento, desaparición fantasma vía WebSocket y bloqueo de concurrencia.
*   **FR-03 — Bandeja Avanzada (Docketing SAC)** *(US-011)*: Filtrado transversal de alto volumen para equipos de servicio al cliente.

### 4.2 Formularios e IDE Pro-Code
*   **FR-04 — Constructor Web de Formularios (iForm IDE)** *(US-003)*: Editor Low-Code/Pro-Code (Mónaco) que genera archivos `.vue` nativos y validaciones `.zod`. Soporta "iForm Maestro" (multi-etapa/rol) y "Formulario Simple". Persistencia en PostgreSQL JSONB.
*   **FR-05 — Ejecución y Envío de Formulario** *(US-029)*: Renderizado dinámico, validación isomórfica Zod, Upload-First para archivos, borradores LocalStorage y envío con feedback visual.
*   **FR-06 — Simulador Zod (QA Sandbox)** *(US-028)*: Sandbox en navegador para inyectar payloads extremos contra esquemas Zod en tiempo real.
*   **FR-07 — Formulario Genérico Base** *(US-039)*: Camaleón operativo que adapta la vista según el esquema recibido.

### 4.3 Diseño de Procesos y Reglas
*   **FR-08 — Modelador BPMN** *(US-005)*: Lienzo visual integrado (bpmn-js) con versionado semántico, despliegue sin código Java y auto-generación RBAC desde Lanes.
*   **FR-09 — Estructuración de Proyectos (WBS)** *(US-006)*: Diseño de plantillas de proyecto con desglose de trabajo.
*   **FR-10 — Copiloto IA Diseñador BPMN** *(US-027)*: Agente experto en ISO 9001 empotrado en el Canvas que audita antipatrones en tiempo real.
*   **FR-11 — Generador Cognitivo de DMN** *(US-007)*: Conversión de lenguaje natural a tablas de decisión DMN ejecutables.

### 4.4 Gestión Ágil, Kanban y Proyectos
*   **FR-12 — Tablero Kanban** *(US-008)*: Gestión visual de tareas con cambio de estado por arrastre.
*   **FR-13 — Proyecto Ágil (Sprints)** *(US-030)*: Instanciación y planificación de proyectos ágiles con tablero Kanban propio.
*   **FR-14 — Proyecto Tradicional (Gantt)** *(US-031)*: Planificación y ejecución con diagrama de Gantt, dependencias y ruta crítica.

### 4.5 Inteligencia Artificial y MLOps
*   **FR-15 — Copiloto AI de Correos M365** *(US-012, US-013, US-014)*: Agente que lee buzones entrantes vía MS Graph API, detecta intención, cruza con CRM, propone borradores de respuesta y sugiere acciones operativas con trazabilidad eDiscovery.
*   **FR-16 — Feedback MLOps** *(US-015)*: Aprendizaje supervisado nocturno basado en correcciones humanas (Human-in-the-Loop).
*   **FR-17 — Gestión Multi-Buzón** *(US-016, US-037)*: Políticas por buzón y CRUD de conexiones de buzones corporativos con aislamiento mandatorio.
*   **FR-18 — Motor Multi-Agente IA** *(US-052)*: 4 agentes especializados (Orquestador, Backend, Frontend, QA) con separación de memorias, reglas CORE universales e inyección modular Just-in-Time.
*   **FR-19 — Antigravity Command Center** *(US-053)*: Fábrica de agentes IA, modelo híbrido de consumo (Suscripción + Billetera Prepaga), alertas de umbral, downgrade automático y resiliencia BPMN ante falta de fondos.

### 4.6 Service Delivery, CRM e Intake Inteligente
*   **FR-20 — Conectividad CRM Resiliente** *(US-019, US-020, US-021)*: Conector API hacia CRM externo con modo degradado (caché), sincronización flexible y mapeo de variables JSON con auditoría.
*   **FR-21 — Intake Controlado (Plan A/B)** *(US-022, US-023, US-024)*: Confirmación por correo que genera tareas sin detonar procesos huérfanos (Plan A) + creación manual global protegida por rol (Plan B).
*   **FR-22 — Cards Dinámicas por Rol** *(US-025)*: Vistas segregadas: Admin (totales), Operador (mis tareas), Cliente (citas/SD).
*   **FR-23 — Embudo Inteligente de Intake** *(US-040)*: Pre-triaje y descarte IA con reentrenamiento de modelos por descarte.
*   **FR-24 — Vista 360 del Cliente** *(US-041)*: Consolidación global de actividades pendientes por servicio y etapa.
*   **FR-25 — Portal del Cliente Externo (B2B/B2C)** *(US-026)*: Auto-consulta de estado táctico y descarga de histórico estratégico con documentos finales.

### 4.7 Dashboards, Reportería y Documentos
*   **FR-26 — Dashboards BAM** *(US-009)*: Visualización de cuellos de botella y salud de proyectos (Grafana/PowerBI).
*   **FR-27 — Métricas de Desempeño** *(US-018)*: Indicadores de eficiencia en ejecución de actividades, equipos y flujos.
*   **FR-28 — Generador Documental (SGDEA)** *(US-010)*: Inyectar JSON en plantilla para generar PDF contractual con hash SHA-256.
*   **FR-29 — Integración Documental SharePoint** *(US-035)*: Bóveda documental con versionado, clasificación categórica e inmutabilidad.

### 4.8 Seguridad, RBAC e Identidad
*   **FR-30 — Matriz RBAC** *(US-036)*: Gestión centralizada de permisos, perfiles, delegaciones con ISO 27001, Row-Level Security y segregación de funciones.
*   **FR-31 — Multi-Rol y EntraID** *(US-038)*: Asignación de múltiples roles por usuario con sincronización desde Azure EntraID.
*   **FR-32 — Gestor Interno de Identidades** *(US-048)*: IdP local para usuarios sin directorio corporativo.
*   **FR-33 — Gobernanza Visual Frontend** *(US-051)*: Auto-colapso de menús por rol, anti-FOUC, gaslighting 404 vs 403, Sudo Mode y revocación WebSocket en caliente.
*   **FR-34 — CIAM (Onboarding Clientes Externos)** *(US-050)*: Magic Links, Zero-Public-Signup y vinculación criptográfica de identidad.

### 4.9 Integraciones, Mensajería y Triggers
*   **FR-35 — Webhooks O365** *(US-004)*: Listener que inicia procesos BPMN desde eventos externos (correos, webhooks O365).
*   **FR-36 — Catálogo de API y Mapeo Visual** *(US-033)*: Registro y visualización de APIs disponibles para integraciones.
*   **FR-37 — Orquestación RabbitMQ** *(US-034)*: Central Message Broker para desacoplamiento de eventos con DLQ, reintentos y monitoreo.
*   **FR-38 — Orquestación Cognitiva (RAG)** *(US-032)*: Integración de LLM con Retrieval-Augmented Generation para tareas generativas dentro de procesos BPMN.

### 4.10 Configuración, Gobernanza y Plataforma
*   **FR-39 — Configuración SLA** *(US-043)*: Calendario comercial parametrizable, horas hábiles, festivos y alertas preventivas de quiebre de nivel.
*   **FR-40 — DevPortal** *(US-042)*: Portal de desarrolladores para API Keys, módulos externos y extensibilidad Zero-Trust.
*   **FR-41 — Gobernanza IA** *(US-044)*: Límites de tokens, configuración de modelos LLM y directrices MLOps por tenant.
*   **FR-42 — Restricciones de Dominio** *(US-045)*: Límites ágiles (máx. sprints, épicas) y documentales (tamaño archivos, retención).
*   **FR-43 — Gobernanza de Rendimiento** *(US-046)*: Rate-limiting, políticas de caché y health-checks de integraciones.
*   **FR-44 — Motor de Notificaciones** *(US-049)*: Motor centralizado de correos con plantillas dinámicas, despacho asíncrono (Outbox pattern) y resiliencia ante caídas SMTP.

### 4.11 Persistencia Arquitectónica
*   **FR-45 — Event Sourcing Inmutable (CQRS)** *(US-017)*: Persistencia inmutable con `form_event_store`, separación lectura/escritura, rollback compensatorio (no destructivo), borradores efímeros con TTL y cifrado PII at-rest.

---

## 5. Stack Tecnológico V1 (Decisiones Confirmadas)

| Capa | Tecnología | Decisión |
|---|---|---|
| **Backend** | Spring Boot (Java) | Arquitectura Hexagonal estricta |
| **Frontend** | Vue 3 + Vite (Monolito SPA) | TypeScript, Pinia, Vue Router |
| **Base de datos** | PostgreSQL (JSONB) | Fuente de verdad para formularios, eventos y configuraciones |
| **Motor BPMN** | Camunda 7 (Embebido) | Aislado del dominio Core. Migración a Zeebe en V2 |
| **Mensajería** | RabbitMQ | Colas, DLQ, reintentos con Exponential Backoff |
| **Infraestructura** | Azure (IaaS + PaaS) | Key Vault, VMSS, Azure AD, Managed Disks |
| **LLM / IA** | API externa (Gemini/OpenAI) | Sesiones HTTP aisladas por agente |
| **Validación** | Zod (isomórfico: Frontend + Backend transpilado) | json-schema-validator en Backend |

---

## 6. Referencia a Documentos Complementarios

| Documento | Propósito | Ruta |
|---|---|---|
| **Scope Master V1** | Gobierno del alcance: MoSCoW + Estado por US | [`scope_master_v1.md`](./scope_master_v1.md) |
| **Historias de Usuario (SSOT)** | 53 US con CAs Gherkin detallados | [`v1_user_stories.md`](./v1_user_stories.md) |
| **MoSCoW Original** | Acta fundacional de alcance (día 0) | [`v1_moscow_scope_validation.md`](./v1_moscow_scope_validation.md) |
| **NFRs** | Atributos de calidad ISO 25010 | [`non_functional_requirements.md`](./non_functional_requirements.md) |
