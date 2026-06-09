# Handoff: Estabilización Backend y Zero-Mock (Sprint 6.2)

## 1. Contexto y Objetivos
**US / BUG:** BUG-S6-004 (Timeouts en Delegación/Director), BUG-S6-005 (AssigneeMultiSelect Mock)
**Rama de Trabajo:** `sprint-6`
**Objetivo:** Erradicar bloqueos transaccionales en operaciones de Kanban/Workdesk que causan timeouts masivos bajo alta concurrencia. Exponer el catálogo real de usuarios para el Frontend.

## 2. Alineación Arquitectónica
- **ADR-001 (Hexagonal):** El endpoint de usuarios debe exponerse vía un Adapter (`UserController` o similar) y delegar a un Puerto (`UserQueryService` o `SecurityService`).
- **Bloqueos de Concurrencia:** Posibles fallos por `OptimisticLockingException` en Camunda durante operaciones simultáneas de Delegación/Claim.

## 3. Requerimientos Técnicos y Funcionales
- **GET /api/v1/users:** Validar si existe. Si no existe, crearlo para retornar una lista ligera de usuarios (id, email, username, roles básicos).
- **Timeouts Workdesk/Delegación:** Revisar si las operaciones JPA en `WorkdeskQueryController` o la lógica de Camunda TaskService (`claim`, `complete`) sufren de consultas N+1 o si les falta índices.

## 4. Tareas a Ejecutar
1. **Endpoint Usuarios:**
   - Crear o modificar `UserController` para exponer `GET /api/v1/users`. 
   - El payload de retorno debe ser útil para poblar el dropdown de `AssigneeMultiSelect.vue` (Mínimo: ID y Nombre/Email).
2. **Diagnóstico Concurrencia (Delegación):**
   - Revisar la lógica que asigna/delega tareas masivamente en la bandeja del Director (ej. `TaskAssignmentService`).
   - Implementar estrategias defensivas: logs sobre tiempos de ejecución JPA o manejo adecuado de reintentos ante `OptimisticLockingException`.

## 5. Criterios de Aceptación
- [ ] El endpoint `GET /api/v1/users` retorna exitosamente un JSON `200 OK` con la data poblada por el script SQL de UAT.
- [ ] No existen vulnerabilidades de N+1 queries al consultar la bandeja Workdesk.

## 6. Instrucciones Operativas y de Compilación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

*Compilación obligatoria:* Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
