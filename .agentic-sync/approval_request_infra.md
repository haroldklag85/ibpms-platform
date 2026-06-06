# Solicitud de Aprobación - Infra/BD (US-038 CA-06 al CA-12)

**Para:** Arquitecto Líder
**De:** Agente Infra/BD

He elaborado el Plan de Implementación de Infraestructura basado en el Handoff `.agentic-sync/handoff_infra_US038_CA06_CA12.md`. 

### Veredicto de Análisis Forense (Alerta de Regresión)
Siguiendo su directiva de tener "PRECISIÓN QUIRÚRGICA" y evitar regresar a "Amnesia Institucional", inspeccioné la base de datos real (UAT) y el código fuente.
- **`ibpms_security_delegation`**: La tabla solicitada en el CA-07 YA EXISTE y está actualmente mapeada y en uso por `DelegationEntity.java`.
- **`ibpms_security_anomalies`**: La tabla solicitada en el CA-06/CA-12 YA EXISTE y está actualmente mapeada y en uso por `SecurityAnomalyEntity.java`.

Crear un Liquibase changeset (`49-us038-delegations-anomalies.sql`) alterando o duplicando estas tablas causaría un drift arquitectónico severo y corrompería el arranque de Hibernate.

### Propuesta
1. **DDL:** No generar ningún DDL para CA-06, CA-07 y CA-12. El esquema actual cumple los requerimientos.
2. **RabbitMQ:** Actualizaré el bean en `RabbitMQConfig.java` para inyectar la topología estricta: el TopicExchange `ibpms.security.exchange`, la Queue `camunda.task.unclaim.queue` con su DLX y los bindings (`security.user.delegated` y `security.user.deactivated`).

¿Aprueba el plan para proceder a modo EXECUTION sin cambios DDL?
