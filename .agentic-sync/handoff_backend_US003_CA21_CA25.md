# Contrato de Arquitectura Backend (US-003 Iteración 5: CA-21 al CA-25)

**Rol:** Desarrollador Backend Java/Spring Boot (Experto en Hexagonal Architecture).
**Objetivo:** Desarrollar los endpoints de almacenamiento temporal transaccional requeridos para dar soporte a la Gobernanza y Resiliencia del Form Designer.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración (Fase 5), el Frontend conectará sus herramientas visuales (Auto-Guardado y Adjuntos Dropzone) con endpoints reales.

Debes abordar:
*   **CA-21 (SGDEA Upload Endpoint):** El frontend enviará archivos multipart. Crea el endpoint `POST /api/v1/forms/upload`. Por ahora, simula el guardado en un Storage Service (ej. System.getProperty "java.io.tmpdir") y retorna el UUID y PATH del archivo (DTO `FileUploadResponse`). No acoples la lógica de MS Graph directamente en el Controller, abstrae a una interfaz `DocumentStoragePort`.
*   **CA-24 (Auto-Guardado Workdesk):** El frontend disparará periódicamente el autoguardado de la estructura JSON del diseñador o del payload en vivo. Crea el endpoint `POST /api/v1/forms/draft` y `GET /api/v1/forms/draft/{formId}`. Guarda este borrador en cachè, Memoria (ConcurrentHashMap), Redis o una tabla JPA `form_drafts`.

## 📐 Reglas de Desarrollo:
1. **Hexagonal Architecture:** Debes modificar el `FormDesignController` (o crear `FormStorageController`), implementar un caso de uso (`SaveFormDraftUseCase`) y la salida hacia el repositorio.
2. **REST Estándar:** Retorna Data DTOs limpios y status `201 Created` o `200 OK`. 
3. Escribe pruebas unitarias esenciales si dispones de herramientas para ello.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal. 
Una vez que hayas codificado los controladores, servicios y DTOs, asegúrate de que el código compile y guarda inmediatamente todo tu trabajo ejecutando en la consola de tu editor:
`git stash save "temp-backend-US003-ca21-ca25"`

Notifica al Arquitecto Humano una vez terminado, pero no empujes nada.
