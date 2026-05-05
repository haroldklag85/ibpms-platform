# Handoff QA: Certificación Formal US-036 (Identity Governance RBAC)

## 1. Metadatos de la Delegación
- **Iteración:** 03-DEV-DAVID
- **US:** US-036 (Matriz de Control de Acceso Basado en Roles)
- **CAs:** CA-01, CA-02, CA-04, CA-06
- **Rama Git:** `DevDavid`
- **Ambiente:** Docker Stack Activo (Port 8080 Backend / 5173 Frontend)
- **Credenciales:** `root@ibpms.local` / `Root#Temp4Sys` (Break-Glass Mode)

## 2. Alineación Arquitectónica
- **ADR-010 (Testing Pyramid):** Se exige validación E2E mediante Playwright sin Mocks.
- **ADR-011 (CQRS/RBAC):** Validación de que los permisos efectivos se calculan correctamente en el backend (vía API/Audit Logs).
- **Ley Global 2:** Ejecución sobre stack Dockerizado.

## 3. Escenarios de Prueba (Gherkin)

### Escenario 1: Importación Dual-Motor (CA-01)
**Given** el administrador accede a la Pantalla 14 (Identity Governance)
**When** activa el modal de "Importar desde EntraID"
**And** selecciona un grupo federado (Simulado en UI con data-testid)
**Then** el sistema crea el rol local y le asigna el tag "Azure AD" en la tabla.

### Escenario 2: Inmutabilidad del Guardián (CA-02)
**Given** el administrador visualiza la lista de roles
**When** intenta localizar el botón de "Eliminar" para `ROLE_SUPER_ADMIN`
**Then** el botón debe estar ausente o deshabilitado en el DOM.
**And** cualquier intento de borrado vía API debe ser rechazado por el backend.

### Escenario 3: Matriz Granular Iniciador vs Ejecutor (CA-04)
**Given** la pestaña "Roles de Proceso"
**When** el administrador alterna el checkbox de "Iniciar" para un proceso específico
**Then** la petición al backend debe retornar un 200 OK
**And** al recargar la página, el estado debe persistir (Zero-Mock Check).

### Escenario 4: Herencia Piramidal (CA-06)
**Given** un rol hijo recién creado
**When** se le asigna un "Rol Padre" con permisos pre-existentes
**Then** el sistema debe computar los permisos efectivos (Visualizados como "Heredados" en la matriz).

## 4. Instrucciones Técnicas para Playwright
- **Ubicación del Test:** `frontend/e2e/us-036-rbac-core.spec.ts`
- **Configuración:** `playwright.config.ts` (Bypass `playwright.e2e.config.ts` si falta `dotenv`).
- **Comando de Ejecución:** `npx playwright test frontend/e2e/us-036-rbac-core.spec.ts`

## 5. Evidencia Requerida
- Captura de pantalla del estado final de cada test.
- Reporte de conformidad final en `.agentic-sync/qa_report_US-036.md`.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_QA.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_QA.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin.
