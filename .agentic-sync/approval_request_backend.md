# Solicitud de Aprobación Arquitectónica - Integración RabbitMQ (US-034 CA-01 al CA-05)

**Dirigido a:** Arquitecto Líder
**De:** Agente Backend
**Rama:** DevDavid
**Iteración:** 01-DEV-034-DAVID

## Diagnóstico y Alineación
He analizado las instrucciones técnicas para los Criterios de Aceptación CA-01 al CA-05 relacionados con la integración de RabbitMQ y el andamiaje asíncrono. Los entregables requieren intervención estricta en las capas de Infraestructura (Adaptadores) y Aplicación (Servicios), manteniendo aislamiento de negocio según ADR-001.

## Plan de Ejecución
Solicito autorización formal para proceder con el diseño detallado en el `implementation_plan.md`. En resumen:

1. **Configuración de Topología (CA-1, CA-3, CA-4):**
   - Creación de `RabbitMQConfig.java`.
   - Definición del Exchange `ibpms.exchange.topic` y DLX `ibpms.exchange.dlx`.
   - Colas base equipadas con prioridad `x-max-priority: 10` y DLQ Global.
2. **Idempotencia (CA-5):**
   - Entidad JPA `ProcessedMessageEntity` mapeada a la tabla ya estructurada por Infra.
   - Creación de `IdempotencyService.java` para protección contra duplicados (Fail-Safe con ACK silencioso).
3. **Admin DLQ Dashboard (CA-2):**
   - Creación de `DlqManagementService.java` que extraerá métricas de RabbitMQ y re-encolará/purgará mensajes usando `RabbitTemplate` y `RabbitAdmin`.
   - Exposición en `AdminQueueController.java` (`GET summary`, `POST retry`, `DELETE purge`).
4. **Verificación:** Cobertura TDD estricta sobre estos nuevos controladores y servicios, finalizando con la validación del protocolo SRE (`mvn clean compile test`).

Por favor emite tu veredicto formal para iniciar la fase `EXECUTION`.
