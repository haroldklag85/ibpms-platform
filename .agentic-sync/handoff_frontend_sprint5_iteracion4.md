# Handoff Frontend — Sprint 5, Iteración 4 (Blindaje: QA Defensivo y Flujos Negativos)

> **Fecha:** 2026-04-18 | **Iteración:** Sprint5-Iter4 | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-21 al CA-28), US-029 (CA-31 al CA-37), US-007 (CA-21 al CA-24)
> **Rama de trabajo:** `main`

---

## 1. Contexto Estratégico

La Iteración 4 es la fase **de blindaje final** del Sprint 5. El Frontend que construiste en la Iteración 3 maneja el "camino feliz": reclamar tareas, enviar formularios, generar DMN. Ahora debes fortalecer esa UI para que **sobreviva al fracaso**: pérdida de red durante submit, despojos forzosos de tarea por supervisores, expiración de SLA, sesiones duplicadas, y operaciones masivas que saturan el WebSocket.

**Prerequisito:** El Backend de la Iteración 4 debe estar pusheado antes de iniciar esta fase. Tus nuevos componentes consumirán los endpoints defensivos que el Backend acaba de crear.

---

## 2. Alineación Arquitectónica

| ADR | Impacto en Iteración 4 |
|-----|------------------------|
| ADR-002 (Vue 3) | Nuevos componentes `ErrorBoundary.vue`, `NetworkRetryModal.vue` y `SessionConflictBanner.vue`. Todos en `components/shared/`. |
| ADR-001 (Hexagonal FE) | Toda lógica de reintentos y cálculo de TTL vive en los stores Pinia, NO en los componentes `.vue`. |
| ADR-010 (Pirámide) | Tests Vitest `mount()` para cada componente negativo. `vi.useFakeTimers()` para timers de TTL y SLA. |

---

## 3. Alcance Técnico (CAs Frontend Iter4)

### 3.1 US-002 — Caminos Infelices de Reclamación (CA-21 al CA-28)

**Store a extender:** `useWorkdeskStore.ts` (el que ya existe con 280+ líneas).

- **CA-21 (Rollback Optimistic UI):** Implementar patrón Optimistic Update en `claimTask()`: (a) mutar la UI inmediatamente (remover tarea del pool visual), (b) si el POST falla con HTTP 4xx/5xx, **revertir** la mutación local y mostrar Toast de error: "No se pudo reclamar. La tarea ha sido devuelta al pool." Usar `structuredClone()` del estado previo como snapshot para hacer el rollback.

- **CA-22 (Separación Visual Bandeja/Cola):** Crear componente `WorkdeskTabs.vue` con dos tabs: "Mi Bandeja" (filtra `GET ?view=PERSONAL`) y "Cola del Equipo" (filtra `GET ?view=POOL`). El tab activo se almacena en `useWorkdeskStore.activeView`. Al cambiar de tab, re-fetch automático sin reload.

- **CA-23 (Agregación WebSocket Masiva):** En el handler STOMP del `useWorkdeskStore`, escuchar el nuevo evento `TASKS_BULK_UPDATED`. Cuando llegue, hacer un re-fetch completo del pool en lugar de intentar actualizar N tareas individuales. Usar debounce de 300ms para evitar multiple re-fetches en ráfaga.

- **CA-24 (Umbrales Configurables SLA):** Crear composable `useSlaTrafficLight.ts` que consuma `GET /api/v1/config/sla-thresholds` y compute el color del semáforo para cada tarea: `class="sla-green" | "sla-yellow" | "sla-red" | "sla-expired"`. Cachear los umbrales en `sessionStorage` con TTL de 5 minutos.

- **CA-25 (Recálculo SLA Visibilitychange):** Añadir listener `document.addEventListener('visibilitychange')` en `useWorkdeskStore`. Cuando el usuario vuelve de una pestaña inactiva (`document.visibilityState === 'visible'`), disparar `fetchSlaStatus()` para cada tarea visible y actualizar los semáforos. Usar `requestIdleCallback` para no bloquear el hilo principal.

- **CA-26 (Relleno Auto post-Remove WS):** Cuando llegue el evento WS `TASK_POOL_REFRESH`, verificar si la lista visible tiene menos ítems que el `pageSize`. Si es así, hacer un re-fetch para rellenar la grilla. NO recargar toda la página.

- **CA-27 (Vocabulario Completo WS):** Crear un `enum WebSocketEventType` en `types/websocket.ts` que refleje el enum del Backend: `TASK_CLAIMED`, `TASK_UNCLAIMED`, `TASK_COMPLETED`, `TASK_EXPIRED`, `TASK_POOL_REFRESH`, `TASKS_BULK_UPDATED`, `TASK_FORCE_UNCLAIMED`. Actualizar el handler STOMP para despachar por tipo. Mostrar Toast específico para `TASK_FORCE_UNCLAIMED`: "⚠️ Un supervisor ha reasignado tu tarea."

- **CA-28 (Botón "Atender Siguiente"):** Añadir botón "Atender Siguiente" en el header del WorkdeskGrid. Al hacer click, invocar `POST /api/v1/workbox/tasks/claim-next`. Si retorna 204, mostrar Toast: "No hay tareas disponibles". Si retorna 200 con tarea, navegar directamente al formulario (`router.push('/task-viewer/' + task.id)`).

### 3.2 US-029 — Casos Negativos de Submit (CA-31 al CA-37)

**Store a extender:** `useFormStore.ts`.

- **CA-31 (Modal de Reintento por Timeout):** Si el `POST /complete` retorna HTTP 504, mostrar `NetworkRetryModal.vue` con: (a) mensaje "El servidor no pudo procesar a tiempo. ¿Deseas reintentar?", (b) botón "Reintentar" que invoque el mismo submit con la misma `idempotency-key`, (c) botón "Cancelar" que regrese al formulario sin perder datos. **El modal debe ser no-dismissable** (sin click-outside).

- **CA-32 (Retry con Idempotency-Key):** En `useFormStore.submitForm()`, si el submit falla por timeout (504) o error de red (ERR_NETWORK), guardar el `idempotencyKey` original y permitir hasta 3 reintentos con la MISMA key. Mostrar un contador visual: "Intento 2 de 3". Si los 3 fallan, mostrar "Contacte a soporte" con el `idempotencyKey` como referencia.

- **CA-33 (Validación por Etapa Wizard):** Crear composable `useWizardValidation.ts` que exponga `validateStep(stepIndex: number)`. Al avanzar de paso en un formulario wizard, invocar `POST /api/v1/workbox/tasks/{id}/validate-step?stepIndex=N`. Bloquear la navegación al siguiente paso si la validación falla. Mostrar errores inline dentro del paso actual.

- **CA-34 (Feedback Archivos Rechazados):** En el componente de upload de archivos, interceptar HTTP 422 de la aduana. Parsear el cuerpo RFC 7807 y mostrar inline debajo del input de archivo: "❌ archivo.exe: extensión no permitida" o "❌ imagen.png: excede el tamaño máximo (10 MB)".

- **CA-35 (Banner Sesión Duplicada):** Crear `SessionConflictBanner.vue` que se muestre cuando el `POST /complete` o `PUT /draft` retorne HTTP 409 con tipo `SESSION_CONFLICT`. Banner sticky en la parte superior: "⚠️ Tienes otra pestaña editando esta tarea. Cierra la otra sesión para continuar." Botón "Forzar Edición Aquí" que invoque `DELETE /api/v1/workbox/tasks/{id}/active-session` y reintente.

- **CA-36 (Countdown TTL Borrador):** Crear composable `useDraftTtl.ts` que haga polling cada 60 segundos al endpoint `GET /api/v1/workbox/tasks/{id}/draft-ttl`. Cuando `ttl < 300` (5 minutos), mostrar banner amarillo: "Tu borrador expira en X:XX". Cuando `ttl <= 0` (HTTP 410), mostrar modal rojo bloqueante: "Tu borrador ha expirado. Los datos se han perdido." y navegar al Workdesk.

- **CA-37 (Captura Global HTTP 5xx):** En el `apiClient.ts` (interceptor de Axios), garantizar que TODA respuesta HTTP 500 muestre un Toast genérico: "Error interno del servidor. Inténtelo más tarde." SIN revelar detalles del stack trace. Los errores HTTP 4xx sí se pasan al componente para manejo específico.

### 3.3 US-007 — Salvaguarda DMN UI (CA-21 al CA-24)

**Store a extender:** `useDmnStore.ts`.

- **CA-21 (Feedback Visual XML Inválido):** Si el `POST /api/v1/dmn/generate` retorna HTTP 422 (XML inválido post-validación), mostrar en el panel de previsualización un bloque rojo con el detalle del error SAX. NO renderizar el XML roto en el canvas.

- **CA-22 (Hit Policy Denegada):** Si HTTP 403 con tipo `HIT_POLICY_FORBIDDEN`, mostrar dialog explicativo: "La política de decisión '[POLICY]' no está autorizada para tu rol. Contacta al administrador DMN." con botón "Regenerar con política UNIQUE".

- **CA-23 (Rate Limit Visual):** Si HTTP 429, mostrar countdown visual: "Has excedido el límite de simulaciones. Puedes reintentar en X segundos." Parsear header `Retry-After` y usar `setInterval` para el countdown. Deshabilitar el botón "Simular" hasta que el countdown llegue a 0.

- **CA-24 (Timeout Generación IA):** Si HTTP 504 del endpoint de generación, mostrar spinner con mensaje: "La generación tardó demasiado. ¿Deseas reintentar con un prompt más simple?" Botón "Reintentar" + Botón "Usar template por defecto" (invoca el mock fallback).

---

## 4. Reglas de Gobernanza Mandatorias

- **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
- **TDD:** Aplica estrictamente `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor). Cada componente `.vue` de esta iteración DEBE tener su `.spec.ts` ANTES de codificar el componente.
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.
- **Reconciliación:** Al finalizar, ejecuta internamente el workflow `.agent/workflows/reconciliacionCoberturaCa.md` para verificar que cada CA tiene commit asociado.
- **Router QA:** Tu código será evaluado bajo `.agent/workflows/router_certificacion_qa.md` — prepárate para auditoría de componentes (Nivel B.4 Automatización SDET).
- **Cierre Deuda:** Todo CA implementado debe seguir la trazabilidad exigida por `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).

---

## 5. NFR/QA Strategy

- Vitest con `mount()` para cada componente negativo: `NetworkRetryModal.spec.ts`, `SessionConflictBanner.spec.ts`, `WorkdeskTabs.spec.ts`.
- Vitest con `vi.useFakeTimers()` para validar countdown TTL y Rate Limit.
- Vitest con `vi.spyOn(apiClient)` para simular HTTP 504, 409, 429, 422, 500.
- Quality Gate: `npm run test:unit` (100% green) + `npm run build` (0 errores) antes de commit.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> ⚠️ **CORRECCIÓN MANDATORIA DEL ARQUITECTO (heredada de Iter3):**
> - NO crees stores duplicados. Extiende `useWorkdeskStore.ts` existente (280+ líneas). NO crear `useWorkboxStore.ts`.
> - Extiende `useFormStore.ts` existente para la lógica de reintentos y TTL.
> - Extiende `useDmnStore.ts` existente para los handlers de error 422/403/429/504.
