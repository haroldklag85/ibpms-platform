# 🧠→🎨 Handoff: 🧠 ARQUITECTO LÍDER / ⚙️ PRODUCT OWNER → 🎨 FRONTEND - VUE
# T-US005-FE01: Corrección de Formato de Carga en Validación Pre-Flight (Multipart vs JSON)

**Emitido por:** [🧠 ARQUITECTO LÍDER / ⚙️ PRODUCT OWNER]
**Destinatario:** [🎨 FRONTEND - VUE]
**Fecha:** 2026-06-06T00:48:00-05:00
**Sprint:** 6 — Iteración 6.2
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor (Vite/Vue Build Audit)
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Requerimientos de la US-005 (Criterio CA-65)
cat docs/requirements/epics/epic_B_formularios_bpmn.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación o comentario `// @Traceability: US-005, CA-65`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Durante la validación interactiva en el Modeler (Pantalla 6) al presionar "Validar y simular", el modal del embudo de validación retorna un error `Request failed with status code 400` en la pestaña **Pre-Flight Analyzer**.

La investigación reveló una discrepancia en el formato del payload transmitido por la red:
*   El **Backend** (`BpmnDesignController.java` línea 184) exige que el endpoint `/validate` reciba el XML de BPMN como un archivo multipart (`multipart/form-data`) bajo el parámetro `file`.
*   El **Frontend** (`useIntegrationStore.ts` línea 33) transmite el XML empaquetado en una trama JSON estándar (`application/json`) con la estructura `{ xml: "..." }`.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Discrepancia del Contrato de Red | `useIntegrationStore.ts:33-35` | Envía JSON en lugar de Multipart/Form-Data esperado por el controlador del backend. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Encapsulación de FormData en useIntegrationStore.ts

**Archivo:** [useIntegrationStore.ts](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/stores/useIntegrationStore.ts)

Modificar la acción `validateProcess` en el store Pinia para interceptar el payload JSON `{ xml }`, encapsular el XML de BPMN dentro de un objeto `FormData` con formato `Blob` simulando un archivo con nombre `process.bpmn`, y despachar la petición POST configurando el encabezado `Content-Type: multipart/form-data`.

```typescript
    // @Traceability: US-005, CA-65
    validateProcess(payload: { xml: string }) {
      const formData = new FormData();
      const blob = new Blob([payload.xml], { type: 'application/xml' });
      formData.append('file', blob, 'process.bpmn');
      
      return this.post(`/design/processes/validate`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
    },
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Transmisión en Formato Multipart | Al ejecutar la validación desde el frontend, el browser debe enviar la cabecera `Content-Type: multipart/form-data; boundary=...` en el request de red. |
| 2 | Parámetro `file` Correcto | El payload de la petición HTTP hacia `/api/v1/design/processes/validate` debe contener una sección `Content-Disposition: form-data; name="file"; filename="process.bpmn"`. |
| 3 | Pre-Flight Exitoso (HTTP 200) | Al presionar "Validar y simular" sobre un proceso BPMN en la UI, la pestaña del Pre-Flight Analyzer debe retornar exitosamente sin alertas rojas de status 400. |
| 4 | Compilación/Build Limpio | Ejecutar `npm run build` en el frontend y verificar que no haya errores de compilación TypeScript. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar [useIntegrationStore.ts](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/stores/useIntegrationStore.ts).
2. Correr la compilación de frontend para validar tipados y build:
   - Seguir las instrucciones en `.agents/skills/frontend_build_audit/SKILL.md`.
3. Confirmar que la compilación sea exitosa.
4. Git Commit y Push en la rama actual de trabajo:
   `git add . && git commit -m "fix(frontend): align validateProcess payload format with backend multipart" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🎨 FRONTEND - VUE.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agentic-sync/handoff_US005_CA65_validate_multipart.md

TU MISIÓN:
1. Modificar el método `validateProcess` en `useIntegrationStore.ts` según las instrucciones quirúrgicas de handoff_US005_CA65_validate_multipart.md para enviar el XML como un multipart `file` en lugar de JSON.
2. Ejecutar el build de frontend `npm run build` para certificar la compilación sin advertencias o roturas de tipados.
3. Consolidar los cambios con un commit convencional a la rama de sprint actual y realizar el push correspondiente.

REGLAS INQUEBRANTABLES:
- Prohibido el uso de git stash bajo la Ley Global 2.
- Mantener la trazabilidad agregando el comentario // @Traceability: US-005, CA-65.
- No modificar el componente BpmnDesigner.vue a menos que sea estrictamente necesario para resolver la integración de los datos.
```
