# Contrato de Arquitectura Backend (Iteración 26 | US-005: CA-52, CA-53, CA-54)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Asegurar la inmutabilidad de los conectores en vuelo (Zero-Breakage), certificar la privacidad penal de datos PII y blindar las reglas OneOf en Pre-Flight.

## 📋 Contexto y Criterios de Implementación:

### Tarea 1: Inmutabilidad de Swagger / Zero-Breakage (CA-52)
*   En el `IntegrationHubController`, cuando se intente actualizar un "Connector Swagger" existente (si el payload borra o muta reglas), forzar la retención estricta: NO SE SOBREESCRIBE.
*   Se clona el conector a una versión nueva (Ej: `API_v2`). Los procesos BPMN `V1` que ya estaban usando `API_v1` en producción no deben colapsar, siguiendo consumiendo el descriptor original congelado.

### Tarea 2: Redacción PII en Auditoría History (Seguridad Shift-Left) (CA-54)
*   Crea o ajusta un `CustomHistoryEventHandler` (o mecanismo de auditoría) para Camunda 7.
*   Intercepta el guardado de variables históricas (`HistoricVariableUpdateEventEntity`). Si la variable viaja marcada con metadatos PII o proviene de un descriptor Sensible, la base de datos de auditoría **NO DEBE PERSISTIR SU VALOR CRUDA**. Remplázalo con el string `"[REDACTED_PII]"`.

### Tarea 3: Analyzer Pre-Flight OneOf (CA-53)
*   Dentro de `PreFlightAnalyzerService`, si una ServiceTask mapea un conector que exige cláusula lógica `oneOf` / `anyOf`, valida el Payload Mapping Inyectado:
    *   Si AMBOS están vacíos: `Error (Bloquea Despliegue)`.
    *   Si falta al menos uno que lo satisfaga: `Error`. 
    *   Si se cumple la regla mínima: Pasa `OK`.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Desplegar lógicas, y archivar en stash locutado al comandante:
`git stash save "temp-backend-US005-ca51-ca55"`

Informa textualmente la confirmación del guardado.
