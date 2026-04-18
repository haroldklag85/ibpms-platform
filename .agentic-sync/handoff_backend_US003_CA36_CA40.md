# Contrato de Arquitectura Backend (US-003 Iteración 8: CA-36 al CA-40)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Implementar validaciones perimetrales de seguridad para adjuntos y proveer soporte para la visualización de auditoría histórica.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración (Fase 8), la madurez del FormDesigner debe aumentar en control de entrada y auditoría.

Debes abordar:
*   **CA-37 (Soporte Visor Histórico Inmutable):** Asegúrate de que los endpoints de consulta de ejecución de formularios (`GET /api/v1/workbox/tasks/{taskId}/form-context` o similares) soporten la inyección de metadatos que certifiquen el "Modo Solo Lectura" para tareas que ya fueron completadas y están en el historial de Camunda (`HistoricTaskInstance`).
*   **CA-39 (Condicionamiento de Archivos Adjuntos - Seguridad):** Modifica el endpoint de subida de archivos (creado en la It. 5: `POST /api/v1/forms/upload`) para procesar cabeceras o parámetros de validación estricta de **Peso Máximo** y **Tipos MIME Permitidos**. Si el archivo vulnera estos parámetros, la API debe rechazar con `415 Unsupported Media Type` o `413 Payload Too Large`.

## 📐 Reglas de Desarrollo:
1. Mantén la consistencia con la arquitectura Hexagonal previamente construida en `FormStorageController`.
2. Las validaciones de seguridad deben ocurrir en la Capa Web (Controller) o Application (UseCase) antes de tocar el Adapter de Almacenamiento.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto finalices el código backend, empaqueta las modificaciones:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualemente al Arquitecto Líder apenas termine el guardado.
