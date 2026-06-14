# Handoff Infra/BD: US-036 (CA-23 al CA-28)

## 1. Metadatos
- **Iteración:** 07-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-23, CA-24, CA-25, CA-26, CA-27, CA-28
- **Rama Git:** DevDavid
- **Estrategia NFR/QA:** desarrollar sobre la arquitectura en la ruta "C:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\docs\architecture\arquitecturar.md"

### Alineación Arquitectónica
- **ADRs Consultados:** ADR-009 (PostgreSQL + Liquibase).
- **Stack:** PostgreSQL, Liquibase.
- **Riesgos:** Asegurar idempotencia en los scripts de Liquibase (uso de `IF NOT EXISTS`) para evitar bloqueos como en iteraciones pasadas.

## 2. Contexto de Negocio
Implementación final de las reglas de gobernanza y topología visual de US-036. Requiere persistencia para los reportes ISO 27001 (CA-24).

## 3. Requerimientos Técnicos
1. **Validar/Crear tabla `ibpms_audit_reports` (CA-24):**
   - Asegurar que exista un Liquibase changeset para la tabla `ibpms_audit_reports`.
   - Columnas sugeridas: `id` (UUID/BigInt), `requested_by` (String/FK), `generated_at` (Timestamp), `content_hash_sha256` (String), `file_url` o `report_data` (Text/JSONB).
2. **Validar tabla de delegaciones (CA-23):**
   - Asegurar que la tabla que soporta delegaciones (ej. `ibpms_role_delegations`) tenga las columnas `start_date` y `end_date`.

## 4. Criterios de Aceptación a Cubrir
- **CA-24:** Persistencia de reporte en `ibpms_audit_reports`.

## 5. Dependencias y Bloqueantes
- Ninguna. Infra arranca primero.

## 6. Validaciones y Entregables
- **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato. 

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
