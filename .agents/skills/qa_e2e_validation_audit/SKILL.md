---
name: qa_e2e_validation_audit
description: >
  Skill obligatoria para el Agente QA/DevOps. Exige ejecución empírica de TODA LA PIRÁMIDE DE PRUEBAS 
  (Unitarias, Integración, E2E) usando JUnit, Vitest y Playwright. Incluye la adjunción de evidencia 
  verificable (screenshots/video/logs) y prohibición absoluta de reportar "pass" sin pruebas de supervivencia.
triggers:
  - "Cuando el agente QA termine de ejecutar un lote de pruebas y se prepare para reportar resultados al Arquitecto o al Humano."
  - "Al recibir un handoff de Backend o Frontend solicitando validación E2E."
  - "Antes de certificar una rama como 'lista para merge' hacia main."
applies_to:
  - QA/DevOps
---

# MANDATO DE VALIDACIÓN EMPÍRICA Y DISCIPLINA SRE (QA / E2E)

🛑 **REGLA DE SUPERVIVENCIA CERO-CONFIANZA (ZERO-TRUST TESTING)**

A partir de este momento, TIENES ESTRICTAMENTE PROHIBIDO asumir que el código funciona solo porque "se ve bien" o porque el agente Backend/Frontend dijo que compiló. Tu flujo de trabajo cambia obligatoriamente a validación empírica verificable:

## 1. PROHIBIDO EL REPORTE CIEGO (PIRÁMIDE DE TESTING COMPLETA)

Antes de reportar cualquier resultado al Arquitecto o al Humano, **DEBES** ejecutar la suite de pruebas correspondiente a tu auditoría, respetando el ADR 011:

1. **Frontend Unit/Components (Vitest):** `cd frontend && npm run test:unit`
2. **Backend Unit/Integration (JUnit/Mockito):** `cd backend/ibpms-core && mvn test`
3. **Plataforma E2E (Playwright):** `npx playwright test --reporter=html`
   *   **Tuning de Hardware (GPU/VRAM):** Si ejecutas en la estación local OMEN con GPU dedicada, debes aplicar obligatoriamente la aceleración por hardware cargando las directivas del skill en [gpu_acceleration_tuning/SKILL.md](file:///home/haroltandrsgmezagu/.gemini/antigravity-ide/skills/gpu_acceleration_tuning/SKILL.md) o [gpu_acceleration_tuning/SKILL.md](file:///home/haroltandrsgmezagu/proyectos/ibpms-platform/.agents/skills/gpu_acceleration_tuning/SKILL.md) e inyectando `PLAYWRIGHT_USE_GPU=true` en tu comando.

Este comando ejecutará los tests Playwright que verificarán:
*   Flujos de usuario completos (login, navegación, CRUD, formularios).
*   Respuestas de red reales contra el backend Docker activo.
*   Renderizado correcto de componentes Vue en el viewport.
*   Accesibilidad básica (contraste, roles ARIA, tabulación).

## 2. AUDITORÍA DE EVIDENCIA (GATEKEEPER DE RESULTADOS)

Inmediatamente después de ejecutar las pruebas, debes verificar la salida:

*   Si observas tests en **ROJO** (`FAILED`), `TimeoutError`, `ElementNotFound` o cualquier error de aserción, **SE TE PROHÍBE REPORTAR "PASS"**.
*   Debes documentar cada fallo con:
    1.  **Nombre del test fallido** y el archivo donde vive.
    2.  **Screenshot o video** del momento del fallo (Playwright los genera automáticamente en `test-results/`).
    3.  **Log de consola del navegador** si aplica (errores JS, llamadas de red fallidas).
*   Solo puedes reportar éxito cuando la consola muestre:
    > `X passed` con **0 failed**

## 3. ADJUNCIÓN OBLIGATORIA DE EVIDENCIA

Todo reporte de QA entregado al Arquitecto o al Humano **DEBE** incluir:

| Evidencia | Obligatoria | Formato |
|-----------|-------------|---------|
| Resumen de tests (passed/failed/skipped) | ✅ Sí | Texto en el reporte |
| Screenshots de tests fallidos | ✅ Sí (si hay fallos) | Archivos `.png` en `test-results/` |
| Video de flujos críticos | 🟡 Recomendado | Archivos `.webm` en `test-results/` |
| Logs de consola del navegador | ✅ Sí (si hay errores JS) | Texto en el reporte |
| Reporte HTML de Playwright | ✅ Sí | `playwright-report/index.html` |

## 4. LEY DE CORRESPONDENCIA GHERKIN (Test vs User Story)

Si durante tu validación detectas que:
*   Un Criterio de Aceptación del SSOT (archivo de Épica en `docs/requirements/epics/epic_X_*.md`, navegable desde `docs/requirements/v1_user_stories_index.md`) **NO tiene test E2E correspondiente**, debes reportarlo como "Cobertura Faltante" — no ignorarlo silenciosamente.
*   Un test E2E valida comportamiento que **NO existe** en el Gherkin del SSOT, debes reportarlo como "Test Fantasma" — podría ser una alucinación del agente que lo creó.

## 5. PROHIBICIÓN DE CERTIFICACIÓN SIN BACKEND VIVO

Tienes **ESTRICTAMENTE PROHIBIDO** ejecutar tests E2E contra mocks estáticos o un backend apagado. Antes de correr la suite:
1.  Verifica que el entorno nativo esté activo. **Tienes estrictamente prohibido usar `docker compose up ibpms-core`** (Ley Global 2).
2.  Levanta la infraestructura E2E y el backend nativamente usando el script oficial: `start-e2e.bat` (o `.sh`).
3.  Espera a que Tomcat reporte puerto 8080 en la consola nativa.
4.  Si tras 2 intentos el backend no arranca en el Host, **NO ejecutes tests**. Reporta el bloqueo al Humano en `.agentic-sync/infra_blocker_[fecha].md`.

**Tus tests NO son válidos hasta que Playwright lo demuestre con evidencia física.**
Una vez que valides los resultados, documenta el reporte en `.agentic-sync/qa_report_[US-XXX].md`, asegúrate de estar en la rama correspondiente (`sprint-X/...`), realiza `git commit` y notifica al Humano Enrutador.
