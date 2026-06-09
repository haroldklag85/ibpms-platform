---
title: "Handoff Backend - US-036 Remediación (Iteración 09-DEV-REMEDIATION)"
agent: "Backend"
branch: "DevDavid"
us: "US-036"
cas: "CA-14, CA-16, CA-17, CA-20b, CA-21, CA-23, CA-24, CA-25"
---

# 1. Metadatos de la Delegación
*   **Rol Asignado:** Backend
*   **Rama de Trabajo:** `DevDavid`
*   **US Objetivo:** US-036 (Identity Governance)
*   **Criterios a Validar:** CA-14, CA-16, CA-17, CA-20b, CA-21, CA-23, CA-24, CA-25.
*   **Alineación Arquitectónica:** Cumplimiento de ADR-001 (Hexagonal), CQRS (ADR-011) y Zero-Trust.

# 2. Contexto Técnico
Debemos remediar las brechas detectadas durante la auditoría.
- **CA-14, CA-21, CA-25 (Kill-Session y Blacklist Redis):** `JwtAuthFilter` y `JwtBlacklistService` deben compartir la infraestructura de Redis (con `spring-data-redis` asíncrono o de baja latencia < 5ms). Se debe erradicar el HashMap dummy y evitar duplicación de filtros entre US-036 y US-038.
- **CA-16, CA-24 (Auditoría CISO ISO 27001):** Implementar endpoint `POST /api/v1/security/audit/reports` que genere el reporte CSV bajo demanda, calcule su SHA-256 y lo persista en `ibpms_audit_reports` (creada por Infraestructura).
- **CA-17 (Traza Indeleble):** `UserService.updateUser` y los servicios de asignación de roles deben escribir explícitamente en la tabla de auditoría (`ibpms_audit_log`) incluyendo quién otorgó el permiso y el timestamp.
- **CA-20b:** Integración global de `DataSegregationService` para la unión multirrol.
- **CA-23 (Delegación In-Flight):** Descomentar `revertAssignee()` e inyectar repositorio/log de auditoría en `TaskDelegationService`.

# 3. Entregables Esperados
1.  **Refactor de Seguridad:** `JwtBlacklistService` conectado a Redis. Remoción de filtros redundantes.
2.  **Reportes de Auditoría:** Endpoint REST expuesto, lógica en la capa de Aplicación (`ApplicationService`), y capa de Infraestructura (`JpaRepository` para `AuditReportEntity`).
3.  **Trazabilidad y Delegación:** Inyección de auditoría en `UserService` y remediación del `TaskDelegationService`.
4.  **Pruebas Unitarias:** Cobertura de las correcciones en la capa correspondiente (Mínimo un Unit Test por servicio tocado).

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

# 4. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
