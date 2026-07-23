# Handoff Frontend — Sprint 5, Iteración 3 (Interfaz Client-Side: GUI y State)

> **Fecha:** 2026-04-18 | **Iteración:** Sprint5-Iter3 | **Arquitecto:** Líder
> **US Objetivo:** US-025 (CA-09 al CA-26), US-002 (CA-11 al CA-15), US-029 (CA-21 al CA-30), US-007 (CA-16 al CA-20)

---

## 1. Contexto Estratégico

Esta es TU iteración. La Iteración 3 es el momento en que el Frontend cobra vida visual. Todas las Tiendas Pinia, Mocks de Axios y contratos API que preparaste en las Iteraciones 1 y 2 ahora se traducen a **componentes Vue 3 funcionales**. El Backend ya tiene endpoints operativos. Tu misión es construir la interfaz "ciega pero obediente" a las directivas de seguridad establecidas previamente.

**Deuda técnica pendiente de Iteración 2:** Implementar el getter `hasActiveExtensions` en `useTimeboxStore.ts` (fue registrado como deuda menor en la certificación).

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-002 (Vue 3 / Pinia) | Toda gestión de estado DEBE fluir por Pinia. Prohibido `provide/inject` para datos transaccionales. |
| ADR-010 (Pirámide Testing) | Tests Vitest para cada componente nuevo (`mount` con `createTestingPinia`). Cero conexiones a BD. |
| ADR-011 (CQRS Local) | Los componentes Vue NO hacen fetch directo. Siempre deben consumir Actions del Store Pinia correspondiente. |

---

## 3. Alcance Técnico (CAs Frontend Iter3)

### 3.1 US-025 — Cards Dinámicas por Rol (CA-09 al CA-26) ★ PRIORIDAD MÁXIMA
Este es el bloque más grande de esta iteración. Construye el sistema de renderizado condicional basado en roles.

- **CA-09/CA-10:** Crear componente `DynamicRoleCards.vue` que reciba el `activeRole` del `authStore` y renderize condicionalmente las Cards/Widgets del Dashboard.
- **CA-11 al CA-14:** Implementar ocultamiento atómico: si el rol no incluye un permiso específico, la Card correspondiente **no debe existir en el DOM** (no usar `v-show`, usar `v-if`).
- **CA-15 al CA-18:** Skeleton Loaders (`SkeletonCard.vue`) visibles durante la carga del `GET /dashboard/cards`. Transición animada cuando los datos reales llegan.
- **CA-19 al CA-22:** Virtual Scrolling para grillas masivas de tareas usando `vue-virtual-scroller` o implementación nativa con `IntersectionObserver`.
- **CA-23 al CA-26:** App Shell reactivo — al cambiar el `activeRole` en el selector del Header, el Dashboard se re-renderiza sin reload completo de página.

### 3.2 US-002 — Interfaz del Workdesk: Reclamar Tarea (CA-11 al CA-15)
- **CA-11/CA-12:** Botón `[Reclamar]` en cada fila de la grilla de tareas disponibles. Al presionar, invocar `workboxStore.claimTask(taskId)` → API POST → feedback visual inmediato (botón muta a `[Abrir Formulario]`).
- **CA-13:** Escuchar WebSocket topic `/topic/tasks` para recibir eventos `TASK_CLAIMED`. Si otro operador reclama una tarea visible, debe desaparecer de mi grilla con animación fade-out.
- **CA-14/CA-15:** Modal de confirmación antes de `unclaim`. Toast de éxito/error post-acción.

### 3.3 US-029 — Interfaz del Form Submit (CA-21 al CA-30)
- **CA-21 al CA-24:** Componente `TaskFormSubmit.vue` con validación isomórfica Zod (`z.object`) en navegador antes del POST. Mostrar errores inline por campo.
- **CA-25/CA-26:** Spinner Loader superpuesto durante el Submit (`isSubmitting` del store). Bloquear doble click con flag reactivo.
- **CA-27/CA-28:** Feedback visual post-submit: Toast "Formulario enviado" (éxito) o Modal "Error de validación" (rechazo server-side).
- **CA-29/CA-30:** Implementar Soft-Undo: durante 5 segundos después del submit exitoso, mostrar banner "¿Deshacer envío?" que invoque un rollback si la tarea aún no fue procesada por Camunda.

### 3.4 US-007 — Panel NLP Modeler (CA-16 al CA-20)
- **CA-16/CA-17:** Componente `DmnNlpPanel.vue`. Textarea para prompt de lenguaje natural. Botón "Generar Reglas DMN".
- **CA-18/CA-19:** Al recibir la respuesta `{ dmnXml }`, renderizar el XML en un panel de previsualización (puede ser `<pre>` con syntax highlight o integración con `dmn-js` viewer).
- **CA-20:** Indicador de confianza visual: barra de progreso coloreada según `confidence` (verde > 80%, amarillo 50-80%, rojo < 50%).

---

## 4. Inventario de Stores Pinia Requeridos

| Store | Estado | Acción Iter3 |
|-------|:------:|-------------|
| `useTimeboxStore` | ✅ Certificado Iter2 | Añadir getter `hasActiveExtensions` (deuda técnica). |
| `workboxStore` (o `useWorkboxStore`) | 🆕 Crear | State: `tasks[]`, `claimingTaskId`, `isLoading`. Actions: `fetchTasks`, `claimTask`, `unclaimTask`. |
| `authStore` | ✅ Existente | Consumir `activeRole`, `effectiveRoles`. Emitir `switchRole(roleId)`. |
| `formSubmitStore` (o `useFormStore`) | 🆕 Crear | State: `formData`, `isDirty`, `isSubmitting`, `validationErrors`. Actions: `saveDraft`, `submitForm`, `softUndo`. |
| `dmnStore` (o `useDmnStore`) | 🆕 Crear | State: `generatedXml`, `confidence`, `isGenerating`. Actions: `generateFromPrompt`. |

---

## 5. Reglas de Gobernanza Mandatorias

- **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
- **TDD:** Aplica estrictamente `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor). Cada componente nuevo DEBE tener su `.spec.ts` ANTES de escribir el template.
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.
- **Reconciliación:** Al finalizar, ejecuta internamente `.agent/workflows/reconciliacionCoberturaCa.md` para verificar que cada CA tiene commit asociado.
- **Router QA:** Tu trabajo será evaluado bajo `.agent/workflows/router_certificacion_qa.md` — categoría B.1 (Auditoría Estática con Frontend).
- **Cierre Deuda:** Todo CA implementado sigue la trazabilidad de `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).
- **Testing:** Usa `createTestingPinia()` inyectado globalmente (ya configurado en `setupVitest.ts`). CERO conexiones a base de datos o red real.

---

## 6. NFR/QA Strategy

Tests Vitest de componentes (`@vue/test-utils` con `mount`). Cada componente nuevo debe tener mínimo: test de renderizado inicial, test de interacción (click/input), y test de estado de error. El Quality Gate exige `npm run test:unit` en 100% verde y `npm run build` sin warnings.

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
