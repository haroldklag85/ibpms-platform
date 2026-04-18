# Handoff QA — US-039 CA-4 al CA-8
## Iteración 72-DEV | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto Estratégico
La US-039 define el **Formulario Genérico Base (Pantalla 7.B)**. Los CA-4 al CA-8 son remediaciones post-auditoría que establecen: campos fijos del formulario, Whitelist de variables, bloqueo VIP, persistencia de borradores y mapeo de botones de pánico a eventos BPMN.

---

## Plan de Testing por Criterio de Aceptación

### CA-4 — Validación del Cuerpo Editable
**Tests Backend (REST Assured / Testcontainers):**
1. `POST /generic-form-complete` con `observations` de 5 chars → esperar HTTP 400.
2. `POST /generic-form-complete` con `observations` de 2001 chars → esperar HTTP 400.
3. `POST /generic-form-complete` con 6 `attachmentUuids` → esperar HTTP 400.
4. `POST /generic-form-complete` sin `managementResult` → esperar HTTP 400.
5. `POST /generic-form-complete` con campo extra no autorizado (`extraField: "hack"`) → esperar stripping silencioso o HTTP 400.
6. `POST /generic-form-complete` con payload válido completo → esperar HTTP 200.

**Tests Frontend (Vitest / Playwright CT):**
1. Renderizar `GenericFormView` → verificar que existen exactamente 3 campos editables.
2. Intentar submit con textarea vacía → verificar que botón [Enviar] está deshabilitado.
3. Escribir 10+ chars en observaciones + seleccionar resultado → verificar que botón [Enviar] se habilita.

### CA-5 — Whitelist Regex por Proceso
**Tests Backend:**
1. Configurar whitelist `["Case_ID", "Client_Name"]` para un proceso. Llamar BFF con una tarea de ese proceso → verificar que `prefillData` SOLO contiene esas 2 claves.
2. No configurar whitelist (null). Llamar BFF → verificar fallback con 4 claves por defecto.
3. Inyectar variable con prefijo `_internal_test` en Camunda. Llamar BFF → verificar que NO aparece en `prefillData`.
4. Intentar configurar whitelist con 11 claves → esperar HTTP 400.

**Tests Frontend:**
1. BFF retorna `prefillData` vacío → verificar mensaje "No hay metadatos disponibles".
2. BFF retorna 4 variables → verificar que se renderizan como campos de solo lectura.

### CA-6 — Roles VIP Pre-Flight
**Tests Backend:**
1. Crear rol con `is_vip_restricted = true`. Asignar a un Lane. Intentar desplegar BPMN con `sys_generic_form` en ese Lane → esperar Hard-Stop en Pre-Flight.
2. Crear rol con `is_vip_restricted = false`. Misma acción → esperar que pase el Pre-Flight.
3. Verificar que los 3 roles seed (`ALTA_DIRECCION`, `APROBADOR_FINANCIERO`, `SELLO_LEGAL`) están marcados como `is_vip_restricted = true` por defecto.

**Tests Frontend:**
1. Si Pantalla 14 tiene columna VIP: verificar que el ícono ⭐ aparece para roles VIP.

### CA-7 — Persistencia y Auto-Guardado
**Tests Backend:**
1. `PUT /drafts/{taskId}` con datos parciales → esperar HTTP 204.
2. `GET /drafts/{taskId}` → esperar datos guardados.
3. `DELETE /drafts/{taskId}` → esperar que no exista borrador posterior.
4. Intentar guardar borrador de tarea asignada a otro usuario → esperar HTTP 403.

**Tests Frontend (Playwright CT):**
1. Escribir en textarea, esperar 10s → verificar que se dispara auto-guardado al servidor.
2. Simular cierre y reapertura → verificar banner de restauración de borrador.
3. Enviar formulario exitosamente → verificar que LocalStorage se purga.
4. Simular 3 fallos de guardado → verificar Toast "guardado automático no disponible".

### CA-8 — Botones de Pánico
**Tests Backend:**
1. `POST /generic-form-complete` con `panicAction: "APPROVED"` + justificación 20 chars → esperar HTTP 200 y verificar que `generic_form_result = "APPROVED"` en variables de Camunda.
2. `POST /generic-form-complete` con `panicAction: "RETURNED"` + justificación 20 chars → esperar HTTP 200.
3. `POST /generic-form-complete` con `panicAction: "CANCELLED"` + justificación 20 chars → esperar HTTP 200 y verificar que se lanzó Error Event con `errorCode = "TASK_CANCELLED_BY_OPERATOR"`.
4. `POST /generic-form-complete` con `panicAction: "CANCELLED"` + justificación 10 chars → esperar HTTP 400.
5. `POST /generic-form-complete` con `panicAction: "CANCELLED"` sin justificación → esperar HTTP 400.

**Tests Frontend:**
1. Clicar botón [❌ Cancelar] → verificar que abre Modal de justificación.
2. Escribir menos de 20 chars → verificar que botón [Confirmar] está deshabilitado.
3. Escribir 20+ chars → verificar que botón [Confirmar] se habilita.
4. Verificar que cada botón de pánico tiene color correcto (verde, naranja, rojo).

---

## Pirámide de Testing (ADR-011)
| Nivel | Herramienta | Cantidad Estimada |
|-------|-------------|-------------------|
| Unitario Backend | JUnit 5 + Testcontainers | ~15 tests |
| Integración Backend | REST Assured + Testcontainers | ~12 tests |
| Componente Frontend | Vitest + Playwright CT | ~10 tests |
| E2E (si Backend vivo) | Playwright | ~5 flows |

---

## Restricciones QA
1. Backend Docker vivo obligatorio para tests de integración.
2. Correspondencia Gherkin bidireccional: cada test DEBE referenciar el CA que valida.
3. Screenshots y logs como evidencia de cada test.
4. **No usar** `git stash`. Solo `git commit` + `git push`.

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en tu propia rama de sprint. Queda estrictamente prohibido usar git stash.
