# ADR 017: Estándar de Documentación de APIs REST con SpringDoc OpenAPI

**Fecha:** 2026-06-10  
**Estado:** Aceptado  
**Contexto:** Plataforma Core iBPMS (Gobernanza de APIs e Integración de Swarm)

---

## 1. Contexto y Problema

En la plataforma Core iBPMS, las APIs expuestas a través de los controladores REST en la capa de infraestructura representan el contrato de integración primordial para el Frontend y clientes externos. Durante la auditoría inicial de la **US-005**, se detectaron los siguientes problemas:
1.  **Falta de Cobertura Homogénea:** Múltiples controladores expuestos carecen de anotaciones descriptivas, impidiendo una correcta generación del Swagger UI de desarrollo.
2.  **Riesgo de Fallos por Dependencias Circulares:** Al exponer accidentalmente objetos nativos de persistencia (JPA Entities) o del motor de workflows (Camunda Engine) en los endpoints, SpringDoc OpenAPI intenta serializarlos de manera recursiva, causando errores críticos como `StackOverflowError` o problemas de mapeo JSON.
3.  **Falta de Estándar para Campos Dinámicos:** Los campos de iForms (JSONB) no se documentaban homogéneamente, lo que impedía previsualizar payloads consistentes desde el navegador.

---

## 2. Decisión

Establecemos las siguientes reglas y directrices inquebrantables para la documentación y diseño de endpoints REST en la plataforma:

### A. Documentación Viva Obligatoria
*   **Anotación de Controladores:** Todo RestController expuesto en `infrastructure/web` debe anotarse con `@Tag` de OpenAPI para su categorización en Swagger UI.
*   **Anotación de Métodos:** Cada método público expuesto debe contar con `@Operation(summary, description)` que detalle de manera explícita el comportamiento del negocio.
*   **Documentación de Códigos HTTP:** Se deben registrar de manera obligatoria todos los códigos de respuesta factibles (`200`, `201`, `400`, `403`, `409`, `422`, `500`) usando `@ApiResponses`.

### B. Aislamiento Total en Firma de Endpoints (Prevención de Exceptions)
*   **Prohibición Absoluta:** Queda terminantemente prohibido usar entidades de persistencia (anotadas con `@Entity`) o clases nativas del framework/motor de workflows (como clases de `org.camunda.bpm.engine.*` o de Spring Security) como tipo de retorno o parámetro de entrada directo en los métodos del controlador.
*   **Uso de DTOs Planos:** Todos los endpoints deben consumir y retornar exclusivamente Data Transfer Objects (**DTOs**) planos. Los mapeos se resolverán en la capa de adaptadores mediante MapStruct.
*   **Exclusión de Objetos Técnicos:** Elementos como `Principal`, `Authentication` o `HttpServletRequest` deben ser anotados con `@Parameter(hidden = true)` para ser excluidos del parseador de OpenAPI.

### C. Especificación de Campos Dinámicos (JSONB)
*   Los esquemas dinámicos (`iForms`) deben viajar y persistirse como `String` o `Map<String, Object>`, y anotarse mediante `@Schema` describiendo su formato Zod/JSON Schema y aportando un ejemplo legible para el consumidor.

---

## 3. Consecuencias

*   **Positivas:**
    *   **Estabilidad del Spec:** Swagger UI y `/v3/api-docs` se autogeneran de forma robusta sin riesgo de bloqueos o excepciones de pila.
    *   **Contratos Claros:** El equipo Frontend y los agentes de IA de UI pueden generar código y realizar pruebas de integración (Playwright) de manera predecible.
*   **Negativas/Riesgos Aceptados:**
    *   Requiere la escritura obligatoria de DTOs dedicados para cada endpoint de infraestructura, incrementando la verbosidad inicial.
