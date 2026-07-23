# 🔧 Handoff Frontend — US-007 Bloque 2
> **Fecha:** 2026-05-03 | **Rama:** `sprint-6` | **Origen:** Auditoría Arquitectónica US-007
> **Agente destino:** Frontend | **Prioridad:** 🟠 MEDIA (Paso 3 de 4 — post Backend)
> **Dependencia:** ✅ Backend completado y pusheado en `sprint-6`

---

## 1. Objetivo
Remediar los 8 GAPs Frontend detectados en la auditoría forense de US-007. Incluye validaciones de seguridad XSS, UX del modo manual, buscador in-app, y timeouts SSE. Todos los cambios deben usar Pinia para estado, Axios para HTTP, y TypeScript estricto (ADR-002).

## 2. Alineación Arquitectónica
| ADR | Regla Frontend |
|-----|----------------|
| ADR-002 | Vue 3 Composition API + Pinia. PROHIBIDO stores globales sin Pinia. |
| ADR-010 | Tests: Vitest (unit) + Playwright (E2E). PROHIBIDO jsdom para renderizado visual. |
| ADR-014 | Error observability: interceptor global Axios + error boundaries. |

## 3. Componentes Existentes (NO tocar sin justificación)
- `DmnIntelligence.vue` — Vista principal Pantalla 4 ✅
- `DmnGridManual` — Componente de grilla manual ✅
- `useDmnStore.ts` — Store Pinia ✅
- `DmnGridManual.spec.ts` — Tests de grilla ✅
- `DmnNlpPanel.spec.ts` — Tests de panel NLP ✅
- `DmnAntiXss.spec.ts` — Tests anti-XSS ✅

## 4. GAPs a Remediar (8 GAPs)

### GAP-03 — CA-04: DOMPurify + Sandbox Visual
**Archivos a verificar/modificar:** `DmnIntelligence.vue`, `DmnGridManual` (componente de celdas)
**Acción:**
- Verificar que TODA celda renderizada en la grilla pase por `DOMPurify.sanitize()` antes de inyectarse en el DOM.
- Si `DOMPurify` no está instalado, agregarlo como dependencia: `npm install dompurify @types/dompurify`.
- Verificar que las expresiones FEEL renderizadas NO usen `v-html` sin sanitización.
- El `DmnAntiXss.spec.ts` ya existe — confirmar que cubre celdas con payloads XSS clásicos (`<script>`, `onerror`, `javascript:`).
**Test:** Si falta, agregar test con celda que contenga `<img onerror="alert(1)">` → debe renderizar texto plano.

### GAP-08 — CA-10: Test QA Virtual Scrolling
**Archivo a verificar:** Tests existentes en `DmnGridManual.spec.ts`
**Acción:**
- Virtual scrolling ya está implementado.
- Verificar que existe test que cargue una tabla de 50+ filas y confirme que solo se renderizan las filas visibles en el viewport.
- Si no existe, agregar test con `wrapper.findAll('.dmn-row')` → contar que sea < 50 cuando la tabla tiene 50 filas.
**Test:** Vitest con tabla de 50 filas → DOM solo contiene ~20 filas renderizadas.

### GAP-10 — CA-12: Test QA Panic Modal
**Archivo a verificar:** Tests existentes o nuevo `DmnPublishModal.spec.ts`
**Acción:**
- El panic modal ("Publicar V2") ya está implementado en `DmnIntelligence.vue`.
- Verificar que existe test que:
  1. Simule clic en [Publicar V2].
  2. Verifique que aparece modal con input obligatorio.
  3. Verifique que escribir `CONFIRMO_V2` habilita el botón de confirmación.
  4. Verifique que escribir cualquier otra cosa lo mantiene deshabilitado.
- Verificar existencia de botón `[⏪ Revertir a V1]` y que invoca `POST /api/v1/dmn/{id}/rollback`.
**Test:** Vitest con mount + interacción modal.

### GAP-21 — CA-24: Buscador In-App para Grilla DMN
**Archivo nuevo:** `components/dmn/DmnGridSearch.vue` (o integrar en `DmnGridManual`)
**Acción:**
- Implementar buscador que intercepte `Ctrl+F` (prevenir el buscador nativo del navegador).
- El buscador debe buscar en TODAS las filas de la tabla (incluidas las no renderizadas por virtual scrolling), consultando la data del store `useDmnStore`.
- Resaltar coincidencias en amarillo (`<mark>` o clase CSS `.search-highlight`).
- Agregar botones `[↑ Anterior]` y `[↓ Siguiente]` para navegar entre coincidencias.
- Al navegar, hacer scroll automático a la fila coincidente.
- Mostrar contador `"3 de 12 resultados"`.
**Test:** Vitest con tabla de 20 filas, búsqueda de texto que aparece en 3 filas → verificar 3 highlights.

### GAP-22 — CA-25: Timeout 30s + Stall 15s SSE
**Archivo a verificar/modificar:** `useDmnStore.ts` (función que abre EventSource SSE)
**Acción:**
- Verificar que existe un `setTimeout(30000)` que cierre la conexión SSE si NO se recibe NINGUNA fila en 30 segundos.
- Verificar que existe un timer de stall: si la generación ya comenzó (≥1 fila) pero pasan 15s sin nueva fila, activar mecanismo de resiliencia (borrador parcial + botón reintentar del CA-19).
- Si el timeout de 30s se dispara, mostrar: *"La generación tardó más de lo esperado. Pulse [🔄 Reintentar]"*.
- Referencia de rendimiento: Time To First Row < 8 segundos.
**Test:** Vitest/mock SSE con delay > 30s → verificar que el store cambia a estado `TIMEOUT`.

### GAP-23 — CA-26: Test Coexistencia Chat + Grilla
**Archivo a verificar:** Tests existentes
**Acción:**
- La coexistencia UI ya está implementada en `DmnIntelligence.vue`.
- Verificar que existe test que confirme que ambos paneles (Chat NLP y Grilla Manual) son visibles simultáneamente.
- Verificar que editar la grilla NO oculta el chat, y viceversa.
**Test:** Vitest con mount `DmnIntelligence` → verificar que tanto `.nlp-panel` como `.dmn-grid` están en el DOM.

### GAP-24 — CA-27: Dropdown Zod Obligatorio en Headers
**Archivo a verificar:** `DmnGridManual` (componente de headers/columnas de entrada)
**Acción:**
- Verificar que al agregar una nueva columna de entrada (Input), el usuario NO puede escribir texto libre.
- Debe usar un dropdown que consuma el Diccionario de Datos Zod de US-003.
- El dropdown debe consumir un endpoint real (ej: `GET /api/v1/forms/{formId}/variables`) o los datos del `useDmnStore`.
- PROHIBIDO permitir variables escritas a mano que no existan en el diccionario Zod.
**Test:** Vitest con intento de agregar input manualmente → verificar que se fuerza el dropdown.

### GAP-25 — CA-30: Editabilidad Post-Carga XML
**Archivo a verificar:** `DmnIntelligence.vue` (flujo de XML upload)
**Acción:**
- Verificar que cuando un usuario sube un `.dmn` (Modo Desarrollador), la tabla resultante es COMPLETAMENTE editable en la grilla visual.
- El usuario debe poder modificar celdas, agregar filas, eliminar filas (excepto catch-all).
- NO debe requerir resubir el XML para cada cambio.
**Test:** Vitest con XML mock cargado → verificar que las celdas son editables (`contenteditable` o input activo).

## 5. Entregables Esperados
- [ ] 8 GAPs remediados/verificados.
- [ ] Tests Vitest para cada GAP.
- [ ] Build limpio (`npm run build` exit 0).
- [ ] `git commit` y `git push` en rama `sprint-6`.

## 6. Restricciones
- **PROHIBIDO:** Stores globales fuera de Pinia.
- **PROHIBIDO:** `v-html` sin DOMPurify.
- **PROHIBIDO:** Parser FEEL en JavaScript (CA-15 explícito: la evaluación DMN es server-side).
- **PROHIBIDO:** Llamadas HTTP directas — usar Axios vía interceptor global.

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend_US007.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend_US007.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
