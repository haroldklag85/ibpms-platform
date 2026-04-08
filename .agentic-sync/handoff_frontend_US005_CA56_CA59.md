# Handoff Arquitectónico Frontend - Iteración 27 (US-005: CA-56 al CA-59)

> **Destinatario:** Agente Frontend (Vue 3 / Composition API)
> **Mandato:** Cierre de Deuda Técnica (Fase Final de US-005).

## 🚨 Reglas de Oro (V1 Scope)
1. **Nada de V2:** Mantener interfaces magras. Ninguna validación estadística ni MLOps.
2. **Gatekeeper:** Consolidar cambios usando `git commit` y `git push` tras la ejecución.

---

## 🏗️ Tarea 1: Ocultamiento de Complejidad Multipart (CA-56)
**Status:** REQUIRED (V1)
**Archivo Objetivo:** `BpmnDesigner.vue` (Panel de Mapeo).

### Implementación Esperada:
*   Si el diseñador asocia un campo de tipo `<InputFile>` hacia el payload de salida de un ServiceTask, el frontend **no** debe preguntarle si desea enviarlo como Base64, Multipart o Link.
*   Simplemente renderice el mapeo visual estándar. La transmutación binaria es responsabilidad exclusiva del Backend (Worker).

---

## 🏗️ Tarea 2: Resiliencia Asíncrona UI (CA-58)
**Status:** REQUIRED (V1)
**Archivo Objetivo:** `BpmnDesigner.vue` (Property Panel de `ServiceTask`).

### Implementación Esperada:
*   Agregar una nueva sección desplegable (Accordion o Sección) titulada `[ ⚙️ Estrategia de Fallo (Retries) ]` cuando se seleccione un `ServiceTask`.
*   Contendrá un switch: "Activar Reintentos (Asincrónicos)".
*   Si está activo, mapeará silenciosamente a `camunda:asyncBefore="true"`.
*   Mostrará un input de texto para `camunda:failedJobRetryTimeCycle` pre-llenado con un valor estándar (Ej: `R3/PT5M` - 3 intentos espaciados por 5 mins). No es necesario construir validadores pesados de cron-expressions para V1, solo inyectar la cadena de texto válida por defecto y permitir su edición bajo responsabilidad del usuario técnico.

---

## 🏗️ Tarea 3: Output Pruning Mapping (CA-59)
**Status:** REQUIRED (V1)
**Archivo Objetivo:** `BpmnDesigner.vue` (Tab de `OUTPUT MAPPING`).

### Implementación Esperada:
*   Asegurar que el `<DataMapperGrid>` existente tenga una interfaz explícita separada (o pestaña) para mapear el *Response* del Gateway (Output Mapping) hacia las variables Zod.
*   Instruir vía UI con un simple helper que el usuario "Solo debe referenciar los datos que necesite del megapayload externo, el motor destruirá los no mapeados automáticamente (Amnesia Selectiva)".

---

### Misión Final (Gatekeeper)
Al concluir los ajustes visuales, ejecutar:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD` y notificar éxito al Comandante.
