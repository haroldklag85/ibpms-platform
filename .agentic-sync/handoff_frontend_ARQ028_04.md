# Handoff Frontend — ARQ-028-04 | Segregación de Cohesión Mixta

> **Rama:** `sprint-6`
> **Estado:** 🟢 SIN ACCIONES REQUERIDAS

La refactorización **ARQ-028-04** corresponde a una normalización de base de datos y refactorización de entidades JPA en el Backend (separando `ibpms_form_definitions` de `ibpms_form_certifications`). 

El Agente Backend ha recibido instrucciones estrictas de **mantener intactos los contratos API (DTOs)**. Por lo tanto, los componentes Vue que dependen del estado de certificación (como `FormDesignerQACert.vue` o similares) no sufrirán roturas ni cambios en sus propiedades.

**Instrucción:** Archivar este ticket y continuar con el roadmap de desarrollo o remediación.
