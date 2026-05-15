---
title: "Handoff Infraestructura/BD - US-038 (CA-06 al CA-12)"
role: "Infra/BD"
epic: "US-038 - Asignación Multi-Rol y Sincronización EntraID"
iteration: "02-DEV-038-DAVID"
branch: "DevDavid"
---

# Handoff Arquitectónico: Infraestructura/BD

## 1. Contexto y Objetivos
El objetivo de esta iteración es proveer la infraestructura de mensajería (RabbitMQ) y el esquema de base de datos (PostgreSQL/Liquibase) para soportar la delegación de roles, auditoría de segregación de funciones (SoD) y el tablero de anomalías de seguridad correspondientes a la US-038.

**Exclusiones:** El CA-09 ("Distributed Tracing V2 Ready") ha sido EXCLUIDO por referenciar explícitamente a arquitecturas futuras (V2). No se requiere infraestructura para este CA.

## 2. Alineación Arquitectónica
* **ADR-009 (PostgreSQL):** Todos los cambios de esquema DEBEN realizarse mediante Liquibase (Changelogs), respetando los tipos UUID nativos y auditoría base.
* **ADR-001 (Hexagonal):** La infraestructura debe quedar desacoplada; RabbitMQ debe configurarse con Dead Letter Queues (DLQ) para tolerancia a fallos.
* **RabbitMQ Topology:** Se debe seguir el estándar de colas y exchanges ya establecido en iteraciones previas (US-034).

## 3. Requerimientos Técnicos (Entregables)

### 3.1 Esquema DDL (Liquibase)
Deberás crear un nuevo changeset (ej. `49-us038-delegations-anomalies.sql`) en `db.changelog-master.yaml` con:
* **Tabla `ibpms_security_delegation` (CA-07):** 
  - `id` (UUID, PK)
  - `delegator_user_id` (UUID, FK a tabla usuarios)
  - `delegate_user_id` (UUID, FK a tabla usuarios)
  - `start_date` (TIMESTAMP)
  - `end_date` (TIMESTAMP)
* **Tabla `ibpms_security_anomaly` (CA-06, CA-12):**
  - `id` (UUID, PK)
  - `anomaly_type` (VARCHAR) -> Ej: "SoD_VIOLATION", "BREAK_GLASS_USED"
  - `description` (TEXT)
  - `is_resolved` (BOOLEAN DEFAULT false)
  - `resolved_at` (TIMESTAMP)
  - `resolved_by` (UUID)

### 3.2 Topología RabbitMQ (CA-07 y CA-08)
Deberás asegurar la existencia de (o crear si no existen en el script de RabbitMQ inicial):
* **Exchange:** `ibpms.security.exchange` (Topic o Direct)
* **Queue:** `camunda.task.unclaim.queue`
* **Routing Keys:** `security.user.delegated`, `security.user.deactivated`
* **DLQ:** Las colas deben tener configurada su política de DLX (Dead Letter Exchange) y reintentos automáticos para evitar pérdida de eventos de exorcismo de tareas.

## 4. Criterios de Aceptación a Soportar
* **CA-06 y CA-12:** Soporte DDL para las anomalías.
* **CA-07 y CA-08:** Soporte de esquema de delegación y mensajería para Exorcismo de Tareas.

## 5. Exclusiones y Limitaciones
* **CA-09:** Excluido. No generar infraestructura de trace-id transversal para V2.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.

---
**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
