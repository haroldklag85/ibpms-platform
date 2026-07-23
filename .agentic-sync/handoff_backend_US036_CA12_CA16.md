# Handoff Técnico - Backend - US-036 (CA-12 al CA-16)

## 1. Contexto y Objetivos
Implementación de los controles avanzados de gobernanza de identidad (Kill-Session), exposición pública de trámites y generación de reportería de cumplimiento ISO 27001. Esta fase cierra la lógica de control administrativo de la Pantalla 14.

**Rama de trabajo:** `DevDavid`
**Iteración:** `05-DEV-DAVID`

## 2. Alineación Arquitectónica
- **ADR-001 (Hexagonal):** La lógica de revocación de sesiones y generación de reportes DEBE residir en la capa `domain/`.
- **ADR-011 (CQRS):** La generación de la "Sábana" de auditoría (CA-16) debe optimizarse como una consulta de lectura (Read Model).
- **Remediación CA-21:** Implementar `POST /api/v1/admin/users/{userId}/revoke-session` usando Redis como blacklist.
- **Remediación CA-24:** Persistencia de metadatos de reportes en `ibpms_audit_reports`.

## 3. Especificaciones Técnicas (Criterios de Aceptación)

### CA-14: Kill-Session (Remediación CA-21)
- **Endpoint:** `POST /api/v1/admin/security/users/{userId}/revoke-session`
- **Lógica:** 
    - Identificar el `jti` (JWT ID) activo del usuario.
    - Insertar en Redis Blacklist con el TTL remanente del token.
    - El `SecurityFilter` debe consultar esta blacklist en cada request.
    - **Dependencia:** Coordinar con el patrón de US-038 (Fail-Open Policy).

### CA-15: Trámite Público
- **Atributo:** Añadir `is_public` (boolean) a la configuración de definiciones de proceso.
- **Seguridad:** Modificar el `SecurityConfig` para permitir acceso `permitAll()` a rutas `/api/v1/public/process/{processId}/start` si el flag es `true`.
- **BPMN:** Asegurar que el inicio de instancia funcione sin `AuthenticatedUser` contextual.

### CA-16: Reporte ISO 27001 (Remediación CA-24)
- **Endpoint:** `GET /api/v1/admin/security/reports/iso-27001`
- **Formato:** CSV o Excel (XLSX).
- **Columnas Requeridas:** Usuario/Robot, Roles Asignados, Procesos Iniciables, Procesos Ejecutables, Fecha de Corte.
- **Integridad:** Incluir Hash SHA-256 de los datos en los metadatos del reporte.
- **Persistencia:** Guardar registro de la generación en la tabla `ibpms_audit_reports`.

### CA-13: Roles Dinámicos
- Integrar la resolución de permisos para que acepte `Lanes Expression` de Camunda.
- La lógica de autorización debe realizar la unión (OR) entre Roles Estáticos (DB) y Roles Dinámicos (BPMN context).

## 4. Modelo de Datos (Liquibase)
- **[NEW]** `36-us036-ca12-ca16-reports.sql`:
    - Tabla `ibpms_audit_reports`: `id`, `report_type`, `generated_by`, `generated_at`, `file_hash`, `metadata_json`.
    - Columna `is_public` en `ibpms_process_definitions`.

## 5. Pruebas y Validación
- **Unitarias:** Validar el cálculo de la matriz de permisos cruzada (User x Role x Process).
- **Integración:** Probar el bloqueo inmediato de un token tras el Kill-Session usando una instancia de Redis (Docker).

## 6. Instrucciones Operativas
Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2).

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
> 3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_backend.md`.
> 4. Detente y notifica al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder..."*
> 5. Aplica **TDD** (`.agents/skills/tdd_first/SKILL.md`) y **Clean Code** (`.agents/skills/clean_code_standards/SKILL.md`).
