# Handoff Frontend — Sprint 5.1 (Cierre de Deuda Técnica: XSS, Read-Only, Validación)

> **Fecha:** 2026-04-18 | **Sprint:** 5.1 (Remediación) | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-5, CA-7, CA-9), US-007 (CA-4), US-029 (CA-2)
> **Rama de trabajo:** `sprint-5/iteracion4`

---

## 1. Contexto Estratégico

El Backend del Sprint 5.1 acaba de remediar las brechas críticas: JWT injection, BD real, IDOR fix, force-unclaim, audit-trail. Tu trabajo es **reflejar esas correcciones en la UI** y cerrar las brechas frontales pendientes: modo read-only pre-claim, sanitización XSS del canvas DMN, y feedback de validación Zod campo-a-campo.

**Prerequisito:** Backend del Sprint 5.1 pusheado en `sprint-5/iteracion4`.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-002 (Vue 3) | Nuevos componentes: `TaskPreviewModal.vue`, `ClaimAuditTrail.vue`. Extiende `WorkdeskGrid.vue`. |
| ADR-001 (Hexagonal FE) | Toda lógica de fetch preview/audit vive en `useWorkdeskStore.ts`, no en componentes. |
| ADR-010 (Pirámide) | Tests Vitest `mount()` para cada componente nuevo. |

---

## 3. Alcance Técnico

### 3.1 US-002 — UI para Claim Remediado (3 CAs)

- **CA-5 (Modo Solo Lectura):** Crear `TaskPreviewModal.vue` que consuma `GET /api/v1/workbox/tasks/{id}/preview`. Al hacer click en una tarea del pool (ANTES de reclamar), mostrar un modal de previsualización con: título, descripción, SLA, tipo, grupo candidato. Botón "Reclamar" que invoca `claimTask()` del store. Botón "Cerrar" que descarta. El contenido es READ-ONLY (todos los campos deshabilitados).
  - En `useWorkdeskStore.ts`, añadir action `fetchTaskPreview(taskId: string)` que llame al endpoint y retorne el DTO.

- **CA-7 (Modal Advertencia al Liberar):** Cuando el usuario haga click en "Liberar tarea" (`unclaimTask`), mostrar un dialog de confirmación: "⚠️ Si liberas esta tarea, perderás todo el progreso no guardado. ¿Estás seguro?" con botones "Liberar" (rojo) y "Cancelar". Solo invocar `unclaimTask()` si el usuario confirma.

- **CA-9 (Historial de Auditoría):** Crear `ClaimAuditTrail.vue` que consuma `GET /api/v1/workbox/tasks/{id}/audit-trail`. Renderizar una tabla timeline con: fecha, acción (CLAIM/UNCLAIM/FORCE_UNCLAIM), actor, motivo. Integrar como tab o accordion dentro del `TaskPreviewModal`.
  - En `useWorkdeskStore.ts`, añadir action `fetchAuditTrail(taskId: string)`.

### 3.2 US-007 — Sanitización XSS (1 CA)

- **CA-4 (Sandboxing XSS en Canvas DMN):** En `DmnIntelligence.vue` (o el componente que renderiza el XML DMN en el DOM), aplicar sanitización ANTES del render. Usar `DOMPurify` (ya como dependencia o añadir como `devDependency`):
  ```typescript
  import DOMPurify from 'dompurify';
  const sanitizedXml = DOMPurify.sanitize(rawDmnXml, { USE_PROFILES: { xml: true } });
  ```
  Verificar que no se inyecten `<script>`, `onerror`, ni `javascript:` en los nodos del canvas. Crear test Vitest que inyecte payload XSS y verifique que es strippeado.

### 3.3 US-029 — Feedback de Validación Zod (1 CA)

- **CA-2 (Errores Campo-a-Campo):** En el componente de formulario dinámico, interceptar la respuesta HTTP 400 del submit. Parsear el cuerpo RFC 7807 que ahora incluye `errors: [{ field, message, rejectedValue }]`. Para cada error, mostrar el mensaje inline debajo del campo correspondiente con estilo rojo. Usar el campo `field` como key para mapear al input correcto.
  - En `useFormStore.ts`, extender `submitForm()` para parsear el array `errors` y poblar `validationErrors` como `Record<string, string>`.

---

## 4. Reglas de Gobernanza Mandatorias

- **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
- **TDD:** Aplica estrictamente `.agents/skills/tdd_first/SKILL.md`. Cada componente nuevo DEBE tener su `.spec.ts` ANTES de codificar.
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.
- **Reconciliación:** Al finalizar, ejecuta internamente el workflow `.agent/workflows/reconciliacionCoberturaCa.md`.
- **Router QA:** Tu código será evaluado bajo `.agent/workflows/router_certificacion_qa.md`.
- **Cierre Deuda:** Todo CA implementado debe seguir la trazabilidad exigida por `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).

---

## 5. NFR/QA Strategy

- Vitest `mount()` para `TaskPreviewModal.spec.ts`, `ClaimAuditTrail.spec.ts`.
- Vitest con payload XSS para verificar sanitización DOMPurify.
- Vitest con mock Axios que retorne HTTP 400 con `errors[]` RFC 7807 → verificar inline messages.
- Quality Gate: `npm run test:unit` (100% green) + `npm run build` (0 errores).

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-5/iteracion4`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> ⚠️ **CORRECCIÓN HEREDADA (Iter3/Iter4):**
> - NO crees stores duplicados. Extiende `useWorkdeskStore.ts` y `useFormStore.ts` existentes.
> - Usa `vi.useFakeTimers()` con teardown limpio en `afterEach`.
