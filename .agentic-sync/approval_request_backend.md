# Arquitecto Líder - Solicitud de Aprobación Backend (US-038 Fase 2)

## Contexto de la Solicitud
Se requiere tu aprobación arquitectónica antes de proceder con la implementación de **CA-06 al CA-12** (Gobernanza de Identidad) bajo la rama `DevDavid`.

## Puntos Clave del Plan
1. **SoD (CA-06):** Se creará `SoDValidatorDomainService` (pura regla de negocio) y se inyectará en los servicios de aplicación (`GenericFormService` / `CompletarTareaService`) para evaluar que `Creator_ID != Approver_ID`. Las violaciones se lanzarán como `SoDViolationException` y se registrarán asíncronamente en `SecurityAnomalyService`.
2. **RabbitMQ Topología (CA-07 y CA-08):** Se creará `RabbitMQSecurityConfig` para mapear el exchange `ibpms.security.exchange`. El productor existente `TaskRescueProducer` se dividirá para enviar `security.user.delegated` y `security.user.deactivated`.
3. **Tablero Anomalías (CA-12):** El controlador `SecurityAnomalyController` ya existe y está alineado; se aplicará TDD para garantizar su correcto funcionamiento sin inyecciones adicionales complejas.

## Pregunta Abierta (Requiere Resolución)
> [!WARNING]
> ¿Debemos buscar el `Creator_ID` de la tarea inspeccionando la variable `initiator` del proceso en Camunda, o existe alguna otra variable estandarizada que se deba utilizar para esta validación (ej. `startUserId` o una variable específica de los formularios genéricos)?

Por favor revisa el plan en `implementation_plan.md` y emite tu veredicto (APROBADO, APROBADO CON OBSERVACIONES, o RECHAZADO).
