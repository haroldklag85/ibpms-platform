# Handoff Frontend: Optimistic UI (J-04)

**Objetivo:** Implementar "Optimistic Updates" en el tablero Kanban y vistas interactivas del Workdesk para sortear las latencias de red y asfixia del backend, garantizando que los tests E2E no fallen por timeout visual.

**Instrucciones Arquitectónicas:**
1. **Kanban Drag & Drop:** Modificar los métodos en los Stores de Pinia (ej. `kanbanStore.ts`) que interactúen con el endpoint `PATCH /api/v1/kanban/tasks/{taskId}/move`. Al soltar la tarjeta, el estado local debe mutar **inmediatamente** reflejando el cambio visual sin hacer `await` bloqueante de Axios.
2. **Rollback UI:** Manejar el bloque `catch` del llamado asíncrono para revertir la tarjeta a su columna original si el backend responde con error (HTTP 4xx/5xx).
3. **Debounce / Retries:** Aplicar reintento con backoff exponencial o interceptores para códigos 503/429.

**Alineación Arquitectónica:**
- Se respeta ADR-002 (Vue3 Microfrontends / Pinia).
- La UI Optimista cuenta con aprobación directiva expresa para eludir los límites de infraestructura local.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
