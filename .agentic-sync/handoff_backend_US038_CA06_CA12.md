---
title: "Handoff Backend - US-038 (CA-06 al CA-12)"
role: "Backend"
epic: "US-038 - Asignación Multi-Rol y Sincronización EntraID"
iteration: "02-DEV-038-DAVID"
branch: "DevDavid"
---

# Handoff Arquitectónico: Backend

## 1. Contexto y Objetivos
El objetivo de esta iteración es implementar la lógica de negocio para la delegación de permisos (con fechas estrictas), auditoría y bloqueo de Segregación de Funciones (SoD) (ej. no aprobar tareas creadas por uno mismo), y exponer la API del Tablero de Anomalías.

**Exclusiones:** El CA-09 ("Distributed Tracing V2 Ready") ha sido EXCLUIDO. No implementar la propagación de `Correlation-ID` en esta fase.

## 2. Alineación Arquitectónica
* **ADR-001 (Hexagonal Architecture):** La lógica de validación de SoD y delegación DEBE residir en la capa `domain/`. Los controladores Web solo enrutarán y RabbitMQ publishers pertenecerán a `infrastructure/`.
* **ADR-010 (Testing Pyramid):** Se exige la creación de tests unitarios que prueben el bloqueo de SoD y la publicación de eventos en RabbitMQ.

## 3. Requerimientos Técnicos (Entregables)

### 3.1 Detección de Segregación de Funciones (CA-06)
* Modificar el servicio de aprobación de tareas en `domain/` para interceptar la transacción: `Creator_ID != Approver_ID`.
* Si un usuario intenta aprobar algo que creó, lanzar una excepción de negocio (`SoDViolationException`).
* Persistir el intento como una Alerta Roja asíncrona en la tabla `ibpms_security_anomaly`.

### 3.2 Proxy Temporal y Exorcismo (CA-07 y CA-08)
* Endpoint `POST /api/v1/security/delegations` para recibir `delegate_id`, `start_date`, y `end_date`.
* Publicar un evento asíncrono hacia el Message Broker (`ibpms.security.exchange` / `security.user.delegated`) para realizar un "Auto-Unclaim Masivo" en Camunda.
* Endpoint para despido/revocación de usuarios que emita un evento con routing key `security.user.deactivated` para lograr el "Exorcismo de tareas".

### 3.3 API del Tablero de Anomalías (CA-12)
* Endpoint `GET /api/v1/security/anomalies` que retorne la lista de anomalías no resueltas.
* Endpoint `PUT /api/v1/security/anomalies/{id}/resolve` para marcar una incidencia como solucionada (`is_resolved = true`, `resolved_at = NOW()`).

## 4. Criterios de Aceptación a Soportar
* **CA-06:** Bloqueo transaccional de Juez y Parte + Registro de anomalía.
* **CA-07:** API de delegación + Publicación RabbitMQ (Guaranteed Delivery).
* **CA-08:** Exorcismo por despido + Publicación RabbitMQ.
* **CA-12:** APIs para listar y resolver anomalías.

## 5. Exclusiones
* **CA-09:** Excluido.
* **CA-10 y CA-11:** Exclusivos de Frontend.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---
**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
