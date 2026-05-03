# 🔧 Handoff QA — US-007 Bloque 3
> **Fecha:** 2026-05-03 | **Rama:** `sprint-6` | **Origen:** Auditoría Arquitectónica US-007
> **Agente destino:** QA | **Prioridad:** 🟡 MEDIA (Paso 4 de 4 — post Frontend)
> **Dependencia:** ✅ Frontend completado y pusheado en `sprint-6`

---

## 1. Objetivo
Certificar los 32 CAs de US-007 mediante pruebas E2E y de integración. Enfoque particular en los 7 GAPs QA-Only detectados + validación cruzada de las remediaciones Backend (B1) y Frontend (B2).

## 2. CAs que YA tienen QA PASS (no re-testear salvo regresión)
| CA | Test Existente | Veredicto |
|----|---------------|-----------|
| CA-01 | SSE + test 504 | ✅ PASS |
| CA-06 | `us007-tenant-isolation.spec.ts` | ✅ PASS |
| CA-19 | Tests resiliencia SSE | ✅ PASS |
| CA-28 | `DmnGridManual.spec.ts` (FEEL validation) | ✅ PASS |
| CA-29 | `DmnGridManual.spec.ts` (catch-all) | ✅ PASS |
| CA-31 | `DmnGridManual.spec.ts` (100 rows limit) | ✅ PASS |

## 3. GAPs QA a Certificar (7 GAPs + validación cruzada)

### GAP-01 — CA-02: Rate Limiting + Caché Hash Hit
**Tipo:** E2E / Integration
**Escenario Gherkin:**
```gherkin
Scenario: Rate limiting bloquea generaciones excesivas
  Given un usuario autenticado con rol ROLE_PROCESS_ARCHITECT
  When envía 6 requests de generación DMN en menos de 60 segundos
  Then el request #6 recibe HTTP 429 Too Many Requests
  And el body contiene "remainingSeconds"

Scenario: Caché hash devuelve tabla sin costo LLM
  Given un prompt "Aprobar si monto < 1000" ya cacheado en Redis
  When el usuario envía el mismo prompt
  Then la respuesta llega en menos de 2 segundos (sin SSE)
  And no se invoca el LLM
```
**Endpoints a validar:** `POST /api/v1/dmn/generate` (SSE)
**Archivo sugerido:** `e2e/us007-rate-limiting.spec.ts`

### GAP-02 — CA-03: GC 24h TTL + Minificación XML
**Tipo:** Integration
**Escenario Gherkin:**
```gherkin
Scenario: Borradores expiran después de 24 horas
  Given un borrador DMN creado hace 25 horas
  When el scheduler DmnDraftCleanupScheduler ejecuta
  Then el borrador es eliminado de la tabla ibpms_dmn_drafts

Scenario: XML publicado se minifica sin corrupción
  Given una tabla DMN con XML con espacios en blanco excesivos
  When el usuario publica (seal) la tabla
  Then el XML almacenado no contiene whitespace innecesario
  And el XML minificado es parseable como DMN válido
```
**Endpoints a validar:** `POST /api/v1/dmn/{id}/publish`, `POST /api/v1/dmn/drafts`
**Archivo sugerido:** `e2e/us007-gc-drafts.spec.ts`

### GAP-05 — CA-07: Catch-All + Hit Policy FIRST
**Tipo:** E2E
**Escenario Gherkin:**
```gherkin
Scenario: Tabla DMN publicada tiene Hit Policy FIRST
  Given una tabla DMN generada por IA o manual
  When el usuario la publica
  Then el XML persistido contiene hitPolicy="FIRST"

Scenario: Catch-All es inamovible
  Given una tabla DMN con 5 filas + catch-all
  When el usuario intenta eliminar la última fila (catch-all)
  Then el sistema rechaza la operación
  And la fila catch-all permanece con icono 🔒
```
**Archivo sugerido:** `e2e/us007-hit-policy-catchall.spec.ts`

### GAP-09 — CA-11: Simulador XAI Iluminación
**Tipo:** E2E
**Escenario Gherkin:**
```gherkin
Scenario: Simulador ilumina fila correcta
  Given una tabla DMN con 3 filas (monto < 1000 → Aprobado, monto >= 1000 → Rechazado, catch-all)
  When el usuario ingresa variables de prueba {monto: 500}
  Then la fila #1 se ilumina en verde
  And el panel muestra output: {decision: "Aprobado"}

Scenario: Simulador XAI traduce FEEL a lenguaje humano
  Given una tabla DMN con expresión FEEL "< 1000"
  Then la columna "Explainable DMN" muestra "Si el monto es menor a 1000"
```
**Endpoints a validar:** `POST /api/v1/dmn/{id}/evaluate-test`
**Archivo sugerido:** `e2e/us007-simulator-xai.spec.ts`

### GAP-11 — CA-13: Persistencia Dual PostgreSQL + LocalStorage
**Tipo:** E2E
**Escenario Gherkin:**
```gherkin
Scenario: Borrador se persiste en PostgreSQL
  Given el usuario está iterando un prompt en el chat NLP
  When genera una tabla parcial y no la publica
  Then la tabla se guarda en ibpms_dmn_drafts via POST /api/v1/dmn/drafts

Scenario: Borrador se cachea en LocalStorage
  Given el usuario tiene un borrador activo en la grilla
  When cierra la pestaña accidentalmente y la reabre
  Then la grilla muestra el borrador desde LocalStorage
  And verifica consistencia con PostgreSQL
```
**Archivo sugerido:** `e2e/us007-draft-persistence.spec.ts`

### GAP-13 — CA-15: Endpoint Simulador Funcional
**Tipo:** E2E
**Escenario Gherkin:**
```gherkin
Scenario: Evaluate-test retorna resultado correcto
  Given una DMN publicada con Decision_Ref "risk_matrix"
  When POST /api/v1/dmn/{id}/evaluate-test con body {monto: 5000, mora_dias: 45}
  Then response contiene matched_rule_index, output, all_rules_evaluated
  And matched_rule_index corresponde a la fila correcta

Scenario: Evaluate-test rechaza DMN en estado DRAFT
  Given una DMN en estado DRAFT
  When POST /api/v1/dmn/{id}/evaluate-test
  Then HTTP 409 Conflict con mensaje "Solo DMNs publicadas pueden evaluarse"
```
**Archivo sugerido:** `e2e/us007-evaluate-test.spec.ts`

### GAP-15 — CA-17: Catálogo DMN con Búsqueda Server-Side
**Tipo:** E2E
**Escenario Gherkin:**
```gherkin
Scenario: Catálogo muestra tablas DMN paginadas
  Given existen 25 tablas DMN en el sistema
  When el usuario accede a la Pantalla 4
  Then se muestra una grilla con las primeras 20 tablas
  And existe paginación funcional (página 2 muestra 5 tablas)

Scenario: Buscador filtra por nombre o Decision_Ref
  Given existen tablas "Riesgo Crediticio" y "Aprobación Express"
  When el usuario escribe "riesgo" en el buscador
  Then solo se muestra "Riesgo Crediticio"
  And la búsqueda es server-side (no filtro local)

Scenario: Cada fila muestra estado y metadatos
  Given una tabla DMN activa versión 3 con 12 reglas
  Then la fila muestra: nombre, Decision_Ref, "v3", "✅ ACTIVA", fecha, "12 reglas"
```
**Endpoints a validar:** `GET /api/v1/dmn?status=ACTIVE&search=riesgo&page=1&size=20`
**Archivo sugerido:** `e2e/us007-dmn-catalog.spec.ts`

## 4. Validación Cruzada de Remediaciones B1 + B2

Además de los 7 GAPs QA-Only, el agente QA DEBE validar la correcta implementación de las remediaciones Backend y Frontend:

| CA | Test de Validación Cruzada |
|----|---------------------------|
| CA-05 (PII) | Verificar que el prompt enviado al LLM NO contiene nombres de variables reales (usar logs o interceptor) |
| CA-08 (dot-notation) | Intentar crear DMN con variable `Cliente.Mora` → verificar HTTP 422 |
| CA-09 (50 filas) | Intentar generar tabla con prompt que produzca >50 filas → verificar Hard-Stop |
| CA-14 (Pre-Flight) | Publicar BPMN con BusinessRuleTask + DMN catch-all + Gateway sin rama → verificar Error bloqueante |
| CA-20 (normalización) | Enviar 2 prompts con diferente capitalización → verificar misma respuesta (caché hit) |
| CA-22 (hitPolicy) | Subir XML con hitPolicy="UNIQUE" → verificar HTTP 422 |
| CA-23 (rate limit simulador) | Enviar 21 evaluate-test en 60s → verificar 429 |
| CA-24 (buscador) | Usar Ctrl+F en grilla con 30 filas → verificar que busca en todas, no solo las visibles |
| CA-32 (badge manual) | Editar tabla IA → publicar → verificar badge "Modificada Manualmente" |

## 5. Entregables Esperados
- [ ] 7 archivos E2E `.spec.ts` nuevos.
- [ ] 9 tests de validación cruzada (integrados en los 7 archivos o separados).
- [ ] Reporte de certificación en `.agentic-sync/approval_request_qa_US007.md`.
- [ ] `git commit` y `git push` en rama `sprint-6`.

## 6. Restricciones
- **PROHIBIDO:** Mocks para endpoints Backend — Zero-Mock policy (ADR-010).
- **PROHIBIDO:** Tests que dependan de datos hardcodeados — usar `task-seeder.ts` para data seed.
- Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa_US007.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa_US007.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
