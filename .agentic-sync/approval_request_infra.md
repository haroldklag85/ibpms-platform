# Solicitud de Aprobación Arquitectónica: Infraestructura US-034 (CA-01 al CA-05)

**De:** Agente Infra/BD
**Para:** Arquitecto Líder
**Rama:** DevDavid
**Iteración:** 01-DEV-034-DAVID

Arquitecto, he completado mi fase de PLANNING y el `implementation_plan.md` está listo. 

**Resumen de hallazgos y plan propuesto:**
1. **Topología RabbitMQ (CA-4):** Auditada exitosamente. Los archivos `docs/architecture/rabbitmq_topology.md` y el `docker-compose.yml` ya se encuentran totalmente alineados con la arquitectura solicitada.
2. **Tabla de Idempotencia (CA-5):** Identifiqué que la tabla `ibpms_processed_messages` existe en BD (vía el script anterior `19-create-rabbitmq-resilience-tables.sql`) pero con `idempotency_key` como Primary Key. 
   Propongo un changeset `47-us034-idempotency.sql` que hace un DROP a la PK actual, inyecta `id` (UUID) como nueva PK, y hace `idempotency_key` de tipo UUID y UNIQUE.

**🚨 ALERTA ARQUITECTÓNICA Y SOLICITUD DE DESBLOQUEO:**
Como Agente Infra/BD, tengo permiso para modificar la Entidad JPA (`ProcessedMessageEntity.java`) para sincronizar el esquema. Sin embargo, al cambiar el `@Id` a la nueva columna `id` (UUID), el archivo `ProcessedMessageRepository.java` (y por ende `IdempotencyGuard.java`) romperán instantáneamente la compilación al esperar un `<Entity, String>`.

Tengo **estrictamente prohibido** tocar la capa de Repositorios/Servicios. Si aplico el cambio, el build se corromperá y fallaré la instrucción *"cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato"*.

**¿Cómo debo proceder?**
A) ¿Me otorgas excepción temporal para alterar únicamente la firma de `ProcessedMessageRepository` a `<ProcessedMessageEntity, UUID>` y ajustar `IdempotencyGuard.java` para mantener la compilación verde?
B) ¿Aplico solo Liquibase + Entity y dejo la rama "rota" para que el Agente Backend lo repare en su sprint inmediato, ignorando la caída del build?

Espero tus instrucciones para pasar a modo `EXECUTION`.
