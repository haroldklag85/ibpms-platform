# Agentic Handoff Request: Arquitectura Hexagonal y Extensibilidad Interna (DevPortal)

**To:** Lead Software Architect Agent 
**From:** Product Owner (PO)
**Related US:** US-042 (DevPortal)
**Date:** 2026-03-07

## Corrección de Visión (Shift to Internal Extensibility)
El PO ha aclarado tajantemente la naturaleza del **DevPortal (Pantalla 13)**. Inicialmente asumimos que era un portal B2B para sistemas de terceros (Típico "Public Dev Portal"). Sin embargo, el PO define que este módulo es una herramienta **Estrictamente Interna** para nuestro propio equipo de desarrollo (Humanos y Agentes IA de código).

## Objetivo Arquitectónico del PO
El propósito del DevPortal es permitir al equipo crear "Súper Módulos" o interfaces/lógicas ultra-complejas que **no caben ni se pueden modelar en el BPMN Builder estándar** (Pantalla 6). 

Esta extensibilidad libre debe cumplir inquebrantablemente con el paradigma de **Arquitectura Hexagonal** del Core iBPMS:
1.  **Frontend Desacoplado:** El equipo puede crear el Frontend en Vue/React de forma totalmente independiente.
2.  **Uso Obligatorio de Puertos:** Para comunicarse con la base de datos o el motor de flujos, el "Súper Módulo" desarrollado DEBE utilizar las credenciales (OIDC) emitidas por la Pantalla 13 y consumir los endpoints oficiales del Hexágono Interno. No se permite acceso a base de datos *raw*.
3.  **Sandboxing:** Todo el código Custom generado por el equipo o los Agentes debe estar aislado perimetralmente para no reventar el Core en caso de errores de memoria o Deadlocks.

## Action Item para el Arquitecto
Por favor, analiza este mandato y presenta una propuesta / diagrama lógico detallando:
*   ¿Cómo se orquestará el despliegue de estos Súper Módulos Custom (Frontends paralelos)? (Ej: Micro-frontends, iframes, reverse-proxies).
*   ¿Cómo se validarán los JWT Tokens emitidos por el DevPortal en nuestros Adapters (Controllers) de Spring Boot para diferenciar el "Core Access" del "Extensibility Access"?
*   Entrega tu análisis como un Blueprint o Policy Document formal para alinear a los Agentes FullStack.
