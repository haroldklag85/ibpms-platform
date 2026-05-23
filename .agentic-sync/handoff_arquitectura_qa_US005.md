# 🧠→🕵️ Handoff: Arquitecto Líder → QA E2E
# US-005-RESOLVED: Certificación de Sandbox (CORS Fix)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA E2E
**Fecha:** 2026-05-23T17:15:00-05:00
**Sprint:** V1 — Certificación Zero-Mock
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff actual (Este documento)
cat .agentic-sync/handoff_arquitectura_qa_US005.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-005, CA-63`. Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

El equipo de backend ha aplicado el parche quirúrgico solicitado en el handoff previo. La cabecera `X-Sandbox-Mode` ya es aceptada y expuesta en el pre-flight CORS. 

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Bloqueo CORS Resuelto | `SecurityConfig.java:94` | Se inyectó `CorsConfigurationSource` habilitando `X-Sandbox-Mode`. Se ha validado la compilación Zero-Mock. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Re-ejecutar la Suite E2E de Sandbox

**Archivo:** `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts` (u homólogo)

Ejecutar la suite completa de Playwright enfocada en la Pantalla 6 (BPMN Modeler) para validar empíricamente que el botón "Probar en Sandbox" ya no recibe un HTTP 403.

```typescript
// Snippet prescriptivo — NO es pseudocódigo, es código ejecutable
// Asegurar que la prueba aserte el HTTP 200 OK del endpoint de Sandbox
// @Traceability: US-005, CA-63
const response = await page.waitForResponse(response => 
  response.url().includes('/api/v1/design/processes/sandbox-simulate') && response.status() === 200
);
expect(response.ok()).toBeTruthy();
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El test E2E de Sandbox pasa exitosamente en Chromium | `npx playwright test` (o comando equivalente) muestra PASS |
| 2 | No existen errores de red (CORS o 403) en los logs de Playwright | Inspección visual de consola / reporte HTML |
| 3 | Build/Compilación exitosa + Commit en rama | Ejecución limpia del pipeline y commit |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Ejecutar las pruebas E2E con Playwright: `cd frontend && npx playwright test`
2. Generar reporte: `npx playwright show-report`
3. Commit: `git add . && git commit -m "test(e2e): verificar fix CORS para Sandbox [US-005]" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat .agentic-sync/handoff_arquitectura_qa_US005.md

TU MISIÓN:

1. Ejecutar la recertificación E2E de la US-005 enfocada en el flujo de Sandbox.
2. Validar que el error HTTP 403 CORS ha sido mitigado.
3. Build/Compile: `cd frontend && npx playwright test`
4. Commit: `git add .; git commit -m "test(e2e): verificar fix CORS en Sandbox [US-005]"`

REGLAS INQUEBRANTABLES:
- DEBES verificar el resultado real (PASS/FAIL) de las pruebas de Playwright.
- PROHIBIDO modificar el código backend o de infraestructura durante esta fase.
- PROHIBIDO cerrar la US si aún hay fallos de red hacia el servidor backend.
```
