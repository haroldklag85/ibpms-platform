# 🧠→🕵️ Handoff: Arquitecto Líder → QA E2E
# Certificación Final T-06: Workdesk Delegation UI (CU-HEX-06 / CU-HEX-07)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🕵️ QA - E2E]
**Fecha:** 2026-05-11T23:03:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 ALTA (Cierre de Épica US-001)
**Dependencia:** Frontend T-06 Fix completado (Commit 555d7135)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal de certificación QA (Zero-Mock enforcement)
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Reporte anterior con los tests skipeados
cat .agentic-sync/qa_report_us001_hexagonal_certification.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO log o reporte generado DEBE incluir la trazabilidad hacia US-001, CA-04. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

En el ciclo anterior, los tests `CU-HEX-06` y `CU-HEX-07` fueron omitidos (SKIP) porque el DOM no contenía el atributo `data-testid="delegation-dropdown"` ni `workdesk-container`.

El agente de Frontend acaba de reportar la solución y ha integrado los selectores en el commit `555d7135`. No hay más excusas técnicas; el DOM está listo y la base de datos ya fue sembrada (T-21) con delegados reales para el usuario E2E.

| Test Pendiente | Selector Agregado | Acción a Validar |
|----------------|-------------------|------------------|
| CU-HEX-06 | `data-testid="delegation-dropdown"` | Verificar que el dropdown existe y carga la lista de delegantes por red (sin mocks). |
| CU-HEX-07 | `data-testid="delegation-dropdown"` | Al seleccionar un usuario, la UI transiciona a modo `DELEGATED` y aparece el Toast/Banner de alerta. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Ejecutar la suite completa para US-001

Asegúrate de que la infraestructura local (Postgres, RabbitMQ, Redis) y el Backend nativo estén corriendo.

**Archivo:** `frontend/e2e/certification/us001-hexagonal-compliance.e2e.spec.ts`

Ejecuta el runner de Playwright para este archivo específico. NO habilites interceptores HTTP (`route.fulfill()`); el test debe transitar hasta la BD.

```bash
cd frontend
npx playwright test e2e/certification/us001-hexagonal-compliance.e2e.spec.ts --project=chromium
```

### Paso 2: Evaluar manejo de Toast (Zero-Alert)

El test `CU-HEX-07` (o uno nuevo si lo consideras) debe validar que, si se intenta delegar a un usuario no autorizado (ej. simulando un 403), ya NO aparezca un `window.alert()`. Debe validarse que el mensaje de error se renderiza en la UI a través del Toast (`store.errorMessage`).

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | 7/7 Tests Passed | Salida exitosa de Playwright (`Exit code 0`) |
| 2 | Selectores Encontrados | Logs de Playwright no muestran fallos de "Timeout waiting for selector `delegation-dropdown`" |
| 3 | Cero `route.fulfill()` | Verificación visual del código del spec y logs de red reales |
| 4 | Generar Reporte | Archivo Markdown de certificación generado en `.agentic-sync/` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Confirmar conectividad al Backend (`curl http://localhost:8080/actuator/health`).
2. Ejecutar suite Playwright de certificación.
3. Analizar la salida (especialmente los tests 6 y 7 que antes estaban SKIP).
4. Generar reporte final en: `.agentic-sync/qa_final_us001_closure.md`.
5. Commit de cualquier ajuste en los specs (si aplicó): `git add . && git commit -m "test(US-001): re-enable CU-HEX-06/07 post frontend fix" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de [🕵️ QA - E2E].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agentic-sync/qa_report_us001_hexagonal_certification.md
4. cat .agentic-sync/handoff_qa_T06_final_certification.md

TU MISIÓN:

1. Ejecutar la re-certificación de la US-001, enfocado en los tests CU-HEX-06 y CU-HEX-07 (Workdesk Delegation) que fueron skipeados anteriormente.
2. Comando: cd frontend && npx playwright test e2e/certification/us001-hexagonal-compliance.e2e.spec.ts --project=chromium
3. Generar el reporte definitivo del cierre en .agentic-sync/qa_final_us001_closure.md detallando el PASS de los 7 tests.
4. Commit si hubo modificaciones al test: git commit -m "test(US-001): assert full compliance" && git push

REGLAS INQUEBRANTABLES:
- PROHIBIDO saltarse la validación real. Si falla el selector 'delegation-dropdown', reportarlo inmediatamente al Arquitecto.
- DEBES verificar que los requests HTTP van hacia http://localhost:8080 y no están siendo mockeados en memoria.
- OBLIGATORIO: El reporte debe incluir la matriz con los 7 tests en PASS.
```
