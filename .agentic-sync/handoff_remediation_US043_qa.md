# Handoff de Remediación: US-043 (SLA & Business Calendar) — Agente QA

**Fecha:** 2026-04-19
**Emisor:** Arquitecto Líder (Auditoría Iteración 2)
**Destino:** Agente QA (Testing E2E & Integración)
**Prioridad:** ALTA — Cierre de Iteración 2

---

## 📋 Contexto Ejecutivo

La US-043 tiene 2 tests de integración existentes en `SlaTimerEngineIntegrationTest.java` que validan el Time-Travel de fin de semana (CA-1) y el threshold del 80% (CA-6). Tras la remediación del equipo de desarrollo, se requiere ampliar la cobertura para cubrir los nuevos artefactos.

## 📍 Test Existente (Baseline)
| Test | CA | Estado |
|---|---|---|
| `testBusinessCalendar_SlaTimerSkipsWeekends()` | CA-1 | ✅ Existente |
| `testSlaThresholdWarning_80PercentConsumed_ReturnsTrue()` | CA-6 | ✅ Existente |

---

## 🧪 Directivas de Testing

### SUITE 1 — CA-1: Validación del Parser ISO 8601 (Unit Test)
**Archivo NUEVO:** `CustomBusinessCalendarTest.java` (unit test, no requiere Spring Context)
**Objetivo:** Verificar que el nuevo parser de duración resuelve correctamente todos los formatos soportados.
**Casos:**
1. `parseDurationToHours("PT4H")` → retorna `4`
2. `parseDurationToHours("PT8H")` → retorna `8`
3. `parseDurationToHours("P2D")` → retorna `48`
4. `parseDurationToHours("PT30M")` → retorna `0` → fallback a `4` (default)
5. `parseDurationToHours("4h")` → retorna `4`
6. `parseDurationToHours("8")` → retorna `8`
7. `parseDurationToHours(null)` → retorna `4` (default)
8. `parseDurationToHours("garbage")` → retorna `4` (default)

### SUITE 2 — CA-2: Bypass Sistémico (Validar que no regresionó)
**Archivo:** `SlaTimerEngineIntegrationTest.java` (ampliar)
**Caso:**
- Invocar `resolveDuedate("SYSTEMIC_24_7", baseDate)` → verificar que retorna `baseDate + 24h` exactas, sin pausas de fin de semana.

### SUITE 3 — CA-3: Recálculo Retroactivo vía Controller (Integration)
**Archivo NUEVO o ampliar:** `SlaAdminControllerTest.java`
**Casos:**
1. `POST /api/v1/admin/sla/apply?applyRetroactively=false` → retorna `200 OK`.
2. `POST /api/v1/admin/sla/apply?applyRetroactively=true` → retorna `202 Accepted` + body con `"Recálculo en progreso"`.

### SUITE 4 — CA-4/CA-5: CRUD completo Business Hours y Holidays (Integration)
**Archivo NUEVO o ampliar:** `SlaAdminControllerTest.java`
**Casos Business Hours:**
1. `GET /api/v1/admin/sla/business-hours` → retorna defaults (`08:00`, `17:00`, `America/Bogota`).
2. `PUT /api/v1/admin/sla/business-hours` con body `{startTime: "09:00", endTime: "18:00", timezone: "America/New_York"}` → retorna entidad persistida.
3. `GET /api/v1/admin/sla/business-hours` post-PUT → retorna los valores actualizados.

**Casos Holidays:**
1. `POST /api/v1/admin/sla/holidays` con body `{holidayDate: "2026-12-25", description: "Navidad"}` → retorna `200` + entidad con UUID.
2. `GET /api/v1/admin/sla/holidays` → lista incluye "Navidad".
3. `DELETE /api/v1/admin/sla/holidays/{uuid}` → retorna `204 No Content`.
4. `GET /api/v1/admin/sla/holidays` post-DELETE → "Navidad" ya no aparece.

### SUITE 5 — CA-6: Early Warning Scheduler (Unit/Mock Test)
**Archivo NUEVO:** `SlaEarlyWarningSchedulerTest.java`
**Casos:**
1. Mockear `TaskService` retornando tareas con 85% consumido → verificar que se invoca `setVariable("isSlaAtRisk", true)`.
2. Mockear tareas con 50% consumido → verificar que NO se flaggean.
3. Mockear tareas ya flaggeadas (`isSlaAtRisk = true`) → verificar que no se vuelven a flaggear (idempotencia).
4. Verificar que `SlaAtRiskEvent` se publica vía `ApplicationEventPublisher`.

### SUITE 6 — Frontend (Vitest) [Si aplica]
**Archivo:** `PmoSettings.spec.ts`
**Casos:**
1. Mock API: `onMounted` llama a `GET /admin/sla/business-hours` y `GET /admin/sla/holidays`.
2. Mock API: Click en "Actualizar SLA" con toggle ON → llama a `POST /admin/sla/apply?applyRetroactively=true` → modal 202 se muestra.
3. Mock API: Click en botón eliminar feriado → llama a `DELETE /admin/sla/holidays/{id}`.

---

## ✅ Criterio de Aceptación QA (DoD)
- [ ] 8+ unit tests para el parser ISO 8601 del `CustomBusinessCalendar`.
- [ ] 4+ integration tests para CRUD de `SlaAdminController`.
- [ ] 4+ tests para `SlaEarlyWarningScheduler` (mocks).
- [ ] Tests existentes (`SlaTimerEngineIntegrationTest`) no regresionan.
- [ ] Compilación y ejecución de suite verde.
