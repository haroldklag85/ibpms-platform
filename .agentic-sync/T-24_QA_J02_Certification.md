# 🧠→🤖 Handoff Arquitectónico: Certificación QA J-02 y Re-Certificación J-04
# T-24: Zero-Mock V2 Testing Gateway

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA E2E
**Fecha:** 2026-05-13
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🔴 Crítica (Bloqueante para Merge)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de iniciar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3, 4)
cat .cursorrules

# 2. Skills principales del agente QA
cat ibpms-platform/.agents/skills/qa_automation/SKILL.md

# 3. ADRs relevantes (Política Zero-Mock)
cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
```

---

## 🔬 Diagnóstico y Contexto del Arquitecto

El fallo de infraestructura local (`PSSecurityException`) ha sido solventado definitivamente mediante la actualización de políticas de ejecución en PowerShell. El entorno Docker ha sido purgado de artefactos UAT y efímeros, garantizando exclusividad para el entorno E2E.
Adicionalmente, los carriles paralelos de Backend y Frontend han finalizado exitosamente el "Wiring" de persistencia **Zero-Mock V2** para el ecosistema J-02 (Low-Code: BPMN y DMN).

**Estado Actual:**
- **J-04 (Workdesk/Kanban):** Pendiente de recertificación (previamente abortada).
- **J-02 (BPMN/DMN):** Ensamblado, trazado e inyectado con llamadas Axios y repositorios JPA reales, a la espera de certificación E2E.

---

## 🎯 Instrucciones Quirúrgicas para el Agente QA

### Paso 1: Re-Certificación Final J-04
**Objetivo:** Confirmar que las resoluciones de DOM, Timeouts y RBAC persisten de manera estable.
1. Ejecuta la suite Playwright sobre los flujos de Workdesk, Kanban y Kill-Switch.
2. Comando: `npx playwright test --project=chromium` (Asegúrate de apuntar a los specs de J-04).

### Paso 2: Certificación Estructural J-02 (BPMN & DMN)
**Objetivo:** Validar que la persistencia Zero-Mock V2 opera correctamente desde la UI hasta la DB vacía.
1. Diseña/Ejecuta pruebas E2E en Playwright para el diseñador BPMN (US-005): Guardado de borrador y despliegue.
2. Diseña/Ejecuta pruebas E2E en Playwright para el modelo DMN (US-007): Generación de reglas y validación Anti-Spoofing.
3. Asegura que ninguna aserción dependa de datos pre-cargados o mocks en memoria.

---

## 🚦 Criterios de Aceptación (DoD)

| # | Criterio | Evidencia Requerida |
|---|----------|---------------------|
| 1 | **Ejecución Exitosa J-04:** | Reporte de Playwright en verde para Workdesk/Kanban. |
| 2 | **Ejecución Exitosa J-02:** | Reporte de Playwright en verde para Modeler (BPMN/DMN). |
| 3 | **Trazabilidad Inyectada:** | Todo nuevo script E2E de J-02 debe llevar `// @Traceability: Certificación E2E J-02 (T-24)`. |
| 4 | **Reporte de Bug Cerrado:** | El bug reportado en `bug_report_qa_j04.md` debe declararse "SOLUCIONADO". |

---

## 📋 Instrucciones de Copiar y Pegar para el Agente 🕵️ QA E2E

```text
Asume el rol de 🕵️ Agente QA E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden:

1. cat .cursorrules
2. cat ibpms-platform/.agents/skills/qa_automation/SKILL.md
3. cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
4. cat ibpms-platform/.agentic-sync/T-24_QA_J02_Certification.md

TU MISIÓN:

1. El error de PowerShell (PSSecurityException) ha sido resuelto a nivel OS.
2. Re-ejecuta los tests E2E del flujo J-04 (Workdesk/Kanban).
3. Diseña o actualiza los tests E2E para el flujo J-02 (BPMN Modeler y DMN) asegurándote de que validen la persistencia real (Zero-Mock V2) y ejecútalos.
4. Genera un reporte de certificación consolidado o marca los fallos detectados.
5. Si todo está verde, finaliza con: `git add . && git commit -m "test(e2e): certificacion zero-mock de ecosistema j-02 y recertificacion j-04 [T-24]"`

REGLAS INQUEBRANTABLES:
- DEBES observar la Ley Global 4 (Inmutabilidad): PROHIBIDO modificar lógica de negocio del frontend o backend.
- DEBES inyectar `// @Traceability: Certificación E2E J-02 (T-24)` en todo código E2E nuevo.
- PROHIBIDO el uso de `git stash`.
```
