# Handoff Arquitectónico — FRONTEND (US-007 Ejecución BPMN)

## 1. 🗂️ METADATOS DEL HANDOFF
- **Rol Destino**: Frontend (Vue 3, TypeScript, Pinia, Tailwind)
- **Iteración/Slot**: Sprint PM-01, Slot 3
- **Historia de Usuario**: US-007 (Ejecución BPMN)
- **Alineación Arquitectónica**: ADR-002 (Microfrontends y Pinia).

## 2. 🎯 CONTEXTO Y OBJETIVO
El backend proveerá endpoints para iniciar procesos y completar tareas de usuario.
**Objetivo**: Consumir estos endpoints reales para permitir que un usuario final inicie un caso (proceso BPMN) desde la UI y pueda completar los "pasos" (User Tasks) asociadas al flujo.

## 3. 🧩 CAs A IMPLEMENTAR
- UI para listar procesos disponibles y botón para **"Iniciar Proceso"**.
- UI en la bandeja (Workdesk) para **"Completar Tarea"** (si no lo cubre US-029).
- Manejo de estado con Axios en Pinia Stores correspondientes (`useProcessStore`).
- Manejo de errores de negocio desde el backend (e.g. 400 Bad Request) mostrando notificaciones (Toasts).

## 4. 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA Y REGLAS ESTRICTAS
1. Verifica que el backend corre (`http://localhost:8080/actuator/health`).
2. **Zero Mocks**: Axios debe apuntar a la API real del backend. Borrar/evitar dependencias a `mockAdapter.ts`.

## 5. 🚦 SECUENCIA DE EJECUCIÓN (UI AUDIT)
1. Conectar las vistas de inicio de procesos a los endpoints reales.
2. Build/Compile obligatorio: Ejecuta el protocolo Zero-Trust (`npm run build`).
3. Verificar que la UI reacciona adecuadamente a las respuestas 200 y 400.
4. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
5. Commit y push.

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia en modo `PLANNING` y elabora tu plan.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud en `.agentic-sync/approval_request_FRONTEND_US007.md`.
4. Dile al Humano: *"Humano, he dejado mi solicitud en `.agentic-sync/approval_request_FRONTEND_US007.md`. Entrégala al Arquitecto."*
5. Espera. Al ser aprobado, pasa a modo `EXECUTION`.
6. ANTES del commit final, actualiza `CHANGELOG_NO_TECNICO.md`.
7. Finaliza con `git commit` y `git push` en la rama indicada. Queda prohibido usar git stash.
