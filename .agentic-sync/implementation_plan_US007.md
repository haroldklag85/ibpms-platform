# Plan de Implementación - Backend US-007 Bloque 1

## Objetivo
Resolver 12 GAPs arquitectónicos detectados en la auditoría forense para la historia de usuario US-007 (Generador Cognitivo DMN).

## Modo
**PLANNING**

## Fases de Desarrollo (Metodología TDD)

### Fase 1: Validadores de Dominio y Procesamiento de Prompt (GAPs 04, 06, 07, 17)
1. **GAP-17 (PromptNormalizer)**
   - *RED*: Crear `PromptNormalizerTest` comprobando que frases con puntuación y case distinto den el mismo resultado normalizado.
   - *GREEN*: Implementar `PromptNormalizer.java` (`toLowerCase`, `trim`, quitar signos extraños).
   - *REFACTOR*: Integrar en `AiDmnGeneratorUseCase`.
2. **GAP-04 (PromptPiiScrubber)**
   - *RED*: Crear `PromptPiiScrubberTest` con casos de PII obvios (Nombres, DNI).
   - *GREEN*: Implementar `PromptPiiScrubber.java` que aplique alias a los PII.
   - *REFACTOR*: Inyectarlo en la llamada pre-LLM del `AiDmnGeneratorUseCase`.
3. **GAP-06 (DmnVariableValidator)**
   - *RED*: Crear `DmnVariableValidatorTest` inyectando variables con dot-notation y date-math (FEEL functions).
   - *GREEN*: Implementar rechazos explícitos y sanitización a `lowercase()`.
   - *REFACTOR*: Integrar en `DmnGovernanceUseCase`.
4. **GAP-07 (DmnRuleValidator)**
   - *RED*: Crear `DmnRuleValidatorTest` probando excedentes de filas, más de 1 output, solapamiento de rangos e input de >4096 caracteres.
   - *GREEN*: Implementar validaciones y el trunk del token limit.
   - *REFACTOR*: Integrar en `DmnGovernanceUseCase`.

### Fase 2: Gobernanza XML y Simulador (GAPs 18, 19, 20, 26, 02)
1. **GAP-18 (XML Minificación Validation)**
   - *RED*: Escribir tests donde el parse de minificación falle y verifique el fallback al orginal.
   - *GREEN*: Agregarlo en `DmnGovernanceUseCase` al realizar Publish.
2. **GAP-19 (hitPolicy == FIRST)**
   - *RED*: Probar uploads con policy `COLLECT` o vacíos.
   - *GREEN*: Rechazar todo != FIRST, inyectar `FIRST` si está vacío.
3. **GAP-20 (Rate Limiter DMN Simulator)**
   - *RED*: Testear que la petición 21 devuelva `HTTP 429`.
   - *GREEN*: Implementar `DmnSimulatorRateLimiter` con control por Bucket o Redis.
4. **GAP-26 (Badge NLP_MODIFIED)**
   - *RED*: Enviar `PUT` manual a una tabla `NLP` y verificar que sube a V2 y graba `NLP_MODIFIED`.
   - *GREEN*: Implementarlo en `DmnGovernanceUseCase`.
5. **GAP-02 (GC TTL Check)**
   - *Verificación*: Inspeccionar `DmnDraftCleanupScheduler.java` para validar el cron. Modificar si no cumple la regla de expiración `< NOW()`.

### Fase 3: Integración Externa y Pre-Flight (GAPs 12, 14, 16)
1. **GAP-12 (Pre-Flight Catch-All vs BPMN Gateway)**
   - *RED*: Realizar test de integración simulando `BusinessRuleTask` atado a una DMN con Catch-All, cuyo siguiente paso sea un Tarea Simple y no un Gateway.
   - *GREEN*: Enganchar la regla en el analizador de BPMN (US-005) llamando a la API DMN.
2. **GAP-14 (FormSchemaChangedRabbitListener)**
   - *RED*: Preparar el testcontainer de Redis y RabbitMQ, publicar un schema changed y verificar vaciado parcial de Caché (por hash).
   - *GREEN*: Crear `FormSchemaChangedListener` y `FormSchemaChangedRabbitListener`.
3. **GAP-16 (OpenAPI Annotations)**
   - *Implementación Dirigida*: Modificar `AiDmnGeneratorController`, `DmnGovernanceController`, `DmnSimulatorController` para incluir las etiquetas OpenAPI exigidas.

### Control de Calidad Transversal
- Ejecutar la suite completa `mvn clean test`.
- Validar la compilación SRE Zero-Trust `mvn clean compile`.
- Hacer commit atómico en `sprint-6`.
