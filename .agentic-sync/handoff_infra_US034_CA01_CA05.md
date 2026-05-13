# Handoff Arquitectónico: Infraestructura / Base de Datos
**Iteración:** 01-DEV-034-DAVID
**Épica:** 12 — Integraciones (US-034)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-034 exige delegar el rate-limiting y encolamiento asíncrono del sistema a un Message Broker de grado Enterprise (RabbitMQ). El objetivo es habilitar alta demanda (CA-1), soportar prioridades de supervivencia (CA-3), registrar la topología oficial (CA-4) e implementar resiliencia estricta mediante idempotencia en workers (CA-5).

## 2. Alineación Arquitectónica
- **ADR-009 (PostgreSQL):** La idempotencia (CA-5) se soportará mediante una tabla SQL específica.
- **Stack Aprobado:** RabbitMQ (broker), PostgreSQL (DB), Liquibase (DDL).
- **Prohibición:** Se prohíbe usar PostgreSQL como mecanismo de encolamiento de alto tráfico (Database Deadlocks).
- **Riesgo Identificado:** Proliferación desordenada de colas. Se mitigará definiendo la topología oficial.

## 3. Requisitos Técnicos y Entregables (Infra/BD)

**A. Tabla de Idempotencia (CA-5):**
Deberás crear un changeset de Liquibase para la tabla `ibpms_processed_messages` que prevendrá el procesamiento duplicado de eventos.
Columnas requeridas:
- `id` (UUID / Primary Key)
- `idempotency_key` (UUID, Unique)
- `processed_at` (Timestamp)
- `queue_name` (String)

**B. Topología RabbitMQ (CA-4):**
Deberás actualizar el archivo `docs/architecture/rabbitmq_topology.md` con la topología base del proyecto:
- Exchange Principal: `ibpms.exchange.topic` (Topic)
- DLX (Dead Letter Exchange): `ibpms.exchange.dlx` que enrute a la cola `ibpms.dlq.global`.
- Asegurar que el entorno local (`docker-compose.yml`) levante RabbitMQ correctamente con el plugin de Management habilitado.

## 4. Criterios de Aceptación a Validar
- CA-1: Configuración de RabbitMQ como broker único.
- CA-4: Documentación de Topología RabbitMQ.
- CA-5: Creación del esquema `ibpms_processed_messages`.

## 5. Instrucciones de Compilación y NFR
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.

## 6. Instrucciones Operativas y de Comunicación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
> 
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
