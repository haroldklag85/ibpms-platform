# Contrato de Arquitectura Backend (Iteración 19 | US-005: CA-16 a CA-20)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Desarrollar los endpoints de concurrencia pesimista, el mock de IA, el simulador lógico (Mock) y endurecer el Pre-Flight.

## 📋 Contexto y Criterios de Implementación:

### Tarea 1: Endurecimiento Pre-Flight Avanzado (CA-18)
Abre la clase `PreFlightAnalyzerService.java` e inyecta 3 validaciones semánticas obligatorias:
1. `TimerEvent`: Falla 422 si la etiqueta `timeDuration` o `timeCycle` no existe o está en blanco.
2. `MessageEvent`: Falla 422 si falta el `messageRef` en el nodo.
3. `CallActivity`: Falla 422 si falta la propiedad `calledElement` (Proceso hijo).

### Tarea 2: Controlador de Herramientas de Modelado (CA-16, CA-17, CA-19, CA-20)
Construye o extiende el `BpmnDesignController.java` para soportar las siguientes rutas (In-Memory/Mock implementations temporales o reales donde aplique):

*   **Bloqueo Pesimista (CA-16):** `POST /api/v1/design/processes/{key}/lock`. Si ya existe en una caché en memoria (Ej: Singleton Map de Locks), devuelve `423 Locked`. Si no, registra el Lock para el usuario en sesión (mock User) y retorna OK. `DELETE .../lock` lo libera.
*   **Copiloto IA (CA-17):** `POST /api/v1/design/processes/ai-copilot`. Recibe el XML Multipart. Retorna una sugerencia estática ISO 9001 (E.g. "Recomendación: Agregar User Task para revisión manual de calidad.") para simular al LLM.
*   **Autosave Borrador (CA-19):** `POST /api/v1/design/processes/{key}/draft`. Acepta XML temporal y retorna HTTP 200.
*   **Sandbox (CA-20):** `POST /api/v1/design/processes/sandbox-simulate`. Retorna un DTO con una lista serial de Node IDs simulados (Extrae 3 o 4 Ids de evento de inicio, tarea y fin parseando el XML adjunto o devolviendo una demo estancada) para que el Frontend los encienda en verde.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Desarrolla los puertos, endurece el servicio y encapsula en stash:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente la confirmación del guardado.
