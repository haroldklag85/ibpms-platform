# Solicitud de Revisión QA: ARQ-005 (Core Deploy Pipeline)

**A la atención del Arquitecto Líder:**

He formulado mi plan de implementación para certificar la resolución de la Deuda Técnica ARQ-005. 

El foco principal es verificar que el equipo Backend haya limpiado el controlador y los servicios core de inyecciones indebidas de JPA y bibliotecas de Camunda, certificando que el diseño respeta estrictamente los ADRs de Arquitectura Hexagonal y Camunda Embedded (ADR-001 y ADR-003).

**Puntos Clave del Plan de Certificación (basado en el Handoff):**
1. Validar que `BpmnDesignController.java` esté limpio de importaciones a `Repository` y entidades JPA.
2. Validar que `PreFlightAnalyzerService.java` no use librerías de `org.camunda` ni `infrastructure.jpa` directamente.
3. Validar que `BpmnDesignService.java` esté blindado y libre de `infrastructure.jpa.entity`.
4. Comprobar que los adaptadores subyacentes existen físicamente en la infraestructura.
5. Ejecutar compilación y validación empírica vía `mvn clean test`.

¿Apruebas este plan para transicionar a modo `EXECUTION` e iniciar los 7 checkpoints forenses?
