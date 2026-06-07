# Handoff Arquitectónico — FRONTEND (US-030 Monitoreo BPMN)

## 1. 🗂️ METADATOS DEL HANDOFF
- **Rol Destino**: Frontend (Vue 3, TypeScript, Pinia, Tailwind)
- **Iteración/Slot**: Sprint PM-01, Slot 3
- **Historia de Usuario**: US-030 (Monitoreo BPMN)
- **Alineación Arquitectónica**: ADR-002 (Microfrontends y Pinia).

## 2. 🎯 CONTEXTO Y OBJETIVO
El backend proveerá endpoints de telemetría y datos operativos sobre la salud del motor BPMN.
**Objetivo**: Crear la vista de "Monitoreo" (Business Activity Monitoring - BAM) para que los supervisores o administradores visualicen los procesos activos, las métricas de incidentes y las instancias en estado suspendido o completado.

## 3. 🧩 CAs A IMPLEMENTAR
- UI Dashboard / Tabla de **Instancias de Proceso** con filtrado por estado.
- UI para ver detalles de **Incidentes** (log de errores técnicos de un proceso).
- Mapeo de datos usando Stores de Pinia correspondientes a telemetría.
- Diseño visual responsivo y alineado al sistema corporativo (Tailwind).

## 4. 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA Y REGLAS ESTRICTAS
1. **Zero Mocks**: Consumir datos reales del Backend. Está prohibido usar mock data en arrays estáticos o en Axios Mock Adapter.
2. Formato de Fechas/Tiempos debe ser amigable usando bibliotecas estándar de Vue/JS o Intl.

## 5. 🚦 SECUENCIA DE EJECUCIÓN (UI AUDIT)
1. Integrar el Dashboard de Monitoreo con los endpoints reales.
2. Build/Compile obligatorio: Ejecutar SRE del frontend (`npm run build`).
3. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
4. Commit y push.

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia en modo `PLANNING` y elabora tu plan.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud en `.agentic-sync/approval_request_FRONTEND_US030.md`.
4. Dile al Humano: *"Humano, he dejado mi solicitud en `.agentic-sync/approval_request_FRONTEND_US030.md`. Entrégala al Arquitecto."*
5. Espera. Al ser aprobado, pasa a modo `EXECUTION`.
6. ANTES del commit final, actualiza `CHANGELOG_NO_TECNICO.md`.
7. Finaliza con `git commit` y `git push` en la rama indicada. Queda prohibido usar git stash.
