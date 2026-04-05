# Solicitud de Aprobación - Arquitecto Líder
**De:** Agente Backend (Java / Spring Boot)
**Para:** Arquitecto Líder (Orquestador)
**Referencia:** US-034 / CA-4 a CA-10 — Arquitectura Resiliente asíncrona de RabbitMQ

He consolidado la instrucción del handoff para integrar un robusto ecosistema de colas con idempotencia y Circuit-Breakers. Mi plan respeta taxativamente la prohibición de borrados al `TaskRescueRabbitConfig.java` y prohíbe el uso de SQL a modo de cola natural.

## Detalles Clave del Despliegue de Resiliencia
1. **Topología (CA-4):** Instauraré el `Topic Exchange` primordial y la zona DLX. `RabbitMqTopologyConfig.java` manejará las 5 colas core y la DLQ, delegando hacia el DLX los fallos. 
2. **Idempotencia (CA-5):** Cimentaré la tabla Liquibase `ibpms_processed_messages` y montaré el `IdempotencyGuard` interceptando la UUID como llave para vetar duplicidad.
3. **Calibración de Retry (CA-7):** Construiré un Backoff Interceptor exponencial finalizando tras 4 intentos con sus Headers respectivos en caso de aborto final en el DLX.
4. **Gobierno DLQ (CA-8, CA-9):** Construiré el Dashboard REST con `Sudo-Mode` preventivo y habilitaré el Archivero Scheduler cruzando contra la tabla `ibpms_dlq_archive` luego de sortear su caducidad de TTL en la DLQ central.
5. **Circuit Breaker (CA-10):** Habilitaré el Actuator indicator `RabbitHealthIndicator` que disparará el circuito abierto al encadenar tres caídas, refugiándose en memoria y en una última instancia en tabla (`ibpms_queue_fallback`).

En base a la directiva impartida, mi planificación de inyecciones y bases relacionales accesorias (sólo funcionales a idempotencia y fallback, mas no a queueing) resulta apegada a la norma Zero-Trust y garantizo que al culminar la JVM será comprobada.

Arquitecto, ¿autorizas la materialización total de este entramado asíncrono hacia el estado de `EXECUTION`?
