# Handoff Frontend — Iteración Sprint PM-01, Slot 4

## 1. Metadatos y SSOT
- **Iteración/Sprint:** Sprint PM-01, Slot 4
- **Rama de Trabajo:** `sprint-8/pm-01/us-008-kanban-real`
- **User Story:** US-008 (Vista Kanban)
- **Criterios de Aceptación (CAs) Target:** CA-4, CA-5, CA-6, CA-8, CA-12
- **Path del SSOT:** `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_A_motor_core.md` (Líneas 737-830)
- **Flujo de Trabajo:** Backend -> Frontend -> QA

## 2. Alineación Arquitectónica y ADRs
- **ADR-010 (Zero-Mock):** Eliminación total de datos falsos. El tablero Kanban ahora deberá poblarse haciendo una llamada HTTP real al Backend.
- **ADR-002 (Vue3 Microfrontends):** Se requiere el uso de Pinia para estado y Axios para HTTP. El componente principal Kanban debe reutilizar estado donde sea posible, pero consumir los endpoints específicos que retornan la grilla.
- **Trazabilidad:** Se asegura que el UX mantenga Optimistic UI para movimientos, pero recupere ante un Conflict (409) debido al *Single-Assignee* (CA-4).

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a modificar:** `frontend/src/stores/kanbanStore.ts` -> Conectar con Axios a los nuevos endpoints de Kanban. Retirar tópicos WebSocket falsos.
- **Archivo a modificar:** `frontend/src/views/kanban/KanbanView.vue` (o la ruta donde esté la vista) -> Eliminar hardcoded items, consumir `kanbanStore`.
- **Archivo a modificar:** Modal de visualización de tareas (`TaskPreviewModal` u análogo) -> Asegurar que abra la misma tarea del Workdesk usando `originalTaskId` si corresponde.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### Identificación de Mocks (Para Eliminar):
- Archivos TS o componentes Vue que importen data pre-cargada.
- Eliminación de `setTimeout` que simulen red en el store de Kanban.

### Frontend - Consumo de Endpoints Reales
En el `kanbanStore.ts`:
```typescript
import { defineStore } from 'pinia';
import apiClient from '@/api/client';

export const useKanbanStore = defineStore('kanban', {
  state: () => ({
    columns: [],
    loading: false
  }),
  actions: {
    async fetchKanbanBoard(projectId: string) {
      this.loading = true;
      try {
        const response = await apiClient.get(`/api/v1/projects/${projectId}/kanban`);
        this.columns = response.data.columns;
      } finally {
        this.loading = false;
      }
    },
    async moveTask(projectId: string, taskId: string, newStatus: string, oldStatus: string) {
      // Aplicar Optimistic UI (Mover en state local)
      
      try {
        await apiClient.patch(`/api/v1/projects/${projectId}/kanban/tasks/${taskId}/state`, {
          new_status: newStatus
        });
      } catch (error) {
        // En caso de error (e.g. 409 Conflict - CA-4), rollback a oldStatus
        console.error("Rollback ejecutado", error);
        // Lógica de rollback en array local
      }
    }
  }
});
```

### Frontend - WebSockets (CA-12)
Asegurar que el Kanban escuche eventos de STOMP / WS:
```typescript
// Suscribirse a /topic/workdesk/kanban para actualizar tareas movidas por otros usuarios
// Si un evento indica "KT-050 moved to IN_PROGRESS", actualizar grilla local.
```

## 5. Matriz de QA y Testing Atómico
**Agente QA / TDD Frontend:**
- `KanbanView.spec.ts` o `kanbanStore.spec.ts` en `frontend/tests/`.

| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| kanbanStore_fetches_real_data | ADR-010 | Mockear Axios. Al llamar `fetchKanbanBoard`, las columnas locales coinciden con el mock de Axios y NO existen mocks nativos en Vue. |
| kanbanStore_optimistic_rollback_on_conflict | CA-4 | Al fallar `moveTask` con 409, la tarea vuelve a `oldStatus` en las columnas. |
| kanban_websocket_event_updates_state | CA-12 | Al recibir un evento WS simulado, la tarea cambia de columna. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> "Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
