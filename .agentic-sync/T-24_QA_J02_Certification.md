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
