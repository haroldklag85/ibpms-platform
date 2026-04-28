# Handoff: AI DEVELOPER AGENT - BACKEND
**Iteración:** 66-DEV (US-002 / CA-6 al CA-10)
**Contexto Aislado:** Backend Java / Spring Boot.

## 1. MISIÓN Y REGLA DE V2
Tu misión es desarrollar las operaciones secundarias del Motor de Reclamos (Release, Unclaim, Delegación).
**REGLA DE V2:** Ignora requerimientos de aprendizaje automático masivo, analítica predictiva o refactorización fuera de V1.

## 2. NOTAS DE IMPLEMENTACIÓN TÉCNICA (TIN) EXIGIDAS
1. **IDOR & Cross-Tenant Validation (CA-06):** ESTRICTAMENTE PROHIBIDO confiar en que el Frontend envió el ID correcto. En el des-reclamo (`Unclaim`), extrae el Principal del JWT: `if (task.getTenantId() !== jwt.getTenantId()) throw new AccessDeniedException()`.
2. **Data Purge Hook S3 Orphans (CA-07):** En `POST /api/tasks/{id}/unclaim`, revisa la tabla de adjuntos temporales. Si hay UUIDs atados a esa Tarea pertenecientes a esta misma sesión de usuario, inyecta `@Async` para accionar `AWS S3 deleteObjects` ANTES de devolver el 200 OK.
3. **Persistencia WebSockets (CA-08):** Inyecta la publicación al Message Broker (Redis PubSub / RabbitMQ) indicando que la tarea ha regresado al Pool general ({ event: 'TASK_UNCLAIMED', taskId='123' }) tras un unclaim exitoso.

## 3. ENTREGABLE ESTRICTO
Realiza las pruebas transaccionales. Empaqueta el código sin tocar UI:
`git stash save "temp-backend-US002-CA6-10"`
Notifica al humano.
