---
title: "Handoff Frontend - US-036 Remediación (Iteración 09-DEV-REMEDIATION)"
agent: "Frontend"
branch: "DevDavid"
us: "US-036"
cas: "CA-16, CA-17, CA-27, CA-28"
---

# 1. Metadatos de la Delegación
*   **Rol Asignado:** Frontend
*   **Rama de Trabajo:** `DevDavid`
*   **US Objetivo:** US-036 (Identity Governance)
*   **Criterios a Validar:** CA-16, CA-17, CA-27, CA-28.
*   **Alineación Arquitectónica:** Cumplimiento de ADR-002 (Vue 3 / Pinia) y Zero-Trust UI.

# 2. Contexto Técnico
Debemos remediar las brechas detectadas durante la auditoría del Frontend.
- **CA-27 (Inmutabilidad de Roles Nativos):** En el modal de edición de roles (`GlobalRolesTable.vue` o `IdentityGovernance.vue`), si el rol seleccionado es fundacional (Ej. `SUPER_ADMIN`), la interfaz de selección de módulos de menú debe estar estrictamente deshabilitada (`Read-Only/Disabled`).
- **CA-28 (Granularidad Macro):** Proveer los controles UI (Checkboxes o Toggles) para habilitar/deshabilitar estrictamente los 7 Módulos Macro principales para roles customizados.
- **CA-16 (Informes Densos CISO):** Conectar el botón de generación de reporte al endpoint real del backend `POST /api/v1/security/audit/reports` y gestionar la descarga del archivo CSV/Excel.
- **CA-17 (Traza Indeleble):** La pestaña de Auditoría de Seguridad debe consumir la API real de auditoría (quitando mocks duros) para visualizar qué Administrador ejecutante inyectó permisos.

# 3. Entregables Esperados
1.  **UI de Gobernanza:** Modal de Roles actualizado respetando inmutabilidad y mostrando 7 Módulos Macro.
2.  **Reportes y Auditoría:** Consumo de endpoints reales mediante Axios y actualización del estado en Pinia si es necesario.
3.  **Pruebas (Unit/Component):** Agregar o corregir tests en Vitest para validar que el `SUPER_ADMIN` no se puede editar (disabled).

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

# 4. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
