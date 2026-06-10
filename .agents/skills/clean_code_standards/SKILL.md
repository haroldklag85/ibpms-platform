---
description: Normativas estrictas de Clean Code. Obligatorio para cualquier escritura o refactorización de código.
---

# Clean Code Standards

> ⚠️ **REGLA DE ORO:** El código que generas debe leerse como prosa. Está prohibido generar "código spaguetti", estructuras monolíticas o métodos de más de 30 líneas sin justificación y división en sub-métodos.

## NORMAS GENERALES PARA TODO EL CÓDIGO

1. **NO a la sobre-ingeniería (YAGNI):** No crees interfaces múltiples para implementaciones únicas a menos que se requiera inyección condicional evidente o esté dictado explícitamente en el ADR Hexagonal.
2. **KISS (Keep It Simple, Stupid):** Favorece lo directo por encima de lo 'inteligente'. Un código simple y legible vale más que código ingenioso pero hermético.
3. **Naming (Nombramiento Semántico):**
   - Variables descriptivas en inglés (si usamos inglés para codear) o en el dominio establecido.
   - NO a siglas oscuras (`TaskInfoProvider` en lugar de `TIP`).
   - El booleano debe ser una pregunta (`isActive`, `hasChildren`).
4. **Comentarios (Don't Repeat Yourself):**
   - El buen código no necesita comentarios explicativos obvios. Prohibido documentar el "qué" (`// Guarda el usuario`). Si necesitas comentarios, documenta el "POR QUÉ" ocurre este proceso (razones de negocio / deuda técnica).
   - Excepto JSDoc / JavaDoc en interfaces públicas de Servicios, eso es mantenible.

## ESTÁNDARES ESPECÍFICOS BACKEND (Java 17+ / Spring Boot)
1. **Evitar Retornos Nulos:** Retorna `Optional<T>` en repositorios o métodos que pueden no resolver la variable. NUNCA retornes `null` explícitamente si puedes tirar una excepción personalizada de dominio o retornar un Optional.
2. **Inmutabilidad:** Prefiere registros (`record`) para DTOs. Usa `final` en inyecciones de dependencias por constructor. Usa inyección por constructor (`@RequiredArgsConstructor`), NO `@Autowired` en los fields (variables).
3. **Manejo Excepciones:** No captures excepciones genéricas `catch (Exception e)`. Captura y tira clases concretas `CustomTaskNotFoundException`. No ocultes el stack trace, si lo re-levantás, pásalo adentro.
4. **Logging Correcto:** Prohibido el uso de `System.out.println`. Utiliza frameworks de Log formales (`@Slf4j`) con su nivel correcto de entropía (`log.trace`, `log.debug`, `log.info`, `log.error`).
5. **Documentación OpenAPI/Swagger (ADR-017):** Es obligatorio documentar todos los endpoints expuestos en RestControllers usando anotaciones `@Operation` y `@ApiResponses`. Se prohíbe exponer entidades JPA o Camunda directamente para evitar `StackOverflowError` en la autogeneración; se exige el uso exclusivo de DTOs planos. Véase [adr-017-api-documentation-standard.md](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/docs/architecture/adr-017-api-documentation-standard.md).

## ESTÁNDARES ESPECÍFICOS FRONTEND (Vue 3 / TypeScript)
1. **Tipado Estricto:** Evita a toda costa los `any`. Define `interfaces` y `types` para contratos de Axios y props de Vue.
2. **Composición:** Mantén setups menores a 100-150 líneas. Extrae lógica repetitiva y robusta a "Composables" (`useTask.ts`, `useWorkdesk.ts`). 
3. **Ref/Reactive:** Evitar mezclar indiscriminadamente. Usualmente `ref()` es el estándar moderno principal excepto para diccionarios u objetos puramente anidados aglomerados.
4. **Zero-Magic Strings:** Si tienes enums u opciones dadas, crea un archivo de constantes compartidas, nunca escribas strings quemados en validaciones.

## INSTRUCCIONES OPERATIVAS

Antes de grabar archivos con nueva lógica, revisa tu propio código con estos principios. Si notas algo en conflicto, refactoriza internamente antes de persistirlo en el workflow.
