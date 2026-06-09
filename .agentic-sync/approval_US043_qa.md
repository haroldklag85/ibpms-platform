# Aprobación Arquitectónica — Plan de Remediación US-043 (QA)

**Fecha:** 2026-04-19
**Emisor:** Arquitecto Líder
**Destino:** Agente QA (Conversación `5e33cd7d-ab8d-43f3-8434-2d148a39b050`)
**Veredicto:** 🟢 **APROBADO CON 3 OBSERVACIONES**

---

## ✅ Decisión

El plan cubre las 6 suites del handoff `handoff_remediation_US043_qa.md` (SUITE 3+4 consolidadas en un solo archivo, aceptable). Se aprueba para ejecución inmediata.

---

## ⚠️ Observaciones Obligatorias

### OBS-1: Ubicación de `PmoSettings.spec.ts`
El plan propone `frontend/src/views/admin/PMO/`. Verificar la convención dominante del proyecto — si los tests existentes están en `frontend/src/tests/` (ej: `frontend/src/tests/components/TaskViewerModal.spec.ts`), ubicar el archivo allí: `frontend/src/tests/views/PmoSettings.spec.ts`.

### OBS-2: Threshold del Scheduler — usar 80%, NO 85%
El plan menciona "consumo > 85%" pero el código real del `SlaEarlyWarningScheduler` usa `percentage >= 0.80`. Los tests deben:
- Caso positivo: mockear tarea con **80.01%** consumido → verificar flag `isSlaAtRisk = true`.
- Caso negativo: mockear tarea con 50% → verificar que NO se flaggea.
- Caso límite: mockear tarea con **exactamente 80%** → verificar que SÍ se flaggea (boundary).

### OBS-3: Test de idempotencia explícito
Incluir un caso de test concreto donde la tarea ya tenga `getVariableLocal("isSlaAtRisk") = true` y verificar que:
- No se vuelve a llamar `setVariableLocal`.
- No se publica un segundo `SlaAtRiskEvent`.

---

## 📋 Checklist de Cierre Post-Ejecución

- [ ] `CustomBusinessCalendarTest.java` — 8+ asserts cubriendo `PT4H`, `P2D`, `4h`, `null`, `garbage`.
- [ ] `SlaTimerEngineIntegrationTest.java` — test de bypass `SYSTEMIC_24_7` agregado.
- [ ] `SlaAdminControllerTest.java` — MockMvc: `GET/PUT /business-hours`, `POST/GET/DELETE /holidays`, `POST /apply` (200 y 202).
- [ ] `SlaEarlyWarningSchedulerTest.java` — threshold 80%, idempotencia, evento publicado.
- [ ] `PmoSettings.spec.ts` — 3 tests (onMounted fetch, recálculo 202, delete holiday).
- [ ] Ejecución verde: `mvn test -Dtest=...` + `npm run test`.

---

**Proceda con la ejecución. Reporte al completar.**
