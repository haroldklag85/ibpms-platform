# Handoff Arquitectónico: Backend
**Iteración:** 01-DEV-034-DAVID
**Épica:** 12 — Integraciones (US-034)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-034 implementa el andamiaje principal de orquestación asíncrona mediante RabbitMQ. En esta fase (CA-1 a CA-5), el backend debe integrarse con RabbitMQ, declarar la topología inicial, exponer el API del Dashboard DLQ y asegurar la idempotencia de los workers (consumidores). 

## 2. Alineación Arquitectónica
- **ADR-001 (Hexagonal):** Los listeners de RabbitMQ son Adaptadores de Entrada (Inbound/Primary Adapters) y deben residir en `infrastructure/messaging/`. Los Publishers son Adaptadores de Salida (Outbound/Secondary Adapters). Toda la lógica de idempotencia debe ser un servicio de dominio o infraestructura y no mezclarse con la lógica de negocio.
- **Stack Aprobado:** Java 17+, Spring Boot, Spring AMQP (RabbitMQ), PostgreSQL.

## 3. Requisitos Técnicos y Entregables (Backend)

**A. Configuración Spring AMQP (CA-1, CA-4):**
- Implementar la configuración (`RabbitMQConfig.java`) definiendo el `ibpms.exchange.topic`, `ibpms.exchange.dlx`, y la cola global `ibpms.dlq.global`.
- El catálogo de Routing Keys debe respetarse (ej. `{dominio}.{prioridad}.{accion}`).

**B. Endpoint para DLQ Dashboard (CA-2):**
- Crear `AdminQueueController.java` exponiendo:
  - `GET /api/v1/admin/queues/dlq/summary`: Retorna conteo total y mensajes más antiguos en DLQ.
  - `POST /api/v1/admin/queues/dlq/retry`: Reintenta mensajes.
  - `DELETE /api/v1/admin/queues/dlq/purge`: Purga la DLQ (debe estar protegido para el rol ADMIN_IT / Sudo-Mode).

**C. Priority Queues (CA-3):**
- Configurar el soporte de prioridad en la declaración de colas (`x-max-priority: 10`).

**D. Mecanismo de Idempotencia en Workers (CA-5):**
- Implementar `IdempotencyService` que valide el header `x-idempotency-key` contra la tabla `ibpms_processed_messages` creada por Infraestructura, aplicando ACK silencioso en caso de duplicados.

## 4. Criterios de Aceptación a Validar
- CA-1: Configuración de RabbitMQ integrada al ecosistema Spring Boot.
- CA-2: Endpoints REST creados y protegidos para el Dashboard DLQ.
- CA-3: Configuración de colas con soporte de prioridad.
- CA-4: Declaración del Exchange, DLX y DLQ global en código.
- CA-5: Servicio de Idempotencia funcional y acoplado a la BD.

## 5. Instrucciones de Compilación y NFR
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta "docs/architecture/arquitecturar.md". Ejecutar pruebas unitarias completas sobre los controladores, servicios de mensajería y persistencia de idempotencia.

## 6. Instrucciones Operativas y de Comunicación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
> 
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
