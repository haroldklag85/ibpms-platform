# 📋 SOLICITUD DE REVISIÓN — Agente QA → Arquitecto Líder
## US-034 / CA-4 a CA-10: Plan de Certificación de Resiliencia RabbitMQ

| Metadato | Valor |
|---|---|
| **Emisor** | Agente QA Senior |
| **Destino** | Arquitecto Líder (Orquestador) |
| **Fecha** | 2026-04-05 |
| **Iteración** | 70-DEV |
| **Rama** | `sprint-3/informe_auditoriaSprint1y2` |
| **Tipo** | Solicitud de Green Light para Ejecución de Tests |

---

## 1. Resumen Ejecutivo

He completado la Fase de Investigación Forense sobre los entregables del Backend y Frontend de la US-034. He analizado minuciosamente **14 archivos fuente** y **1 migration script Liquibase** para construir el plan de tests.

### Veredicto Preliminar

| Criterio | Artefactos | Estado |
|---|---|---|
| CA-4: Topología | `RabbitMqTopologyConfig.java`, `rabbitmq_topology.md` | ✅ CONFORME |
| CA-5: Idempotencia | `IdempotencyGuard.java`, `ProcessedMessageEntity.java`, Liquibase | ✅ CONFORME |
| CA-6: Prioridades | `MessagePriority.java` | ⚠️ Enum definido pero NO USADO en producers |
| CA-7: Retry Backoff | `AmqpConfig.java`, `TaskRescueConsumer.java` | ⚠️ Sin clasificación de excepciones |
| CA-8: DLQ Admin BE | `DlqAdminController.java` | 🔴 Sin `@PreAuthorize`, sin `audit_log` |
| CA-8: DLQ Dashboard FE | `DlqDashboard.vue` | ✅ Refactorizado a API real, modales presentes |
| CA-9: TTL/Archivado | `MqMaintenanceJob.java`, Liquibase | ✅ CONFORME |
| CA-10: Health Check | `RabbitHealthIndicator.java`, Liquibase | ✅ CONFORME |

---

## 2. Defectos Críticos Identificados

| ID | Severidad | Descripción |
|---|---|---|
| DEF-02 | 🔴 CRÍTICA | `DlqAdminController` carece de `@PreAuthorize("hasRole('ADMIN_IT')")`. |
| DEF-03 | 🔴 CRÍTICA | Endpoints DLQ no registran en `ibpms_audit_log`. Handoff lo exige explícitamente. |
| DEF-01 | 🟡 MEDIA | `MessagePriority` no se inyecta en ningún `MessagePostProcessor` de los producers. |
| DEF-04 | 🟡 MEDIA | No hay routing directo a DLQ para `ValidationException`. |

---

## 3. Preguntas que Requieren su Dictamen

### OQ-1 (Bloqueante)
> ¿Los defectos DEF-02 y DEF-03 bloquean la certificación de CA-8 Backend? ¿Debo devolver handoff al equipo Backend o certificar como "Deuda Técnica Conocida"?

### OQ-2
> ¿El defecto DEF-04 (sin clasificación de excepciones para routing directo a DLQ) es aceptable como limitación V1 del CA-7?

### OQ-3 (Técnica)
> Propongo tests unitarios puros con Mockito para los componentes RabbitMQ (no hay Testcontainers RabbitMQ en el proyecto). ¿Aprobado?

---

## 4. Plan de Tests Propuesto

- **7 clases de test Java** (1 por cada CA del Backend)
- **5 test cases Vitest** para el Frontend DlqDashboard
- **Total: ~25-30 assertions**
- Ver detalle completo en `implementation_plan.md` del agente QA.

---

## 5. Acción Requerida

> **Necesito su veredicto formal (Green Light / Red Light) para proceder a la fase de EXECUTION.**

**Firma:** Agente QA Senior — Iteración 70-DEV
