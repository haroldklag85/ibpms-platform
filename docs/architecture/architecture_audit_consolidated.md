# Auditoría Consolidada de Arquitectura: Brechas, Patrones y Alineación C4 (iBPMS)

> [!CAUTION]
> **HALLAZGOS GRADUADOS AL SSOT:** Las brechas y decisiones de diseño consolidadas en este documento de auditoría arquitectónica fueron solventadas y graduadas como Criterios de Aceptación formales en los archivos de Épica correspondientes (`docs/requirements/epics/`). Este archivo es ahora histórico y de solo lectura.

> **Origen:** Este documento consolida `architecture_review.md` y `c4_audit_report.md` (eliminados tras la fusión).
> **Última Auditoría:** 2026-04-09 — Saneamiento arquitectónico integral.

---

## 1. Patrones de Arquitectura Validados

### Arquitectura Hexagonal (Puertos y Adaptadores)
*   **Definición:** El dominio central (lógica, casos de uso) se aísla de infraestructuras externas (UI, DB, APIs, motores BPM). Todo entra y sale mediante "Puertos" (interfaces) y "Adaptadores".
*   **Aplicación iBPMS:** En aplicaciones core duraderas, la lógica comercial (`Expediente`, `SLAs`) sobrevive a múltiples cambios de tecnología.
*   **Beneficios:** Mantenibilidad extrema (unit-test sin DB/framework), desacoplamiento total contra vendor lock-in.

### Domain-Driven Design (DDD)
*   **Definición:** Modelado basado en lenguaje ubicuo del dominio real ("Aprobar Expediente" en lugar de `updateRow("estado", 1)`).
*   **Aplicación iBPMS:** Bounded Contexts por módulo funcional (Case Management, DMN, SGDEA).

### Command Query Responsibility Segregation (CQRS)
*   **Definición:** Separación del modelo de escritura (Comandos) del modelo de lectura (Consultas).
*   **Estado V1:** CQRS local (ADR-011) — misma base PostgreSQL con proyecciones ligeras en la capa de servicio. No hay Read Model separado.
*   **Estado V2 (Roadmap):** CQRS distribuido con ElasticSearch como Read Model dedicado.

### Micro-Frontends (MFE)
*   **Aplicación iBPMS:** Formularios desacoplados vía Server-Driven UI (JSON Schema) renderizados por Vue 3 (ADR-002).

---

## 2. Gap Analysis por Categoría

### A. Patrones de Arquitectura de Aplicaciones

| # | Brecha | Estado | Solución / Comentario |
|---|--------|--------|----------------------|
| A-1 | **Idempotencia en APIs REST** | ✅ CERRADO | Idempotency-Keys obligatorias en REST Controllers. Documentado en `implementation_plan.md` L290. |
| A-2 | **Consistencia Eventual (CQRS)** | ⚠️ MITIGADO | Diseño optimista en Pinia (Vue 3). Polling/SSE como alternativa futura. Sin CQRS distribuido en V1. |

### B. Patrones de Integración

| # | Brecha | Estado | Solución / Comentario |
|---|--------|--------|----------------------|
| B-1 | **Dead Letter Queue (DLQ)** | ✅ CERRADO | DLQs implementadas en `rabbitmq_topology.md`. Topología validada con Testcontainers. |
| B-2 | **Service Mesh (mTLS)** | ⏳ V2 | Diferido a V2 (Kubernetes + Istio/Linkerd). En V1 TLS 1.2+ intra-VNet es suficiente. |

### C. Patrones de Datos

| # | Brecha | Estado | Solución / Comentario |
|---|--------|--------|----------------------|
| C-1 | **Data Archiving (TTL)** | ✅ CERRADO | Documentado en `implementation_plan.md` L91 y L94. Table Partitioning por rango de fechas en PostgreSQL. Cold Storage +90 días. |
| C-2 | **Transactional Outbox** | ✅ CERRADO | Implementado en V1 nativamente mediante el **Event Publication Registry de Spring Modulith**, garantizando envíos a RabbitMQ sin Debezium ni overhead externo. |

### D. Patrones de Resiliencia

| # | Brecha | Estado | Solución / Comentario |
|---|--------|--------|----------------------|
| D-1 | **Circuit Breaker & Bulkhead** | ⚠️ DOCUMENTADO | Prescrito como Resilience4j en el plan. Falta ADR dedicado con configuración de thresholds/timeouts/fallbacks. |
| D-2 | **Validación Empírica (MQ, Storage)** | ✅ CERRADO (i70-DEV) | Testcontainers (PostgreSQL + RabbitMQ) estandarizado. Evidencia: `TestcontainersBaseIT.java`. |

### E. Patrones de Seguridad

| # | Brecha | Estado | Solución / Comentario |
|---|--------|--------|----------------------|
| E-1 | **Inyección de Secretos** | ✅ CERRADO | Azure Key Vault + Managed Identities implementado (L70-71 del plan). |
| E-2 | **mTLS Interno** | ⏳ V2 | Diferido a V2 con Service Mesh. TLS 1.2+ intra-VNet en V1. |

### F. Patrones Cloud / Infraestructura

| # | Brecha | Estado | Solución / Comentario |
|---|--------|--------|----------------------|
| F-1 | **Infrastructure-as-Code** | ✅ CERRADO | Terraform/Bicep obligatorio. VMSS para producción (L79 del plan). |
| F-2 | **Elasticidad Horizontal** | ✅ CERRADO | VMSS + Azure Monitor configurados. Evolución a AKS en V2. |

---

## 3. Auditoría de Alineación: Escenarios de Negocio vs Diagramas C4

### Escenario 1: Triggers por Correo Institucional (O365)
| Nivel C4 | Estado | Detalle |
|----------|--------|---------|
| L1 Contexto | ✅ OK | Actor `Microsoft 365 / Exchange` modelado con Webhook. |
| L2 Contenedores | ✅ OK | Inbound Listener, Event Broker (RabbitMQ V1), Azure Storage Disks. |
| L3 Componentes | ⚠️ GAP | Falta adapter del extractor de adjuntos (Graph API connector) en diseño L3. El L3 solo modela la recepción del evento RabbitMQ. |

### Escenario 2: Case Management & Kanban
| Nivel C4 | Estado | Detalle |
|----------|--------|---------|
| L1 Contexto | ✅ OK | Usuario de Negocio monitorea flujos. |
| L2 Contenedores | ✅ OK (V1) | Una sola PostgreSQL para V1 — CQRS distribuido diferido a V2. |
| L3 Componentes | ✅ OK | `CaseManagement UseCase` y entidad `Expediente`. |

### Escenario 3: Motor de Reglas (DMN)
| Nivel C4 | Estado | Detalle |
|----------|--------|---------|
| L1 Contexto | ✅ OK | Actor Admin configura DMN. |
| L2 Contenedores | ✅ OK | DMN vía Camunda 7 embebido (`.jar`). |
| L3 Componentes | ⚠️ GAP | Falta `DmnEvaluationAdapter` como Driven Adapter en L3. El DMN se evalúa vía la API Java interna de Camunda 7, pero esto no está explícito. |

### Escenario 4: Asignación Dinámica (RBAC/ABAC)
| Nivel C4 | Estado | Detalle |
|----------|--------|---------|
| L1 Contexto | ✅ OK | IdP modelado. |
| L2 Contenedores | ✅ OK | APIM inyecta JWT, Backend valida autónomamente (Zero-Trust). |
| L3 Componentes | ✅ OK | `SecurityPolicyUseCase` modelado en C4 V1 actualizado. Auto-generación de roles BPMN documentada. |

### Escenario 5: Integración con ERP (Saga)
| Nivel C4 | Estado | Detalle |
|----------|--------|---------|
| L1 Contexto | ✅ OK | ERP / Core Bancario modelado. |
| L2 Contenedores | ⚠️ GAP | La flecha de Saga sale del Broker pero no toca un Outbound Connector explícito en L2. |
| L3 Componentes | ✅ OK | `ERP Connector (Feign Client)` modelado. |

### Escenario 6: Gestión Documental (SGDEA)
| Nivel C4 | Estado | Detalle |
|----------|--------|---------|
| L1 Contexto | ✅ OK | SharePoint opcional modelado. |
| L2 Contenedores | ✅ OK | Módulo SGDEA nativo + Bóveda Blobs. |
| L3 Componentes | ⚠️ GAP | Falta `SgdeaOutboundAdapter` en L3 del Backend Core. Si avanza un proceso y necesita leer/guardar un expediente al SGDEA, no hay adaptador CMIS/REST modelado. |

---

## 4. Preguntas Resueltas (Historial)

| # | Pregunta | Respuesta / ADR |
|---|----------|-----------------|
| Q1 | ¿Camunda 7 vs 8? | **Camunda 7 Embebido** — ADR-003. PostgreSQL como backend de estado + negocio. |
| Q2 | ¿DMN síncrono o empotrado? | **Empotrado** — Camunda 7 `.jar` incluye motor DMN nativo. Sin DaaS Container separado en V1. |
| Q3 | ¿ABAC Frontend o Backend? | **Backend** — `SecurityPolicyUseCase` con Zero-Trust autónomo. Frontend no filtra seguridad. |
| Q4 | ¿SGDEA PoC? | **ADR-004** — Almacenamiento local en Managed Disks con metadata en PostgreSQL. Adaptador CMIS formal diferido. |

---

## 5. Gaps C4 Pendientes de Resolución

> [!WARNING]
> Los siguientes gaps del modelo C4 requieren actualizaciones al diagrama L3 para completar la trazabilidad:

| ID | Gap | Componente Faltante en C4 L3 | Prioridad |
|----|-----|------------------------------|-----------|
| GAP-L3-01 | Graph API Connector para extracción de adjuntos O365 | `GraphApiOutboundAdapter` | 🟡 Sprint actual |
| GAP-L3-02 | DMN Evaluation port del engine Camunda 7 | `DmnEvaluationAdapter` (Java API interna) | 🟢 Próximo sprint |
| GAP-L3-03 | Conector al SGDEA desde Backend Core | `SgdeaOutboundAdapter` | 🟡 Sprint actual |
| GAP-L2-01 | Outbound Connector explícito para Sagas ERP | Flujo visual Broker → Outbound → ERP | 🟡 Sprint actual |

---

## 6. Conclusión

La arquitectura iBPMS V1 **es robusta y escalable**. El Patrón Strangler facilita la transición a SaaS Multitenant. La combinación DDD + Hexagonal sobre Spring Boot blinda contra vendor lock-in.

**Brechas cerradas (9/12):** Idempotencia, DLQ, Data Archiving, Testcontainers, Key Vault, IaC, VMSS, RBAC/ABAC, Transactional Outbox.
**Brechas diferidas a V2 (2/12):** mTLS, CQRS distribuido.
**Brechas pendientes de documentación (1/12):** Circuit Breaker (ADR faltante).

---

## 7. Registro de Decisiones Pendientes Resueltas (Sesión 2026-04-08)

| # | Pregunta Abierta | Decisión | Artefacto Impactado |
|---|-----------------|----------|---------------------|
| P1 | ADR-004 faltante (salto en secuencia 003→005) | **Renumeración completa:** ADR-005→ADR-004, ADR-006→ADR-005, ..., ADR-011→ADR-010. Trazabilidad preservada. | Todos los archivos `adr_00X_*.md` renombrados + contenido interno actualizado. |
| P2 | CQRS Local vs Distribuido | **Local CQRS para V1** formalizado. Proyecciones/DTOs sobre PostgreSQL único. ElasticSearch diferido a V2. | Nuevo `adr_011_local_cqrs_v1.md` |
| P3 | Transactional Outbox: implementar en V1 o diferir a V2 | **Implementar en V1** mediante **Spring Modulith Event Publication Registry**. Sin Debezium ni overhead externo. | `implementation_plan.md` L157, esta auditoría C-2. |
| P4 | Pureza Hexagonal del Kanban (ADR-007) | **POJOs puros obligatorios** en la capa de Dominio. Entidades JPA restringidas a la capa de Infraestructura/Persistencia. MapStruct para conversión bidireccional. | `adr_007_cmmn_vs_kanban.md` Sección 4, punto 2. |
| P5 | Purgar Kafka de V1 | **Verificado: 0 menciones erróneas.** Todas las referencias a Kafka están correctamente acotadas como "V2 / futuro" o como ejemplo de prohibición (ADR-001). | `implementation_plan.md`, `c4-model.md` (limpios). |

---

## 8. Certificación Final Iteración 3 (Sprint 6.2)

> **Veredicto QA:** GO Oficial UAT (Aprobado). Ejecución de la suite completa de 53 escenarios.

| Área Validada | Veredicto | Detalle Arquitectónico |
|---------------|-----------|------------------------|
| **(US-039) Security Framework** | ✅ APROBADO | Interceptor de Opacidad **CA-3** validado y ajustado (Soporte Multi-Role en `Vue Router`). Rutas protegidas rutean a falso 404 para evasión y ceguera de pentesting. Funciones Core de Auth y JWT rotación activas. |
| **(US-003) Garbage Collector** | ✅ APROBADO | Reparado deshidratador de estado local. `LocalStorageGarbageCollector` procesa metadatos `_timestamp` para purga de drafts stales > 7 días en Board Frontend. |
| **Integración SSE (CORS)** | ✅ APROBADO | Adaptación Zero-Trust: Refactorizados endpoints de EventSource en Store y Copilotos DMN/BPMN para enrutar a proxies relativos (`/api/v1/*`), eludiendo explícitamente barreras CORS no configuradas. |

**Hito:** Con la estabilización final de la pantalla de *Identity Governance* (Pantalla 14), la iteración técnica queda formalmente finalizada habilitando el Handoff al Release UAT.
