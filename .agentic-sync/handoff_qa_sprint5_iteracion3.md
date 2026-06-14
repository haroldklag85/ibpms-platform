# Handoff QA — Sprint 5, Iteración 3 (Certificación de Interfaz Client-Side)

> **Fecha:** 2026-04-18 | **Iteración:** Sprint5-Iter3 | **Arquitecto:** Líder
> **US Objetivo:** US-025 (CA-09 al CA-26), US-002 (CA-11 al CA-15), US-029 (CA-21 al CA-30), US-007 (CA-16 al CA-20)

---

## 1. Contexto Estratégico

La Iteración 3 levanta por primera vez los componentes Vue 3 funcionales que consumirán los endpoints y stores preparados en las iteraciones previas. Tu rol como QA es **certificar que la integración vertical (Store → Componente → API) funciona completamente**, detectando regresiones visuales, errores de estado y violaciones de accesibilidad.

**Enrutamiento QA (según `router_certificacion_qa.md`):**
Esta iteración involucra **Interfaces con Frontend**, categoría **B.1 (Auditoría Estática Transversal)** complementada con **B.4 (Automatización SDET con Playwright)** para los flujos críticos de Claim y Submit.

---

## 2. CAs a Validar con Scenarios Gherkin de Referencia

### 2.1 US-025 — Cards Dinámicas por Rol (CA-09 al CA-26)

| CA | Scenario Gherkin Resumido | Tipo de Prueba |
|----|---------------------------|:-:|
| CA-09 | Given rol "Operador", When dashboard carga, Then solo Cards de Operador visibles | Vitest Component |
| CA-10 | Given rol "Gerente", When dashboard carga, Then Cards de Gerente + heredadas visibles | Vitest Component |
| CA-11 al CA-14 | Given Card sin permiso, When render, Then Card NO existe en DOM (`v-if`) | Vitest + DOM assertion |
| CA-15 al CA-18 | Given carga lenta, When fetch pendiente, Then Skeleton Loaders visibles | Vitest Component |
| CA-19 al CA-22 | Given 500+ tareas, When scroll, Then Virtual Scrolling no degrada perf (< 200ms) | Playwright Performance |
| CA-23 al CA-26 | Given cambio de rol en Header, When switch, Then Dashboard re-render sin reload | Playwright E2E |

### 2.2 US-002 — Workdesk: Reclamar Tarea (CA-11 al CA-15)

| CA | Scenario Gherkin Resumido | Tipo de Prueba |
|----|---------------------------|:-:|
| CA-11 | Given tarea disponible, When click Reclamar, Then botón muta a "Abrir Formulario" | Vitest Component |
| CA-12 | Given tarea reclamada por mí, When click Abrir, Then formulario se abre | Playwright E2E |
| CA-13 | Given otro operador reclama T-001, When WebSocket TASK_CLAIMED, Then T-001 desaparece de mi grilla | Vitest + Mock WebSocket |
| CA-14 | Given click Unclaim, When modal confirma, Then tarea retorna a disponibles | Vitest Component |
| CA-15 | Given unclaim exitoso, When respuesta 200, Then toast "Tarea liberada" visible | Vitest Component |

### 2.3 US-029 — Form Submit Seguro (CA-21 al CA-30)

| CA | Scenario Gherkin Resumido | Tipo de Prueba |
|----|---------------------------|:-:|
| CA-21 al CA-24 | Given formulario con campos inválidos, When submit, Then errores Zod inline por campo | Vitest Component |
| CA-25/CA-26 | Given submit en progreso, When doble click, Then segundo click bloqueado (flag) | Vitest Component |
| CA-27/CA-28 | Given submit exitoso, When respuesta 200, Then toast "Formulario enviado" | Vitest Component |
| CA-29/CA-30 | Given submit exitoso, When < 5 seg, Then banner Soft-Undo visible. After 5 seg, desaparece. | Vitest + Timer mock |

### 2.4 US-007 — Panel NLP Modeler (CA-16 al CA-20)

| CA | Scenario Gherkin Resumido | Tipo de Prueba |
|----|---------------------------|:-:|
| CA-16/CA-17 | Given panel DMN, When input prompt + click Generar, Then request POST enviado | Vitest Component |
| CA-18/CA-19 | Given respuesta XML, When render, Then panel previsualización muestra HTML/XML | Vitest Component |
| CA-20 | Given confidence 0.92, When render, Then barra verde visible | Vitest Component |

---

## 3. Estrategia de Testing (Pirámide ADR-010)

### Nivel 1: Tests Unitarios Vitest (Componentes Vue)
- **Framework:** Vitest + `@vue/test-utils` + `createTestingPinia()`.
- **Scope:** Cada componente nuevo (`DynamicRoleCards.vue`, `TaskFormSubmit.vue`, `DmnNlpPanel.vue`, `SkeletonCard.vue`) debe tener su `.spec.ts`.
- **Mock Strategy:** Los Stores Pinia se inyectan con `createTestingPinia({ initialState })`. Los endpoints API se mockean con `vi.mock('@/services/apiClient')`.
- **Assertion Pattern:** Validar existencia/inexistencia en DOM (`wrapper.find('.card').exists()`), estados reactivos (`store.isLoading`), y emisiones de eventos (`wrapper.emitted()`).

### Nivel 2: Tests de Integración Playwright (Flujos E2E Críticos)
- **Flujo 1 (US-002):** Abrir Workdesk → Ver tareas → Reclamar T-001 → Verificar mutación de botón → Abrir formulario.
- **Flujo 2 (US-029):** Abrir formulario de tarea reclamada → Llenar campos → Submit → Verificar toast de éxito.
- **Flujo 3 (US-025):** Login como Operador → Verificar Cards visibles → Switch a Gerente → Verificar Cards heredadas aparecen.
- **Concurrencia (US-002):** Dos ventanas de incógnito simultáneas intentando reclamar la misma tarea. Una debe recibir error visual.

### Nivel 3: Tests de Regresión Visual (Opcional Stretch)
- Captura de screenshots con `page.screenshot()` para los estados Skeleton → Loaded en DynamicRoleCards para detectar regresiones CSS.

---

## 4. Endpoints y Vistas a Verificar en Integración

| Capa | Artefacto | Validación |
|------|-----------|------------|
| Backend | `POST /api/v1/workbox/tasks/{id}/claim` | Retorna 200 con `{ status: CLAIMED }` |
| Backend | `POST /api/v1/workbox/tasks/{id}/complete` | Retorna 200 o 403 si no es assignee |
| Backend | `PUT /api/v1/workbox/tasks/{id}/draft` | Retorna 200 con `{ status: DRAFT_SAVED }` |
| Backend | `POST /api/v1/dmn/generate` | Retorna 200 con `{ dmnXml, confidence }` |
| Backend | `GET /api/v1/dashboard/cards?role=X` | Retorna array de Cards filtradas |
| Frontend | `DynamicRoleCards.vue` | Renderizado condicional por rol |
| Frontend | `TaskFormSubmit.vue` | Validación Zod + Submit + Soft-Undo |
| Frontend | `DmnNlpPanel.vue` | Input prompt + render XML |

---

## 5. Reglas de Gobernanza Mandatorias

- **Ley de Correspondencia Gherkin:** Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar que cada CA tiene su test correspondiente. Todo CA sin test debe reportarse como **Cobertura Faltante**.
- **Router de Certificación:** Este handoff fue enrutado bajo `.agent/workflows/router_certificacion_qa.md`, caminos B.1 (Estático) + B.4 (Playwright SDET).
- **Reconciliación:** Al finalizar, ejecuta `.agent/workflows/reconciliacionCoberturaCa.md` — cruza SSOT vs Handoff vs Commits vs Matriz. Detecta Falsos Positivos.
- **Cierre Deuda:** Actualiza `.agentic-sync/coverage_matrix.md` marcando cada CA validado con commit hash y resultado.
- **Build obligatorio Frontend:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md` para verificar que el build de producción pasa sin warnings.
- **TDD:** Aplica `.agents/skills/tdd_first/SKILL.md` — escribe el test ANTES del componente.
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.

---

## 6. NFR/QA Strategy

Certificación mediante suites Vitest (componentes unitarios) y Playwright (E2E multi-usuario). El Quality Gate exige:
1. `npm run test:unit` — 100% verde (cero skips no justificados).
2. `npx playwright test` — flujos críticos pasando contra `localhost:8080` (o con MSW si backend no disponible).
3. Actualización de `coverage_matrix.md` con estado final de cada CA.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
