# 🧠→🕵️ Handoff: ARQUITECTO LÍDER → QA E2E
# T-20: Regresión Inmutable — Ejecución de Suite J-04 Zero-Mock

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA - PLAYWRIGHT / SRE
**Fecha:** 2026-05-12T21:45:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** T-21 (Saneamiento Liquibase e Identidad de Spring) completada.

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3, 4)
cat .cursorrules

# 2. Skill principal del agente receptor (QA Playwright)
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes (Testing estricto sobre host nativo)
cat docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.
>
> ⚠️ **LEY GLOBAL 4 — Inmutabilidad de Regresión:** Tienes ESTRICTAMENTE PROHIBIDO modificar las aserciones, expect o lógica de negocio de los 18 tests del J-04 para forzarlos a pasar. "Se arregla el código, NUNCA el test".

---

## 🔬 Diagnóstico del Arquitecto

Durante la Iteración 7.1, se estabilizó la infraestructura resolviendo el bloqueante `BeanDefinitionOverrideException` y saneando las migraciones de Liquibase (`task_drafts`). Si bien el backend ahora inicia exitosamente y cuenta con un "Green Build" a nivel de compilación e integración, es imperativo certificar que los componentes de la interfaz de usuario (Playwright E2E) no sufrieron regresiones debido a los ajustes masivos del backend.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Riesgo de Regresión UI | `frontend/e2e/certification/` | Los 18 tests E2E de Playwright (J-04) no se han ejecutado tras la resolución del P0 del FormAdapter y el saneamiento Liquibase. |
| Inestabilidad de red E2E | `frontend/` | Falsos positivos por tiempo de espera en el servidor local. Requiere parametrización `--retries=1` según el ADR-014. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Ejecución Exhaustiva Playwright (Zero-Mock)

**Archivo:** `frontend/` (Directorio de trabajo)

Ejecuta la suite completa de certificación J-04. Es mandatorio mapear TODOS los errores sin aplicar "Fail-Fast". Adicionalmente, aplica un retry para mitigar problemas puros de latencia.

```bash
# Snippet prescriptivo — NO es pseudocódigo, es código ejecutable
cd frontend
npx playwright test e2e/certification/ --retries=1
```

### Paso 2: Auditoría de Exceptions Silenciosas

**Archivo:** Consolas locales y Logs (Backend/RabbitMQ)

Una vez completada la prueba (así resulte en `passed`), inspecciona el log del proceso de Backend (Spring Boot) buscando `NullPointerException` o caídas en hilos asíncronos que la UI haya ignorado.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Ejecución Exhaustiva Total | El output del comando `npx playwright test` procesa los 18 tests, independientemente de si fallan o pasan. |
| 2 | Inmutabilidad de Pruebas (LG-04) | `git status` y `git diff` arrojan vacío sobre la carpeta `frontend/e2e/certification/`. Ningún archivo modificado. |
| 3 | Reporte de Daños Consolidado | Se entrega al Arquitecto Líder el sumario de Playwright y el estado de salud libre de excepciones silenciosas del backend. |
| 4 | Build/Compilación exitosa + Commit en rama | Entrega del reporte y (si aplica por actualizaciones documentales permitidas) commit en rama. (Nota: Según alineación, NO se actualiza `task.md` por QA, se pasa el log al Arquitecto). |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Cambiar al frontend: `cd frontend`
2. Ejecutar suite E2E: `npx playwright test e2e/certification/ --retries=1`
3. Revisar logs del servidor Backend buscando excepciones de hilos asíncronos.
4. Documentar los resultados (Mapa completo de daños) en el chat para el Arquitecto.
5. Commit: `git add . && git commit -m "test(regression): ejecucion de certificacion J-04 (T-20)" && git push` (solo si hubieron cambios en logs permitidos).

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA - PLAYWRIGHT / SRE.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/adr_010_testing_pyramid_governance.md
6. cat .agentic-sync/T-20_QA_Regression_J04_Handoff.md

TU MISIÓN:

1. Ingresar al entorno UI: `cd frontend`
2. Ejecutar regresión Playwright: `npx playwright test e2e/certification/ --retries=1`
3. Analizar los logs nativos de la terminal del Backend para validar que no existieron excepciones ocultas (silenciosas).
4. Reportar el resultado de los 18 tests directamente al Arquitecto Líder (Mapa completo de daños).
5. Build/Compile: `npx playwright test e2e/certification/ --retries=1`
6. Commit: `git add . && git commit -m "test(regression): resultados de T-20 J-04" && git push` (solo si generaste algún reporte físico en el repositorio).

REGLAS INQUEBRANTABLES:
- Inmutabilidad de Regresión (Ley Global 4): Tienes ESTRICTAMENTE PROHIBIDO alterar cualquier `expect` o `assert` dentro de las especificaciones de los tests. "Se arregla el código, nunca el test".
- No Fail-Fast: Deja que todos los tests corran hasta el final para mapear el 100% de la superficie.
- Trazabilidad Inversa (Ley Global 3): Si por mandato del usuario modificas código, no olvides insertar `// @Traceability: US-XXX, CA-XX`.
```
