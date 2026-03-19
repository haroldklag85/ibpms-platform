# Handoff Arquitectónico Backend - Iteración 27 (US-005: CA-56 al CA-59)

> **Destinatario:** Agente Backend (Java/Spring Boot)
> **Mandato:** Cierre de Deuda Técnica (Fase Final de US-005).

## 🚨 Reglas de Oro (V1 Scope)
1. **Nada de V2:** Ninguna optimización analítica prematura ni refactores de persistencia no relacionados con estos CAs.
2. **Gatekeeper:** Cero commits directos. Usar `git stash` tras la ejecución.

---

## 🏗️ Tarea 1: Ley de Omisión Pura de Llaves Nulas (CA-57)
**Status:** REQUIRED (V1)
**Archivo Objetivo:** Clase que empaquete el Request de la Integración o Interceptor JSON.

### Implementación Esperada:
*   Si una variable mapeada como opcional se resuelve a `null` al momento del `ExecutionListener` o de orquestar el Request, el JSON resultante debe **purgar completamente la llave (Drop Key)**.
*   *Ejemplo Backend:* Configurar el `ObjectMapper` estándar (Jackson/Gson) con el directive `JsonInclude.Include.NON_NULL`. 
*   *Excepción (Opcional si es fácil acceder al Swagger):* Solo emitirá campos a nulo explícitamente si la definición del ServiceTask o el conector indica `nullable: true`. Si esto requiere un cruce de metadatos complejo en caliente, priorizar el `NON_NULL` universal para V1.

---

## 🏗️ Tarea 2: Amnesia Selectiva de Respuesta (Output Pruning - CA-59)
**Status:** REQUIRED (V1)
**Archivo Objetivo:** Delegate/Servicio que procese el HTTP Response del conector.

### Implementación Esperada:
*   Cuando la API externa responde, Camunda no debe tragarse el JSON completo (15MB) en `execution.setVariable()`.
*   El backend debe leer la configuración de `OutputMapping` y parsear el response usando un motor (ej., `JsonPath`). 
*   Solo extraer las llaves/rutas mapeadas en la Pantalla 6, e inyectarlas como variables individuales.
*   **Destruir** inmediatamente el texto base original (`Garbage Collection`). No persistirlo bajo ninguna circunstancia como una variable `HttpResponseData` gigante.

---

## 🏗️ Tarea 3: Delegación Transparente Binaria (CA-56)
**Status:** SIMPLIFIED REQUIRED (V1)
**Archivo Objetivo:** Servicio formador de peticiones (Payload Builder).

### Implementación Esperada:
*   Para V1, implementar una heurística simplificada:
*   Si el input mapping invoca a una variable Zod cuyo tipo en la BD documental marca un "Array de Docs SGDEA UUIDs", y el Swagger exige un tipo Byte/Base64, el Backend debe encargarse de transmutarlo asíncronamente (Consultar SGDEA y embeber).
*   Si esto implica demasiada dependencia de arquitectura del gestor de archivos aun no creado, agregar la heurística base y lanzar log un WARN `[TODO: Integrar SDK SGDEA para Mutación Multipart/Base64]`.

---

## 🏗️ Tarea 4: Resiliencia Asíncrona Parametrizable (CA-58)
**Status:** DELEGADO AL SISTEMA NATIVO
**Archivo Objetivo:** `PreFlightAnalyzerService` o Constructor XML.

### Implementación Esperada:
*   Configurar los conectores y `ServiceTasks` para usar `camunda:asyncBefore="true"`.
*   Asegurar que el XML entienda los intentos con la notación `camunda:failedJobRetryTimeCycle` (Ej: `R3/PT5M`).
*   Esto libera del bloqueo síncrono. Esta característica es nativa del motor; no hay que reinventar la rueda sino garantizar que el XML la exporte.

---

### Misión Final (Gatekeeper)
Al concluir los ajustes, ejecutar:
`git stash push -m "Iteracion 27: US-005 Backend Handoff CA-56 a CA-59 (Drop Null, Pruning, Retry)"` y notificar éxito al Comandante.
