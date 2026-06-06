# Handoff Técnico: QA — US-036 (Identity Governance) - Fase 2

## 1. Contexto de la Tarea
- **Iteración:** 04-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-06, CA-07, CA-08, CA-09, CA-10, CA-11
- **Rama:** `DevDavid`
- **SSOT:** [epic_E_seguridad_identidad_config.md](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/docs/requirements/epics/epic_E_seguridad_identidad_config.md)

## 2. Estrategia de Prueba (NFR/QA)
- **Credenciales:** 
  - Usuario: `root@ibpms.local`
  - Contraseña: `Root#Temp4Sys`
- **Herramientas:** Playwright (E2E), JUnit (Backend), Vitest (Frontend).
- **Referencia:** Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story).

## 3. Escenarios Gherkin a Validar

### CA-07: Soft-Delete
- **Given** un usuario activo en el sistema.
- **When** el administrador lo desactiva desde la Pantalla 14.
- **Then** el usuario no debe poder loguearse.
- **And** en la tabla debe aparecer con el sello `[Usuario Inactivo]`.
- **And** en la base de datos el registro debe persistir con un flag de estado.

### CA-09: Delegación Temporal
- **Given** un usuario autenticado.
- **When** crea una delegación para un suplente con un rango de fechas que incluye el día de hoy.
- **Then** el suplente debe poder visualizar los módulos del delegante.
- **When** se modifica la fecha de fin a un día en el pasado.
- **Then** el acceso del suplente debe revocarse automáticamente.

### CA-10: Service Accounts (API Keys)
- **Given** el módulo de Cuentas de Servicio.
- **When** se genera una nueva API Key.
- **Then** el sistema debe mostrar la llave solo una vez.
- **When** se realiza una petición HTTP manual (curl/postman) al backend usando la API Key en el header.
- **Then** el backend debe autorizar la petición basándose en el rol atado a la llave.

## 4. Instrucciones Operativas
- **Compilación obligatoria:** Antes de iniciar pruebas, asegurar que el backend y frontend compilen sin errores.
- **Evidencia:** Adjuntar capturas de pantalla de los sellos de `[Usuario Inactivo]` y del modal de `API Key`.

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
