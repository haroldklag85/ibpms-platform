# 🐞 Handoff de Remediación QA — Defecto en Vitest Reactivity (US-028)

> **Fecha:** 2026-04-07 | **Sprint:** 74-DEV | **Rama:** `sprint-3/informe_auditoriaSprint1y2`
> **Emisor:** Agente QA (TDD Gatekeeper)
> **Receptor:** Agente Frontend

---

## 🚨 Descripción del Defecto

Durante la ejecución de las suites de certificación para **US-028 CA-12 a CA-17**, los test unitarios de Frontend ubicados en `FormDesignerQACert.spec.ts` han devuelto un estado **🔴 RED STAGE (3 Failed, 1 Passed)**.

El fallo no radica necesariamente en la lógica de negocio del componente, sino en **problemas de asincronía y el ciclo de reactividad de Vue 3** en los entornos de Vitest. 

**Excepción capturada repetidamente:**
Los tests intentan simular cambios en el estado interno (ej: `certificationState`, `showFuzzerModal`, `currentSchemaVersion`) mediante reasignación a la instancia `wrapper.vm`. Sin embargo, debido a que el componente usa `<script setup>`, las refs internas están cerradas o no disparan el ciclo del DOM eficientemente solo haciendo `await nextTick()`. Las aserciones como `expect(wrapper.text()).toContain(...)` fallan porque el DOM no ha mutado.

---

## 🛠️ Instrucciones de Remediación Obligatoria (Frontend Agent)

Se exige que el Agente Frontend refactorice **exclusivamente el archivo de tests** `frontend/src/views/admin/Modeler/__tests__/FormDesignerQACert.spec.ts` para que pase limpiamente (Green Stage), aplicando técnicas avanzadas de testing en componentes de Vue 3.

### Criterios de Aceptación para la Remediación:

1. **Uso de `flushPromises` y Reactividad Correcta:** 
   Debes importar y utilizar `flushPromises` de `@vue/test-utils` exhaustivamente tras cada alteración de estado o interacción, seguido de `await nextTick()`, para garantizar que el DOM del Virtual DOM (y los eventos generados) se resuelvan antes de la aserción.

2. **No romper el componente productivo:**
   Está ESTRICTAMENTE PROHIBIDO modificar el código fuente de `FormDesigner.vue`. Este problema es del arnés de pruebas (Vitest), no del código productivo en sí. Tu única misión es hacer que el test pase.

3. **Mocks de Pinia y Vuelta a la Normalidad:**
   Actualmente se hace un `global: { plugins: [createPinia()] }`. Si es necesario simular los *stores* para desencadenar el renderizado profundo o los `v-if`, hazlo con un `vi.spyOn` o usando patrones que no asuman variables expuestas del componente.

4. **Revisión de los 3 Tests Críticos:**
   Asegúrate de que los tests que verifican:
   * **CA-12:** El badge de revocación QA / Aprobado.
   * **CA-13:** El indicador de versión en Sandbox `📋 Esquema V5`.
   * **CA-17:** Panel de Coherencia BPMN ↔ Zod y las comparaciones de matches.

---

## ✉️ Mensaje Estricto para tu Ejecución

> **Frontend Agent:** Ejecuta las remediaciones en `FormDesignerQACert.spec.ts` descritas en `.agentic-sync/handoff_qa_US028_frontend_defect_remediation.md`. 
> Reemplaza las inyecciones directas en `wrapper.vm` por configuraciones controladas en el montaje (`props`, *mocking de funciones de setup/pinia* o *data inicial*) y asegúrate de aplicar `await flushPromises()` y `await nextTick()`.
> **Prueba local:** `npm run test -- src/views/admin/Modeler/__tests__/FormDesignerQACert.spec.ts` DEBE resultar en `4 passed`.
> Al finalizar, realizar `git add`, `git commit -m "test(QA-Fix): Resuelve falla de sincronía Vitest flushPromises para CA-12/13/17"` y `git push`.
