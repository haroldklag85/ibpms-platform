# Handoff Arquitectónico — BACKEND (US-007 Ejecución BPMN)

## 1. 🗂️ METADATOS DEL HANDOFF
- **Rol Destino**: Backend (Java 17, Spring Boot)
- **Iteración/Slot**: Sprint PM-01, Slot 3
- **Historia de Usuario**: US-007 (Ejecución BPMN)
- **Alineación Arquitectónica**: ADR-001 (Hexagonal), ADR-003 (Camunda Embedded).

## 2. 🎯 CONTEXTO Y OBJETIVO
Las funcionalidades para ejecutar e interactuar con instancias de procesos BPMN están incompletas. 
**Objetivo**: Exponer o completar los endpoints REST (en `domain/` adaptados mediante controllers) que permitan a la UI iniciar una instancia de proceso, y completar tareas de usuario (`UserTask`) a través de la API del motor (Camunda). 

## 3. 🧩 CAs A IMPLEMENTAR
- Endpoint para **iniciar un proceso BPMN** por `processDefinitionKey` pasando variables.
- Endpoint para **completar una tarea** de usuario por `taskId`, adjuntando variables.
- Respetar los contratos en `API_CONTRACTS.md` o crearlos si no existen.
- Lógica de negocio ubicada estrictamente en los Use Cases / Ports (Hexagonal).

## 4. 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA Y REGLAS ESTRICTAS
El backend corre en puerto `8080` (host local), Postgres en `5433` (Docker).
1. `curl -s http://localhost:8080/actuator/health` → `{"status":"UP"}`
2. **Zero Mocks**: La integración debe ser contra las APIs nativas de Camunda (`RuntimeService`, `TaskService`), prohibido usar mocks en la capa de servicios.

## 5. 🚦 SECUENCIA DE EJECUCIÓN (SRE AUDIT)
1. Implementar `StartProcessUseCase` y `CompleteTaskUseCase`.
2. Exponer a través de Controladores REST.
3. Compilación obligatoria (`mvn clean compile`).
4. Arrancar y probar los endpoints vía curl o TestRestTemplate.
5. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
6. Commit y push.

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia en modo `PLANNING` y elabora tu plan.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud en `.agentic-sync/approval_request_BACKEND_US007.md`.
4. Dile al Humano: *"Humano, he dejado mi solicitud en `.agentic-sync/approval_request_BACKEND_US007.md`. Entrégala al Arquitecto."*
5. Espera. Al ser aprobado, pasa a modo `EXECUTION`.
6. ANTES del commit final, actualiza `CHANGELOG_NO_TECNICO.md`.
7. Finaliza con `git commit` y `git push` en la rama indicada.
