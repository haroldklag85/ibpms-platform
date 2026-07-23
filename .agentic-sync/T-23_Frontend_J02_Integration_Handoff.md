# 🧠→🎨 Handoff: Arquitecto Líder → Frontend
# T-23: Integración Frontend J-02 (Low-Code Ecosystem)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 Frontend
**Fecha:** 2026-05-13
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🟡 Media (Ensamblaje Funcional)
**Dependencia:** Requiere que el Agente Backend complete T-22 (o simular/esperar los endpoints reales).

---

## 1. Resumen Ejecutivo (El "Por Qué")
Iniciamos la Iteración 7.2. El objetivo es ensamblar el Journey J-02: El ecosistema Low-Code (IDE de Formularios US-003, BPMN Designer US-005, DMN Intelligence US-007). 
Tu misión en Frontend es **conectar la interfaz (wiring)**. Partiendo de una base de datos vacía, el usuario debe poder navegar entre estos tres diseñadores, crear artefactos (formularios, diagramas, reglas) desde cero, y guardarlos consumiendo las APIs reales del Backend. Debes erradicar cualquier mock estático que haya quedado en los *stores* de Vue (Pinia) para estos módulos.

---

## 2. Definición de Hecho (DoD - AP-02)
- ✅ El enrutamiento de Vue (`vue-router`) permite la navegación fluida entre los módulos Form IDE, BPMN Designer y DMN Engine.
- ✅ Los *stores* (Pinia) correspondientes (ej. `useFormStore`, `useBpmnStore`) consumen AXIOS/Fetch apuntando a las APIs reales. **Cero Mocks (Zero-Mock V2)**.
- ✅ La UI maneja correctamente los estados de carga asíncrona (`try/catch/finally` sin *dangling promises*) al guardar o desplegar artefactos, mostrando feedback visual al usuario (Notificaciones).
- ✅ Ley Global 3 (Trazabilidad): `// @Traceability: Integración Frontend J-02 (T-23)`.
- ✅ El código compila nativamente (`npm run build`).

---

## 3. Contexto Mandatorio (ADRs y Leyes Globales)
*   **Zero-Mock V2 (ADR-010):** Se revoca la autorización pasada para usar mocks. El ecosistema J-02 debe operar 100% contra el Backend. No asumas datos pre-cargados (la BD estará vacía).
*   **Ley Global 3:** Trazabilidad inyectada.

---

## 4. Dependencias Técnicas Previas
- Endpoints de Backend listos (T-22).

---

## 5. Plan de Acción (Action Plan)
1.  **Purgar Mocks:** Revisa los componentes y stores asociados a US-003, US-005 y US-007. Elimina objetos pre-cargados o llamadas a `setTimeout` que simulen APIs.
2.  **Wiring de APIs:** Configura las llamadas HTTP (`POST`, `PUT`, `GET`) hacia los endpoints canónicos (`/api/v1/...`).
3.  **Estabilización UI:** Asegura que los botones de "Guardar" o "Desplegar" disparen la acción, bloqueen la interfaz temporalmente (loading) y se liberen en el `finally`.
4.  **Validación Manual Local:** Verifica en tu entorno de desarrollo (`npm run dev`) que la creación cruzada de un flujo básico no emita errores de consola en el VDOM.

---

## 6. Estrategia de Testing (Validación)
- Renderizado de componentes sin errores. `npm run type-check` sin advertencias críticas de TypeScript en los stores modificados.

---

## 7. Agnostic Handoff Payload (Copia y Pega esto al Agente Frontend)
```text
Asume el rol de 🎨 Agente Frontend.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat ibpms-platform/.agents/skills/frontend_vue3_tailwind/SKILL.md
3. cat ibpms-platform/.agentic-sync/T-23_Frontend_J02_Integration_Handoff.md

TU MISIÓN:

1. Inicia el ensamblaje del ecosistema Low-Code (J-02). Tu tarea es conectar el IDE de Formularios (US-003), el BPMN Designer (US-005) y el DMN Intelligence (US-007) con las APIs reales del Backend.
2. Política Zero-Mock V2 Estricta: Elimina cualquier dato estático, mock o `setTimeout` en los *stores* (Pinia) o componentes asociados a estas US. El usuario debe poder crear los artefactos desde una base de datos vacía y guardarlos nativamente.
3. Asegura el manejo de estado asíncrono estricto (`try/catch/finally`) para que la UI no quede congelada al interactuar con el Backend.
4. Inyecta trazabilidad: `// @Traceability: Integración Frontend J-02 (T-23)`.
5. Valida que Vue compila correctamente (`npm run type-check` / `npm run build`) y realiza el commit: `feat(ui): wiring de componentes lowcode contra backend real j-02 [T-23]`.
```
