# 🔧 Handoff Backend — US-007 Bloque 1
> **Fecha:** 2026-05-03 | **Rama:** `sprint-6` | **Origen:** Auditoría Arquitectónica US-007
> **Agente destino:** Backend | **Prioridad:** 🔴 ALTA (Paso 2 de 4 — post Infra/BD)
> **Dependencia:** ✅ Infra/BD completado y pusheado en `sprint-6`

---

## 1. Objetivo
Remediar los 12 GAPs Backend detectados en la auditoría forense de US-007 (Generador Cognitivo DMN). Todos los cambios deben respetar la Arquitectura Hexagonal (ADR-001), el motor Camunda 7 embebido (ADR-003), y la estrategia LLM agnóstica (ADR-012).

## 2. Alineación Arquitectónica
| ADR | Impacto | Regla |
|-----|---------|-------|
| ADR-001 | Toda lógica nueva en `domain/` o `application/usecase/` | Puertos+Adaptadores. PROHIBIDO lógica en controladores. |
| ADR-003 | Motor DMN vía API Java interna de Camunda 7 | `DmnSimulatorUseCase` usa `DmnEngine` embebido |
| ADR-012 | Sin SDKs monolíticos de IA | `AiDmnGeneratorPort` → Raw HTTP adapter |
| ADR-010 | Tests obligatorios Unit + Integration | Testcontainers para PostgreSQL real |

## 3. Componentes Existentes (NO tocar sin justificación)
- `AiDmnGeneratorController.java` — SSE endpoint ✅
- `DmnGovernanceController.java` — CRUD + drafts ✅
- `DmnSimulatorController.java` — evaluate-test ✅
- `AiDmnGeneratorUseCase.java` — UseCase SSE ✅
- `DmnGovernanceUseCase.java` — UseCase gobernanza ✅
- `DmnSimulatorUseCase.java` — UseCase simulador ✅
- `AiDmnGeneratorPort.java` — Puerto out agnóstico ✅
- `DmnDraftCleanupScheduler.java` — GC drafts ✅
- `DmnGarbageCollectorJob.java` — GC alternativo ✅

## 4. GAPs a Remediar (12 GAPs)

### GAP-04 — CA-05: Seudonimización PII del Prompt
**Archivo nuevo:** `application/service/PromptPiiScrubber.java`
**Acción:**
- Crear servicio que intercepte el prompt ANTES de enviarlo al LLM.
- Reemplazar nombres de variables con alias genéricos (`var_1`, `var_2`, etc.) antes del envío.
- Mantener un mapa inverso para reconstruir la tabla DMN post-respuesta.
- Inyectar en `AiDmnGeneratorUseCase` antes de la llamada al `AiDmnGeneratorPort`.
- Extraer `authorHash` desde el JWT vía `SecurityContextUtils` para la auditoría.
**Test:** Unit test `PromptPiiScrubberTest.java` con mínimo 3 escenarios (sin PII, con PII, con nombres Unicode).

### GAP-06 — CA-08: Variables planas + prohibición Date-Math
**Archivo a modificar:** `DmnGovernanceUseCase.java` o nuevo `DmnVariableValidator.java`
**Acción:**
- Validar que TODAS las variables del XML DMN sean planas (sin dot-notation: `Cliente.Mora` → rechazar con 422).
- Validar que NO existan funciones Date-Math en expresiones FEEL (`date and time()`, `duration()`, `now()` → rechazar con 422).
- Aplicar `lowercase()` en todas las comparaciones de texto FEEL generadas.
**Test:** Unit test con XMLs que contengan dot-notation y date-math → esperar rechazo.

### GAP-07 — CA-09: Límites cognitivos + Overlap Check
**Archivo a modificar:** `DmnGovernanceUseCase.java` o nuevo `DmnRuleValidator.java`
**Acción:**
- Implementar Hard-Stop de 50 filas para tablas generadas por IA (100 para modo manual).
- Validar outputs atómicos (máximo 1 output compuesto en V1).
- Implementar Overlap Check básico: detectar rangos numéricos que se solapen entre filas adyacentes.
- Truncar prompt al token limit (4096 chars) antes de enviar al LLM.
**Test:** Unit test con tabla de 51 filas, con overlaps, y con prompt excedido.

### GAP-12 — CA-14: Pre-Flight Catch-All vs BPMN Gateway
**Archivo a modificar:** Pre-Flight Analyzer existente en US-005 (buscar en `application/usecase/bpmn/`)
**Acción:**
- Agregar regla de validación en el Pre-Flight del BPMN:
  1. Para cada Business Rule Task con `Decision_Ref`, consultar la DMN referenciada.
  2. Si la DMN tiene fila Catch-All activa, verificar que el elemento INMEDIATAMENTE posterior sea un Exclusive Gateway.
  3. Si el Gateway no tiene rama que enrute el output "Revisión Humana" → Error ❌ bloqueante.
  4. Si no hay Gateway posterior (conecta directo a otra tarea) → Warning ⚠️.
- **Endpoint DMN a consumir:** `GET /api/v1/dmn/{id}` (ya existe) para obtener si tiene catch-all.
**Test:** Integration test con BPMN mock que contenga BusinessRuleTask + Gateway vs BusinessRuleTask sin Gateway.

### GAP-14 — CA-16: Invalidación Caché Redis por `FORM_SCHEMA_CHANGED`
**Archivos nuevos:**
- `application/port/in/FormSchemaChangedListener.java` — Puerto de entrada
- `infrastructure/listener/FormSchemaChangedRabbitListener.java` — Adaptador RabbitMQ
**Acción:**
- Crear `@RabbitListener` que escuche en la queue `ibpms.dmn.form-schema-invalidation`.
- Al recibir evento `{form_id, tenant_id, changed_fields[]}`:
  1. Buscar en Redis TODAS las claves de caché DMN que contengan el `form_id` en su hash.
  2. Purgar ÚNICAMENTE esas claves (invalidación quirúrgica, NO nuke total).
  3. Registrar log INFO con cantidad de claves purgadas.
- **Publicador:** El publicador del evento `FORM_SCHEMA_CHANGED` vive en el módulo de formularios (US-003). Si no existe aún, documentar la dependencia en el `approval_request` como bloqueador parcial y crear solo el consumer.
**Test:** Integration test con Testcontainers (RabbitMQ + Redis) simulando publicación de evento y verificando purga.

### GAP-16 — CA-18: OpenAPI Annotations Completas
**Archivos a modificar:** `AiDmnGeneratorController.java`, `DmnGovernanceController.java`, `DmnSimulatorController.java`
**Acción:**
- Agregar `@Tag(name = "DMN")`, `@Operation(summary = "...")`, `@ApiResponse(...)` en TODOS los endpoints DMN.
- Endpoints a documentar:
  - `POST /api/v1/dmn` → Crear DMN
  - `GET /api/v1/dmn` → Listar con filtros
  - `GET /api/v1/dmn/{id}` → Detalle
  - `PUT /api/v1/dmn/{id}` → Actualizar (genera V2)
  - `POST /api/v1/dmn/{id}/publish` → Publicar
  - `POST /api/v1/dmn/{id}/rollback` → Rollback
  - `POST /api/v1/dmn/{id}/evaluate-test` → Simulador
  - `POST /api/v1/dmn/drafts` → Crear/actualizar borrador
  - `DELETE /api/v1/dmn/drafts/{id}` → Purgar borrador
  - `POST /api/v1/dmn/{id}/archive` → Archivar
**Test:** Verificar que Swagger UI muestra todos los endpoints agrupados bajo "DMN".

### GAP-17 — CA-20: Normalización Prompt para Caché
**Archivo a modificar:** `AiDmnGeneratorUseCase.java` o nuevo `PromptNormalizer.java`
**Acción:**
- Antes de calcular el hash de caché, normalizar el prompt:
  1. `toLowerCase()`
  2. `trim()` + colapsar espacios múltiples a uno
  3. Eliminar signos de puntuación irrelevantes (`.` final, `,` sueltas)
- Resultado: `"Aprobar si MONTO < 1000."` y `"aprobar si monto < 1000"` → mismo hash.
**Test:** Unit test con 5 variantes del mismo prompt → mismo hash resultante.

### GAP-18 — CA-21: Validación Post-Minificación XML
**Archivo a modificar:** `DmnGovernanceUseCase.java` (en el flujo de publish/seal)
**Acción:**
- DESPUÉS de minificar el XML (eliminar whitespace), ejecutar un parse de validación contra el schema DMN de Camunda.
- Si el parse falla → CANCELAR minificación, guardar XML ORIGINAL, registrar WARNING en logs.
- NUNCA hacer COMMIT de XML minificado que no supere validación.
**Test:** Unit test con XML intencionalmente mal formado post-minificación → fallback a original.

### GAP-19 — CA-22: Rechazo XML Upload con hitPolicy != FIRST
**Archivo a modificar:** `DmnGovernanceUseCase.java` (en el flujo de XML upload)
**Acción:**
- Al recibir XML upload (Modo Desarrollador), parsear y verificar `hitPolicy` de `<decisionTable>`.
- Si `hitPolicy` != `FIRST` → HTTP 422 con mensaje localizado.
- Si no existe atributo `hitPolicy` → inyectarlo automáticamente como `FIRST`.
**Test:** Unit test con XMLs con UNIQUE, COLLECT, ANY, sin atributo → 3 rechazos + 1 inyección.

### GAP-20 — CA-23: Rate Limiting Simulador 20/min
**Archivo a modificar:** `DmnSimulatorController.java` o nuevo `DmnSimulatorRateLimiter.java`
**Acción:**
- Implementar rate limiter INDEPENDIENTE del de generación IA (CA-02).
- Máximo 20 evaluaciones/minuto por usuario autenticado (extraer subject del JWT).
- Si excede → HTTP 429 con `remainingSeconds` en body.
**Test:** Unit test simulando 21 requests en 60s → request 21 recibe 429.

### GAP-26 — CA-32: Badge "Modificada Manualmente" + Versión V2
**Archivo a modificar:** `DmnGovernanceUseCase.java`
**Acción:**
- Al recibir un `PUT /api/v1/dmn/{id}` con cambios manuales sobre una tabla generada por IA:
  1. Incrementar versión obligatoriamente (V1 → V2).
  2. Actualizar columna `source` a `NLP_MODIFIED` (o `MANUAL` si fue creación manual pura).
  3. Registrar en `ibpms_dmn_audit_log` con `source_badge = "Modificada Manualmente"`.
**Test:** Unit test: tabla IA V1 → edición manual → verificar V2 con source `NLP_MODIFIED`.

### GAP-02 — CA-03: Verificar GC TTL 24h
**Archivo a verificar:** `DmnDraftCleanupScheduler.java`
**Acción:**
- Confirmar que el scheduler purga drafts con `expires_at < NOW()`.
- Confirmar que el scheduler ejecuta diariamente (o cada hora).
- Confirmar que la minificación XML se ejecuta en el flujo de publish (no en drafts).
**Test:** Verificar configuración `@Scheduled` con cron correcto.

## 5. Entregables Esperados
- [ ] 12 GAPs remediados con código productivo.
- [ ] Tests unitarios/integración para cada GAP.
- [ ] Compilación limpia (`mvn compile` exit 0).
- [ ] `git commit` y `git push` en rama `sprint-6`.

## 6. Restricciones
- **PROHIBIDO:** Lógica de negocio en controladores (ADR-001).
- **PROHIBIDO:** Imports de `jakarta.persistence.*` en `domain/`.
- **PROHIBIDO:** SDKs de IA (LangChain, Spring AI) — solo Raw HTTP via `AiDmnGeneratorPort`.
- **PROHIBIDO:** H2 en tests — usar Testcontainers.

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend_US007.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend_US007.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
