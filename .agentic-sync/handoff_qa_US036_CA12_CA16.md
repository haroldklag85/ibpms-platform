# Handoff Técnico - QA - US-036 (CA-12 al CA-16)

## 1. Objetivo de la Certificación
Validar los mecanismos de expulsión de usuarios, acceso anónimo a procesos y la integridad de los reportes de auditoría ISO 27001.

**Rama de trabajo:** `DevDavid`
**Iteración:** `05-DEV-DAVID`

## 2. Estrategia NFR/QA
- **Protocolo:** Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md`.
- **Especial:** Probar la latencia de revocación (Kill-Session) y el acceso sin token (Public URL).
- **Credenciales:** `root@ibpms.local` / `Root#Temp4Sys`.

## 3. Escenarios Gherkin de Prueba

### Escenario 1: El Exorcismo Táctico (CA-14)
- **Given:** Un usuario "Prueba" con sesión activa en una pestaña.
- **When:** El Admin en Pantalla 14 presiona `[Revocar Todo y Matar Sesión]`.
- **Then:** La siguiente petición del usuario "Prueba" debe retornar HTTP 401.
- **And:** El usuario debe ser redirigido al Login automáticamente.

### Escenario 2: Acceso Ciudadano Anónimo (CA-15)
- **Given:** Un proceso con el switch `[Permitir Trámite Público]` activado.
- **When:** Se accede a la URL del proceso desde un navegador en incógnito (sin login).
- **Then:** El formulario debe cargar y permitir el envío.
- **And:** Al desactivar el switch, la misma URL debe retornar 404 (Falso 404 / CA-03 de US-051).

### Escenario 3: Integridad del Reporte CISO (CA-16)
- **Given:** Una base con usuarios, roles y procesos asignados.
- **When:** Se genera el reporte "ISO 27001".
- **Then:** El archivo descargado (CSV/Excel) debe contener las columnas de cruce correctas.
- **And:** Verificar que el reporte incluya el Timestamp UTC y el Hash de integridad.

### Escenario 4: Roles Dinámicos (CA-13)
- **Given:** Un proceso BPMN con un Lane definido por expresión `${manager_id}`.
- **When:** El usuario asignado dinámicamente entra a su Workdesk.
- **Then:** Debe ver la tarea aunque el rol no esté asignado estáticamente en Pantalla 14.

## 4. Herramientas Obligatorias
- **Playwright:** Para las pruebas de Kill-Session y Public URL.
- **JUnit:** Para validar la lógica de reporte en el backend.

## 5. Instrucciones Operativas
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia en modo `PLANNING` y elabora un `implementation_plan.md`.
> 2. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_qa.md`.
> 3. Notifica al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`..."*
> 4. **TIENES PROHIBIDO** reportar "pass" sin adjuntar evidencia verificable (logs de 401 o capturas del reporte generado).
