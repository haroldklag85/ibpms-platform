# Solicitud de Aprobación del Arquitecto - Backend US-007 Bloque 1

**A: Arquitecto Líder**
**De: Agente Backend**

He iniciado el modo `PLANNING` para el Bloque 1 de la US-007 (Generador Cognitivo DMN). Tras revisar las directrices arquitectónicas de la historia y los SKILLS requeridos (TDD, Clean Code, Zero-Trust SRE), he generado un plan de implementación.

## Resumen del Plan de Trabajo
El plan detalla la remediación de 12 GAPs en 3 Fases incrementales:

1. **Fase 1: Validadores de Dominio y Preprocesamiento**
   - GAP-17: Normalización de Prompt para Caché (LowerCase, Trimming).
   - GAP-04: Enmascaramiento PII del Prompt hacia el LLM.
   - GAP-06: Sanitización de Variables Planas y prohibición de Date-Math.
   - GAP-07: Validaciones Cognitivas de Capacidad (Filas, Output único, Overlap) y Token Limit.

2. **Fase 2: Gobernanza y Restricciones XML**
   - GAP-18: Fallback Original frente a errores del Minificador DMN.
   - GAP-19: Enforce de hitPolicy=FIRST en uploads DMN.
   - GAP-20: Limitador de peticiones al Simulador (20 rq/min).
   - GAP-26: Promoción incondicional a versión V2 y Source `NLP_MODIFIED` tras edición humana.
   - GAP-02: Rectificación y ajuste de periodicidad del GC para el Draft Cleanup TTL.

3. **Fase 3: Integraciones Inter-dominio**
   - GAP-12: Conexión con analizador BPMN para requerir Gateways si existe Catch-All.
   - GAP-14: Conector asíncrono RabbitMQ para purga focalizada del caché en Redis.
   - GAP-16: Marcado generalizado OpenAPI en endpoints DMN.

**Reglas aplicadas:**
Todas las fases incluirán pruebas bajo metodología **TDD (Red -> Green -> Refactor)** y superarán la verificación local de **Zero-Trust (mvn compile y test)** antes de subir cambios a `sprint-6`.

El detalle físico del plan reside en: `.agentic-sync/implementation_plan_US007.md`.

*¿Autorizas que transicione al modo EXECUTION?*
