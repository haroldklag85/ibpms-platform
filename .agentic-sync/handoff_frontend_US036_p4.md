# Handoff Frontend - Iteración 39 (US-036: CA-16 a CA-18)

## Propósito
Exponer la visualización inmutable (Solo Lectura) de la pestaña de Auditoría CISO y proveer el puente de red para la descarga binaria (BLOB) del informe matricial general.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-16 (Descarga Sábana CSV CISO):** Añadir botón `[ Descargar Matriz CSV ]` en el Header de Pantalla 14. Este botón accionará Axios con `responseType: 'blob'`, forzando al navegador web a desplegar la ventana de guardar archivo (Generador de Enlace en Memoria `window.URL.createObjectURL`).
* **CA-17 (Traza Indeleble):** Añadir la Tab Final a Pantalla 14: `Tab de Seguridad y Auditoría`. Será una grilla estricta de **Solo Lectura**. Consumirá el Endpoint de Audit Roles y mostrará el `Timestamp` (Alineado UTC), `Admin_ID`, y un botón Modal `[ Ver JSON Delta ]` que despliegue el JSON de los permisos mutados.
* **CA-18 (SoD / Juez y Parte):** **[NO-OP]** No programar validaciones complejas de formulario si un analista intenta completar tareas cruzadas, el modelo V1 no censura este comportamiento.

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Conectar Axios BLOB para descargar el CSV de Fiscalización.
2. Pintar la Grilla Analítica de Audit Trails con el visor JSON en Modal.
