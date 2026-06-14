# Handoff Arquitectónico: Infraestructura / Base de Datos
**Iteración:** 01-DEV-038-DAVID
**Épica:** 13 — Seguridad/RBAC (US-038)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-038 establece el motor multi-rol y la sincronización con EntraID. Para esta fase de infraestructura, es crítico garantizar la resiliencia de la Caché (Redis) que soporta el Kill-Switch (Lista Negra de Tokens) y asegurar que cualquier log o auditoría generada por el Break-Glass Protocol (CA-04) tenga soporte en la base de datos si requiere nuevas tablas o columnas en el Audit Ledger.

## 2. Alineación Arquitectónica
- **ADR-001 y ADR-009:** La infraestructura de Redis debe estar configurada en el `docker-compose.yml` para tolerar caídas simuladas.
- Las tablas del Audit Ledger deben poder registrar los accesos de emergencia.

## 3. Requisitos Técnicos y Entregables (Infra/BD)

**A. Verificación Redis Fail-Open (CA-01):**
- Validar que el entorno local (`docker-compose.yml`) despliega Redis correctamente. No se requieren cambios estructurales si ya existe, pero debes confirmarlo.

**B. Soporte DDL para Break-Glass (CA-04) y JIT (CA-03):**
- Si el JIT Provisioning requiere almacenar los Claims faltantes (Ej. `Sucursal_ID`, `Codigo_Jefe`), asegúrate de que la tabla `ibpms_service_accounts` o `ibpms_users` posea soporte para JSONB o columnas específicas para estos metadatos. De ser necesario, crea un changeset Liquibase `48-us038-user-metadata.sql`.

## 4. Criterios de Aceptación a Validar
- CA-01: Infraestructura Redis operativa.
- CA-03 / CA-04: Esquema de base de datos preparado para JIT claims y logs Break-Glass.

## 5. Instrucciones de Compilación y NFR
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.

## 6. Instrucciones Operativas y de Comunicación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
> 
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
