# 🏛️ Arquitectura iBPMS Platform — Resumen Ejecutivo Completo

> **Base de conocimiento:** Lectura directa y literal de los 22 documentos en `docs/architecture/`.  
> **Fuentes leídas:** 13 ADRs + C4 V1 + C4 V2 + ERD + RabbitMQ Topology + UI Schema + BPMN Mapping + Architecture Audit Consolidated.  
> **Zero alucinaciones. Zero inferencias.**

---

## 1. ¿Qué es iBPMS?

**iBPMS** (Intelligent Business Process Management System) es una plataforma empresarial de orquestación de procesos de negocio. Su propósito central es digitalizar, automatizar y auditar flujos de trabajo corporativos complejos (expedientes, aprobaciones, contratos, gestión documental), añadiendo capacidades de Inteligencia Artificial (RAG, LLM) para procesos cognitivos avanzados.

La plataforma está diseñada en **dos etapas evolutivas**:
- **V1 (Táctica / PoC):** Monolito modular sobre Azure IaaS VMs con Camunda 7 empotrado.
- **V2 (Estratégica / Cloud-Native):** Multi-Agent System sobre Kubernetes (AKS), con CQRS distribuido, Kafka y LLMs como motor de decisiones.

---

## 2. Diagrama de Capas de la Arquitectura V1

```mermaid
graph TB
    subgraph Actores["👥 Actores"]
        A1[Usuario Interno<br/>Workdesk/Bandeja]
        A2[Cliente B2B/B2C<br/>Portal]
        A3[Arquitecto/Admin<br/>Modeler BPMN/DMN]
    end

    subgraph ExtSystems["🌐 Sistemas Externos"]
        E1[Microsoft Graph<br/>O365 Webhooks]
        E2[SharePoint API]
        E3[CRM Corporativo]
        E4[ERP Local]
        E5[IdP / EntraID]
    end

    subgraph Frontend["🖥️ Frontend (Vue 3 + Vite · Puerto 5173)"]
        F1[Workdesk / Bandeja Unificada]
        F2[IDE Modeladores<br/>bpmn-js / dmn-js / Zod]
        F3[Portal B2B<br/>Autoconsulta]
        F4[DevPortal<br/>Súper Módulos / iFrames]
        F5[Dashboard BAM<br/>Grafana Embebido]
    end

    subgraph APIM["🛡️ Azure APIM (API Gateway)"]
        G1[Punto de entrada único<br/>JWT Validation · TLS 1.2+]
    end

    subgraph Backend["⚙️ Backend Monolítico (Spring Boot 3.2.3 · Puerto 8080)"]
        subgraph HexDomain["Dominio Hexagonal"]
            D1[Expediente/Proyecto<br/>POJO Puro]
        end
        subgraph UseCases["Application Use Cases"]
            UC1[CaseManagement UseCase]
            UC2[Security & RBAC UseCase]
            UC3[AppBuilder UseCase]
            UC4[Circuit Breaker Manager]
            UC5[Javers Audit & TX]
        end
        subgraph DrivingAdapters["Driving Adapters (Entrada)"]
            DA1[REST Controllers]
            DA2[O365 Webhook Listener]
        end
        subgraph DrivenAdapters["Driven Adapters (Salida)"]
            DR1[Camunda 7 API Adapter]
            DR2[PostgreSQL JPA Repositories]
            DR3[Graph API Client - MS SDK]
            DR4[CRM Outbound Port - Feign]
            DR5[ERP Connector - Feign]
            DR6[AI / LLM Adapter]
            DR7[Template Renderer - PDFBox]
            DR8[Azure Blob Storage SDK]
        end
        subgraph EmbeddedEngine["Motor Empotrado (.jar)"]
            CE[Camunda 7 BPM + DMN Engine<br/>v7.21.0]
            DMN[ibpms-dmn-engine<br/>camunda-engine-dmn 7.20.0]
        end
    end

    subgraph MsgBroker["📨 RabbitMQ 3 (Puerto 5672)"]
        Q1[ibpms.notifications.email]
        Q2[ibpms.ai.generation]
        Q3[ibpms.integrations.webhook]
        Q4[ibpms.bpmn.events]
        Q5[ibpms.task.rescue]
        DLQ[ibpms.dlq.global · TTL 30d]
    end

    subgraph Infra["🗄️ Infraestructura de Datos"]
        PG[(PostgreSQL 15+ · pgvector<br/>Puerto 5432)]
        RD[(Redis 7<br/>Puerto 6379 · Cache/Locks)]
        BS[(Azure Blob Storage<br/>Documentos Binarios)]
    end

    subgraph RPA["🤖 RPA Module"]
        SC[judicial-scraper<br/>Python 3.11]
    end

    A1 & A2 & A3 --> APIM
    E1 -->|Webhook Push| APIM
    APIM --> Frontend
    APIM --> Backend
    Frontend <-->|JSON API| APIM

    DA1 & DA2 --> UseCases
    UseCases --> HexDomain
    UseCases --> DrivenAdapters
    DR1 <--> CE
    CE <--> DMN
    DR2 <--> PG
    CE <--> PG
    Backend <--> MsgBroker
    DR8 <--> BS
    Backend <--> RD
    Backend --> E1 & E2 & E3 & E4
    E5 --> APIM
    SC --> Backend
```

---

## 3. Decisiones de Arquitectura (ADRs) — Mapa Completo

| ADR | Título | Decisión Central | Estado |
|-----|--------|-----------------|--------|
| **ADR-001** | Arquitectura Hexagonal y DDD | Dominio aislado de frameworks. Puertos & Adaptadores obligatorios. | ✅ Aceptado |
| **ADR-002** | Vue 3 + Vite para Frontend | Vue 3 (Composition API + Script Setup) empacado con Vite 5. | ✅ Aceptado |
| **ADR-003** | Camunda 7 Embedded (Motor BPM) | Camunda 7 como `.jar` empotrado en Spring Boot. Deuda técnica aceptada para V1. | ✅ Aceptado |
| **ADR-004** | Almacenamiento de Binarios | **PROHIBICIÓN** de BLOB en DB. Azure Blob Storage con patrón Claim Check. Solo metadata en PostgreSQL. | ✅ Aprobado |
| **ADR-005** | Extensibilidad / DevPortal | Súper Módulos vía **iFrames sandboxed** en V1. Module Federation (V2). JWT Custom Claims para auth. | ✅ Aceptado |
| **ADR-006** | Motor Low-Code iForms (Vue 3) | Formularios dinámicos JSON→UI vía Server-Driven UI. Zod Factory Pattern. Sin `eval()`. | ✅ Aprobado |
| **ADR-007** | CMMN vs Kanban | **RECHAZO ROTUNDO** de CMMN. Kanban = Máquinas de Estado Relacionales puras (JPA/Spring) | ✅ Aprobado |
| **ADR-008** | Time Tracking Universal | Tabla `ibpms_time_logs` polimórfica con `reference_type (BPMN/AGILE/GANTT)`. Componente `<UniversalSlaTimer/>`. | ✅ Aprobado |
| **ADR-009** | PostgreSQL + pgVector | Migración de MySQL 8 a **PostgreSQL 15+ con pgvector**. Motor relacional + vectorial en 1 VM. | ✅ Aceptado |
| **ADR-010** | Pirámide de Testing (4 niveles) | Testcontainers reemplaza H2. REST Assured para contratos. Playwright CT para Frontend. | ✅ Aceptado |
| **ADR-011** | Local CQRS V1 | CQRS lógico (no físico) sobre PostgreSQL único. Proyecciones DTO, sin ElasticSearch en V1. | ✅ Aceptado |
| **ADR-012** | Integración LLM Agnóstica | Puertos `LlmChatProvider` + `LlmEmbeddingProvider`. **PROHIBICIÓN** de LangChain/Spring-AI. REST raw. | ✅ Aceptado |
| **ADR-013** | Estrategia RAG Dual | `CognitiveMemoryPort` (sesión/efímero) vs `KnowledgeBasePort` (documental/persistente). Tablas separadas en pgvector. | ✅ Aceptado |

---

## 4. Modelo de Datos — Entidades Clave (ERD)

```mermaid
erDiagram
    ibpms_case {
        UUID id PK
        VARCHAR definition_key
        VARCHAR business_key
        VARCHAR status
        JSON payload
        CHAR process_instance_id
        TIMESTAMP created_at
        TIMESTAMP deleted_at
    }

    ibpms_task {
        UUID id PK
        UUID case_id FK
        VARCHAR name
        VARCHAR source_type
        VARCHAR assignee
        CHAR parent_task_id FK
        JSON candidate_groups
        VARCHAR status
        TIMESTAMP due_date
    }

    ibpms_document {
        CHAR id PK
        CHAR case_id FK
        VARCHAR document_type_code
        VARCHAR file_name
        VARCHAR blob_uri
        VARCHAR sha256_hash
        TIMESTAMP retention_end_date
    }

    ibpms_audit_log {
        CHAR id PK
        VARCHAR entity_type
        CHAR entity_id
        VARCHAR event_type
        VARCHAR performed_by
        JSON event_data
        TIMESTAMP created_at
    }

    ibpms_ui_template {
        CHAR id PK
        VARCHAR name
        VARCHAR type
        TEXT raw_code
        VARCHAR version
    }

    sys_role {
        UUID id PK
        VARCHAR name
        VARCHAR type
        VARCHAR process_definition_id
        VARCHAR lane_id
    }

    ibpms_time_logs {
        UUID id PK
        UUID user_id
        VARCHAR reference_id
        ENUM reference_type
        INT duration_minutes
        DATETIME log_timestamp
    }

    ibpms_case ||--o{ ibpms_task : "contiene 1:N"
    ibpms_task ||--o{ ibpms_task : "sub-tareas ad-hoc"
    ibpms_case ||--o{ ibpms_document : "consolida 1:N"
    ibpms_case ||--o{ ibpms_audit_log : "historial Javers"
    ibpms_task ||--o{ ibpms_audit_log : "historial Javers"
    ibpms_case ||--o| ibpms_ui_template : "usa template N:1"
    ibpms_task }o--o{ sys_role : "asignado a grupos"
```

> **Nota crítica de datos:** Hay **2 esquemas separados** en la misma BD PostgreSQL:
> - `ibpms_*` → Tablas de negocio propias (el equipo las controla).
> - `ACT_*` → Tablas nativas de Camunda (NUNCA tocar directamente con SQL).

---

## 5. Topología de Mensajería RabbitMQ

```mermaid
graph LR
    Producer[Backend Spring Boot<br/>Publisher] --> TopicExchange

    subgraph RabbitMQ["RabbitMQ 3 — Topología"]
        TopicExchange[ibpms.exchange.topic<br/>TYPE: Topic]
        DLX[ibpms.exchange.dlx<br/>TYPE: Direct · DLX]
        DLQ[ibpms.dlq.global<br/>TTL: 30 días]

        Q1[ibpms.notifications.email]
        Q2[ibpms.ai.generation]
        Q3[ibpms.integrations.webhook]
        Q4[ibpms.bpmn.events]
        Q5[ibpms.task.rescue]

        TopicExchange --> Q1 & Q2 & Q3 & Q4 & Q5
        Q1 & Q2 & Q3 & Q4 & Q5 -->|"Mensaje envenenado<br/>(max retries)"| DLX
        DLX --> DLQ
    end

    Q1 -->|consume| ConsumerEmail[Email Processor]
    Q2 -->|consume| ConsumerAI[AI NLP Processor]
    Q3 -->|consume| ConsumerWebhook[Webhook Hub]
    Q4 -->|consume| ConsumerBPMN[BPMN Signal Handler]
    Q5 -->|consume| ConsumerRescue[Anti-Cherry-Picking]
```

---

## 6. Estrategia RAG Dual (Inteligencia Artificial)

```mermaid
graph TB
    subgraph Dominio["Capa de Dominio (Ports)"]
        MP[CognitiveMemoryPort<br/>US-056<br/>· ingest · recall · dream · expire]
        KP[KnowledgeBasePort<br/>US-057<br/>· indexDocument · recall · sync · remove]
    end

    EP[EmbeddingProviderPort<br/>US-054 — COMPARTIDO<br/>Azure OpenAI / Gemini / Claude]

    subgraph PgVector["PostgreSQL + pgvector"]
        MV[(ibpms_memory_vectors<br/>Efímero · session_id<br/>Ciclo: Dream → REM → Expiración)]
        KV[(ibpms_knowledge_vectors<br/>Persistente · knowledge_space_id<br/>Ciclo: Versioning + TTL explícito)]
    end

    subgraph LLMContext["Ensamblaje de Contexto para LLM"]
        direction TB
        L1[1. System Prompt inmutable]
        L2[2. Contexto Documental · PRIORIDAD ALTA]
        L3[3. Contexto Conversacional · PRIORIDAD MEDIA]
        L4[4. Memoria Activa últimos N turnos]
        L5[5. User Prompt actual]
    end

    MP <--> EP
    KP <--> EP
    EP --> MV & KV
    KP -->|recall| L2
    MP -->|recall| L3
    L1 --> L2 --> L3 --> L4 --> L5
```

**Regla de priorización:** Si el token budget es insuficiente, el **conocimiento documental tiene prioridad** sobre el historial conversacional.

---

## 7. Patrones y Principios Arquitectónicos

### 7.1 Patrones de Aplicación

| Patrón | Aplicación Concreta en iBPMS |
|--------|------------------------------|
| **Hexagonal / Ports & Adapters** (ADR-001) | Dominio Java puro (POJO sin `@Entity`). Camunda solo existe en la capa de adaptadores. |
| **DDD / Bounded Contexts** | Módulos: Case Management, DMN, SGDEA, TimeTracking, AI Core. |
| **Server-Driven UI** | Backend expone JSON Schema semántico → Frontend Vue 3 renderiza dinámicamente sin recompilación. |
| **Local CQRS** (ADR-011) | Commands: JPA/Spring Data. Queries: Proyecciones DTO + JDBC Template + Materialized Views. |
| **Claim Check** (ADR-004) | Frontend sube archivo → Azure Blob Storage → DB guarda solo `blob_uri + sha256_hash`. |
| **Transactional Outbox** | Spring Modulith Event Publication Registry para publicar a RabbitMQ sin riesgo de pérdida. |
| **Polimorfismo Relacional** (ADR-008) | `ibpms_time_logs.reference_type` ∈ {BPMN, AGILE, GANTT} — un solo módulo de tiempo para todos. |
| **Strangler Fig** | Patrón de migración: APIM como fachada que irá enrutando gradualmente de V1 a V2. |

### 7.2 Patrones de Resiliencia

| Patrón | Estado |
|--------|--------|
| **Circuit Breaker** (Resilience4j) | Implementado para CRM (`slidingWindowSize: 10, failureRateThreshold: 50%`) |
| **Dead Letter Queue (DLQ)** | ✅ Implementado — TTL 30 días, 5 colas de negocio con DLX |
| **Rate Limiting** | Bucket4j implementado como Anti-DoS |
| **Retry con Backoff** | Spring Retry para Camunda Adapter |
| **Feature Flags / Circuit Breaker Manager** | Toggles para modo degradado de CRM (caché Redis) |

### 7.3 Patrones de Seguridad

| Patrón | Implementación |
|--------|----------------|
| **Zero-Trust JWT** | APIM valida en borde. Backend valida autónomamente en `SecurityFilterChain`. |
| **RBAC + ABAC** | JWT liviano (roles) + `SecurityPolicyUseCase` cruza contra matrices relacionales en PostgreSQL. |
| **Auto-generación de Roles BPMN** | Al desplegar un archivo `.bpmn`, un Deployment Hook lee los Lanes y crea roles `BPMN_Proceso_Carril` automáticamente. |
| **Scopes JWT para DevPortal** | Audience: `ibpms.extensibility.supermodules`. `@PreAuthorize` en Spring Boot valida scope. |
| **Inmutabilidad Documental** | `sha256_hash` + `Javers Audit Ledger` — Before/After de cada cambio. Cumplimiento ISO 9001. |
| **Zero Secrets en código** | Azure Key Vault + Managed Identities. API Keys de LLM inyectadas por CI/CD, nunca en código. |

---

## 8. Pirámide de Testing (ADR-010)

```mermaid
graph TB
    subgraph Nivel4["Nivel 4 — E2E / Contratos"]
        T4[REST Assured · BDD-style<br/>Valida JWT malformados, RBAC, flujos HTTP completos]
    end
    subgraph Nivel3["Nivel 3 — Frontend Component Testing"]
        T3[Playwright CT<br/>@playwright/experimental-ct-vue<br/>Renderizado en Chromium/Webkit real]
    end
    subgraph Nivel2["Nivel 2 — Integración"]
        T2[Testcontainers<br/>PostgreSQL 16 + RabbitMQ 3 efímeros<br/>@ServiceConnection · Sin H2]
    end
    subgraph Nivel1["Nivel 1 — Unitarios"]
        T1[JUnit 5 + Vitest<br/>Dominio puro sin DB<br/>jsdom para Vue 3]
    end

    Nivel1 --> Nivel2 --> Nivel3 --> Nivel4
```

> **Regla:** H2 está **prohibido** para integration tests. Todo test de integración debe levantar contenedores Docker reales vía Testcontainers. Requiere Docker Desktop activo.

---

## 9. Hoja de Ruta V1 → V2

```mermaid
timeline
    title Evolución Arquitectónica iBPMS
    section V1 — Táctica (Actual)
        Spring Boot + Camunda 7 embedded : Motor BPM empotrado
        PostgreSQL + pgvector : Una sola VM de datos
        RabbitMQ : Mensajería asíncrona
        Vue 3 + Vite : SPA monolítica
        Local CQRS : Proyecciones sobre misma BD
        iFrame Sandboxing : DevPortal extensibilidad
    section V2 — Cloud-Native (Roadmap)
        AKS Kubernetes + Istio mTLS : Orquestación de contenedores
        Camunda 8 Zeebe / Multi-Agent : Reemplazo del motor BPM
        Apache Kafka : Broker event-driven distribuido
        ElasticSearch CQRS : Read Model dedicado separado
        Neo4j / CosmosDB : Graph DB para RAG avanzado
        Module Federation MFE : Micro-frontends reales
        GraphQL : Query Service para bandejas masivas
```

---

## 10. Interfaces de Usuario (Pantallas) y su Mapa Arquitectónico

Según los documentos, la plataforma tiene al menos **18 pantallas funcionales** identificadas. Las principales y su relación arquitectónica:

| Pantalla | Nombre | Tecnología Backend | Tecnología Frontend |
|----------|--------|--------------------|---------------------|
| **P0** | Inicio / Catálogo de Procesos | Start Event Camunda | Vue Router |
| **P1** | Bandeja Unificada (Workdesk) | `CaseManagement UseCase` + CQRS Queries | Vue 3 + Pinia |
| **P2** | Formulario Dinámico de Tarea | JSON Schema → `ibpms_ui_template` | `<FormRenderer/>` + Zod |
| **P3** | Tablero Kanban Ágil | JPA State Machine (sin CMMN) | vuedraggable |
| **P4** | Taller de Reglas IA (DMN) | `ibpms-dmn-engine` + Camunda DMN | dmn-js + Monaco Editor |
| **P5** | Rastreador de Casos | CQRS Read + `ibpms_audit_log` | Vue 3 |
| **P6** | Modelador BPMN | Deploy Hook auto-roles | bpmn-js |
| **P7** | iForm Builder (Low-Code) | `ibpms_ui_template` | Monaco Editor + Vue Render |
| **P8** | Template Builder (Gantt/WBS) | frappe-gantt backend | frappe-gantt |
| **P11** | Hub de Integraciones | Webhook adapter + RabbitMQ | Vue 3 |
| **P12** | SGDEA / Bóveda Documental | Azure Blob + `ibpms_document` | Vue 3 multipart |
| **P13** | DevPortal | iFrame sandbox + JWT scopes | iframe + postMessage |
| **P14** | Gestión de Usuarios y Roles | `sys_role` + Auto-generación BPMN | Vue 3 |
| **P18** | Portal B2B Clientes | RLS Seguro | Vue 3 separado |

---

## 11. Stack Tecnológico Completo

```mermaid
graph LR
    subgraph Backend
        J[Java 17]
        SB[Spring Boot 3.2.3]
        C7[Camunda BPM 7.21.0]
        DMN[Camunda DMN 7.20.0]
        JWT[JJWT 0.12.5]
        LIQ[Liquibase]
        MS[MapStruct 1.5.5]
        LOK[Lombok 1.18.30]
        RES[Resilience4j 3.1.1]
        JAV[Javers 7.3.7]
        B4J[Bucket4j 8.9.0]
        PDF[PDFBox 3.0.1 + OpenPDF 1.3.36]
        AZ[Azure Storage SDK 12.25.1]
        PGV[pgvector 0.1.5]
        MAV[Maven 3.9.x]
    end

    subgraph Frontend
        V3[Vue 3.4.x]
        VI[Vite 5.1.x]
        TS[TypeScript 5.2.x]
        PIN[Pinia 2.x]
        VR[Vue Router 4.x]
        TW[TailwindCSS 3.4.x]
        AX[Axios 1.6.x]
        ZD[Zod 3.22.x]
        BJ[bpmn-js 18.12.x]
        MO[Monaco Editor 0.55.x]
        STOMP[STOMP.js 7.3.x]
        PW[Playwright 1.59.x]
        VT[Vitest 1.4.x]
        ND[Node.js 20 LTS]
    end

    subgraph Infra
        PG[(PostgreSQL 15+ pgvector)]
        RMQ[(RabbitMQ 3)]
        RED[(Redis 7)]
        DOC[Docker Desktop]
    end

    subgraph RPA
        PY[Python 3.11]
        REQ[requests 2.31.0]
        BS4[beautifulsoup4 4.12.3]
    end
```

---

## 12. Resumen Ejecutivo en 5 Líneas

1. **iBPMS es un BPM empresarial** con orquestación BPMN/DMN (Camunda 7), gestión de expedientes, formularios dinámicos, Kanban ágil, SGDEA y capacidades de IA (RAG dual con pgvector).

2. **La arquitectura base es Hexagonal + DDD** (ADR-001): el dominio Java puro nunca importa frameworks. Camunda, JPA, Redis y los servicios externos son meros adaptadores reemplazables.

3. **La infraestructura V1 es austera y deliberada**: un monolito Spring Boot sobre una VM Azure, una PostgreSQL que sirve simultáneamente como BD relacional, vectorial (pgvector) y almacén de estado Camunda. RabbitMQ para async. Redis para locks y caché.

4. **El Frontend es Server-Driven UI**: el backend expone JSON Schemas semánticos que Vue 3 renderiza dinámicamente sin recompilación, habilitando formularios cambiantes sin nuevos despliegues.

5. **La V2 es una evolución, no una reescritura**: el Patrón Strangler (APIM como fachada), la Arquitectura Hexagonal y la separación de esquemas de datos `ibpms_*` vs `ACT_*` permiten migrar gradualmente a Kubernetes + Kafka + Multi-Agent sin romper el dominio de negocio construido en V1.
