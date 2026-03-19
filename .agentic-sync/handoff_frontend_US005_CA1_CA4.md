# Contrato de Arquitectura Frontend (Iteración 15 - US-005: CA-1 al CA-4)

**Rol:** Desarrollador Frontend Vue 3 / JavaScript (Integrador de bpmn.io).
**Objetivo:** Levantar el lienzo nativo del Diseñador BPMN, capturar su XML subyacente y enviar el Payload correctamente atado al Backend, manejando sus rechazos (422).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 15 debemos reconstruir la integración de `bpmn-js` en el componente `BpmnDesigner.vue` (Actualmente con deuda alta por rollback) apuntando a los Criterios base:

*   **CA-1 (Instanciación y Despliegue):** Inicializa el Modeler de `bpmn-js` en un contenedor nativo (ref). Crea un botón global `[🚀 VALIDAR Y DESPLEGAR]`. Al hundirlo, debes invocar la API asíncrona del modeler `modeler.saveXML({ format: true })`. Envía el String XML resultante empaquetado como `multipart/form-data` (archivo Blob) hacia `POST /api/v1/design/processes/deploy`.
*   **Sincronización de Nodos (Prevención de Desconexión):** El Arquitecto Líder advirtió el riesgo: "Desconexión entre propiedades inyectadas y el XML final". Asegúrate de que, si implementas un panel de propiedades, uses el `modeling.updateProperties(element, { ... })` oficial de bpmn-js para que los datos muten el XML y no queden sueltos en variables Vue huérfanas.
*   **CA-2, CA-3 y CA-4 (Feedback Visual Pre-Flight):** Si el servidor Backend rechaza el XML con HTTP 422 (Unprocessable Entity), atrapar el JSON de error y renderizar una Cónsola Inferior o Panel lateral con la lista de "Errores Semánticos y Advertencias" detectados (Ej. "Falta Start Event FormKey", "Service sin DelegateExpression"). Los marcadores visuales iterativos dependerán del JSON listado. No cierres / no bloquees el Canvas; permítele al usuario corregir el error y volver a oprimir el botón.

## 📐 Reglas de Desarrollo:
1. `bpmn-js` suele requerir que emules un XML inicial vacío (Boilerplate de Start Event) cuando se carga. Créalo dinámicamente o ten una variable string constante.
2. Maneja limpiamente el montaje y desmontaje (elimina la instancia del modeler en `onUnmounted`) para prevenir fugas severas de DOM.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal. 
Levanta el Modeler, constata que puedes dibujar nodos y atrapar el XML. Luego congela todo:
`git stash save "temp-frontend-US005-ca1-ca4"`

Escribe textualmente la comprobación del STASH exitoso en este chat al terminar.
