# Contrato de Arquitectura Frontend (Iteración 16 | US-005: CA-5 y CA-6)

**Rol:** Desarrollador Frontend Vue 3 / JavaScript (Integrador de bpmn.io).
**Objetivo:** Manipular dinámicamente el diccionario de propiedades (*Root Elements*) para inyectar reglas de metadatos estandarizadas.

## 📋 Contexto y Criterios de Aceptación:
Se excluyeron las lógicas de migraciones V2. Nos centramos en la inyección de reglas operativas para la V1 en el `BpmnDesigner.vue`.

*   **CA-5 (Nomenclatura Obligatoria de Instancia):**
    *   **Funcionalidad:** Implementa en la Interfaz Lateral o en un modal de "Propiedades del Proceso" un Input de texto obligatorio llamado `Regla de Nomenclatura (Ej: OC-{Solicitante})`.
    *   **Binding:** Cuando el usuario asigne un valor en este campo, debes inyectar esto en el nodo raíz del XML (`bpmn:Process`) usando las propiedades oficiales de Camunda (`camunda:properties`), especificando un `name="ReglaNomenclatura"`. Esto es fundamental; si no lo envías, el backend arrojará un 422 Pre-Flight Reject.
*   **CA-6 (Roles Autogenerados - Feedback Visual):**
    *   El usuario dibujará Carriles (Lanes) libremente con bpmn-js (Nativo).
    *   Cuando el usuario presione Desplegar y el Backend retorne el 201 Created, el JSON de respuesta traerá un array `generatedRoles`. Extrae ese array y lanza un Modal de Éxito o notificación extensa (Ej: SweetAlert / Toasts) informando textualmente:
        *"Proceso desplegado. Se han auto-generado los siguientes perfiles de seguridad: [Lista de Roles]"*.

## 📐 Reglas de Desarrollo:
1. Para modificar la base del proceso en bpmn-js, utiliza el objeto `canvas.getRootElement().businessObject` o la API `modeling.updateProperties(...)` orientada al proceso raíz.
2. Si el Backend devuelve Error 422 por "Falta de Nomenclatura" (CA-5), asegúrate de que el Toast/Consola ya construida en la Iteración 15 lo intercepte correctamente.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal. 
Dibuja el Input de nomenclatura, asegura la mutación del XML y congela todo:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Escribe textualmente la comprobación del STASH exitoso en este chat al terminar.
