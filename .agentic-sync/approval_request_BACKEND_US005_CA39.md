# Solicitud de Revisión: BACKEND US-005 CA-39 / CA-40

**De:** ⚙️ Agente Backend Java
**Para:** 🧠 Arquitecto Líder

He analizado el Handoff y el Diagnóstico Forense para la estabilización del catálogo de formularios activos. 
Mi plan de implementación detallado se encuentra listo, pero requiero aprobación técnica antes de codificar (Zero-Trust).

### Resumen del Plan:
1. **Controller**: Modificar `FormCatalogController.java` para capturar `EntityNotFoundException` al consultar un `processKey` inexistente, permitiendo que la lista no se rompa y retorne todos los forms (fallback).
2. **Estados**: Actualizar el filtro para que admita `"ACTIVE"` y `"DRAFT"`.
3. **DTO**: Implementar el nuevo `FormCatalogDTO.java` tal cual se define en el Handoff.
4. **Tests**: Programar `FormDesignControllerTest.java` como test unitario puro (Mockito, sin Spring Context) respetando los Clean Code Standards.

### ⚠️ PREGUNTAS CRÍTICAS PARA EL ARQUITECTO:
1. **Contrato JSON Frontend**: El Handoff me ordena crear `FormCatalogDTO` con la firma `(UUID id, String technicalName)`. Sin embargo, el diagnóstico detectó que el Frontend mapea el ID del select usando `f.id` (que anteriormente correspondía al `technicalName`). Si retorno el UUID en `id`, el Frontend fallará. ¿Me autorizas a mapear `technicalName` como `"id"` en el JSON o prefieres que deje el DTO estrictamente como dicta el Handoff?
2. **Propiedad Stages**: El Frontend utilizaba la propiedad `stages`. El nuevo DTO no la incluye. ¿Es seguro omitirla o debo añadirla al Record DTO?

Espero autorización para proceder con la fase de ejecución.
