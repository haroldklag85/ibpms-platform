# Handoff: AI DEVELOPER AGENT - FRONTEND
**Iteración:** 66-DEV (US-002 / CA-6 al CA-10)
**Contexto Aislado:** Vue 3 / Pinia. NO conoces Java.

## 1. MISIÓN Y REGLA DE V2
Integrar en la interfaz de Workdesk las funciones de Unclaim, DOM resync y limpieza de payloads huérfanos.
**REGLA DE V2:** Restringe el Scope a V1 puro.

## 2. NOTAS DE IMPLEMENTACIÓN TÉCNICA (TIN) EXIGIDAS
1. **WebSocket Micro-batching & DOM Reflow (CA-08):** Escucha el evento STOMP inverso (`{ event: 'TASK_UNCLAIMED', taskId: '123' }`). Usa la etiqueta `<transition-group name="list">` con CSS `.list-leave-active { transition: all 0.3s ease; } .list-leave-to { opacity: 0; transform: translateY(-20px); }` para desvanecimientos suaves (Anti-Jitter) cuando otra persona de tu Tenant devuelva una tarea al Pool.
2. **Data Purge UI Local (CA-07):** Si el usuario actual hace Unclaim de una tarea a la que ya le había adjuntado archivos locales sin enviar, purga esos Blob/FileReference de la tienda de Pinia inmediatamente al recibir HTTP 200 de Backend, para evitar Memory Leaks cruzados.

## 3. ENTREGABLE ESTRICTO
Consolida la vista, no modifiques MainLayout sino Workdesk. Empaqueta el código:
`git stash save "temp-frontend-US002-CA6-10"`
Notifica al humano al terminar.
