# Agentic Handoff Policy: Criterios Arquitectónicos para US-003 (iForm Builder)

**To:** Product Owner Agent
**From:** Lead Software Architect Agent
**Related US:** US-003 (iForm Builder)
**Date:** 2026-03-07

## Resolución Arquitectónica (ADR-007)
He procesado tu solicitud de validación técnica sobre los límites orgánicos de Vue 3. La buena noticia es que Vue 3 soporta de manera excepcional el paradigma Low-Code sin "Vendor Lock-in". He emitido el documento de diseño `docs/architecture/adr_007_vue3_lowcode_engine.md` resolviendo todas tus dudas de seguridad, rendimiento y aislamiento.

## Action Item Estratégico para el PO
Debes trasladar estas decisiones técnicas a garantías contractuales con el negocio. Por favor, actualiza inmediatamente la **US-003** en la Única Fuente de Verdad (`docs/requirements/v1_user_stories.md`) inyectando los siguientes Criterios de Aceptación (Gherkin):

```gherkin
  Scenario: [Arquitectura] Sandboxing Estricto contra XSS (AST Evaluator)
    Given que el constructor del iForm inyectó una regla de negocio Javascript en un campo dinámico
    When el motor de renderizado de la Pantalla 7 interpreta el formulario en el navegador
    Then la plataforma prohíbe estructuralmente el uso de la función `eval()`
    And toda expresión JS es analizada y ejecutada internamente mediante un intérprete de gramática seguro (Abstract Syntax Tree Parser) que rechaza cualquier intento de manipulación del `window`, `document` o peticiones `fetch`.

  Scenario: [Arquitectura] Factoría Reactiva de Zod On-The-Fly
    Given la estructura JSON del formulario generada por el IDE
    When el motor requiere validar los campos renderizados en pantalla
    Then el sistema NO emite archivos estáticos de código fuente JS para re-compilar
    And instanciará dinámicamente el esquema global cruzado utilizando la factoría de validaciones `zod` conectada en tiempo real a la memoria reactiva (`reactive()`) de Vue.

  Scenario: [Arquitectura] Aislamiento Perimetral CSS (Shadow DOM)
    Given que el usuario redactó reglas exóticas de CSS para colorear botones específicos de su Formulario
    When la pantalla cliente dibuja el componente en el Workdesk
    Then el iBPMS encapsulará todo el componente inyectado usando el estándar HTML5 "Shadow DOM"
    And ninguna de las clases CSS inyectadas podrá sangrar (Style Bleed) hacia el exterior ni distorsionar la barra superior o menús laterales de Tailwind corporativo.

  Scenario: [Arquitectura] Render Functions y Teleportación
    Given una directriz para renderizar componentes infinitamente anidados (Ej: Grillas dentro de Módulos dentro de Secciones)
    Then el motor subyacente de Vue prescindirá del HTML rígido (`<template>`) utilizando funciones programáticas puras de Virtual DOM (`h()`) para renderizado ultrarrápido
    And los Tooltips dinámicos o ventanas emergentes forzarán el uso de la etiqueta nativa `<Teleport to="body">` para romper el encierro del z-index y sobreponerse perfectamente en la jerarquía visual del monitor.
```

Una vez que consolides la `v1_user_stories.md` con estos lineamientos técnicos, la US-003 pasará a estado `Ready for Development` y los Agentes Frontend sabrán a qué herramientas avanzadas de Vue 3 anclarse.
