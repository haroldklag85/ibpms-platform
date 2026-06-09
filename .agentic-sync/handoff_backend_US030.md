# Handoff Arquitectónico — BACKEND (US-030 Monitoreo BPMN)

## 1. 🗂️ METADATOS DEL HANDOFF
- **Rol Destino**: Backend (Java 17, Spring Boot)
- **Iteración/Slot**: Sprint PM-01, Slot 3
- **Historia de Usuario**: US-030 (Monitoreo BPMN)
- **Alineación Arquitectónica**: ADR-001 (Hexagonal), ADR-003 (Camunda Embedded).

## 2. 🎯 CONTEXTO Y OBJETIVO
Se requiere visualizar el estado de salud y métricas de los procesos que corren en el motor.
**Objetivo**: Exponer métricas de Camunda (vía `HistoryService` y `RuntimeService`) para listar instancias activas, completadas, incidentes (errores técnicos) y cuellos de botella (tareas retrasadas).

## 3. 🧩 CAs A IMPLEMENTAR
- Endpoint para **Listar Instancias de Proceso** (Paginado), incluyendo estado (ACTIVE, COMPLETED, SUSPENDED).
- Endpoint para **Listar Incidentes** (Errores de ejecución).
- Lógica encapsulada en la capa de Use Cases (`GetProcessInstancesUseCase`, `GetProcessIncidentsUseCase`).

## 4. 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA Y REGLAS ESTRICTAS
1. Verifica que el backend corre (`http://localhost:8080/actuator/health`).
2. **Zero Mocks**: Evitar endpoints "falsos" o in-memory. Usa la API de Camunda genuina.

## 5. 🚦 SECUENCIA DE EJECUCIÓN (SRE AUDIT)
1. Desarrollar Use Cases de monitoreo.
2. Implementar los controladores REST según contratos API.
3. Compilación obligatoria (`mvn clean compile`).
4. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
5. Commit y push.

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia en modo `PLANNING` y elabora tu plan.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud en `.agentic-sync/approval_request_BACKEND_US030.md`.
4. Dile al Humano: *"Humano, he dejado mi solicitud en `.agentic-sync/approval_request_BACKEND_US030.md`. Entrégala al Arquitecto."*
5. Espera. Al ser aprobado, pasa a modo `EXECUTION`.
6. ANTES del commit final, actualiza `CHANGELOG_NO_TECNICO.md`.
7. Finaliza con `git commit` y `git push` en la rama indicada.
