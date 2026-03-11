# 🤖 [PROMPT MAESTRO] - Misión Épica: Pantalla 10.B (Planner Tradicional - US-031)

**PARA:** Squad de Agentes de Desarrollo (Backend, Frontend, QA/DevOps)
**DE:** Lead Architect
**OBJETIVO:** Implementar el módulo de Ejecución de Proyectos (Gantt) según la US-031, acatando estrictamente el Blueprint de Arquitectura V1 Auditado.

---

## 📜 DIRECTRICES ARQUITECTÓNICAS MANDATORIAS (0-TRUST & V1-COMPLIANCE)

Atención Escuadrón. Queda **ESTRICTAMENTE PROHIBIDO** desviarse del documento fuente oficial:
📄 `ibpms-platform/docs/architecture/blueprint_pantalla10b_auditado.md`

### 🛠️ TAREAS DEL AGENTE BACKEND (Java 17 + Spring Boot 3 + Camunda 7 Embebido)
1. **Modelo de Datos:** Crea las entidades `ProjectTaskExecution` y `ProjectBaseline` en `com.ibpms.core.project.domain`. Repositorios e interfaces de servicio para transaccionalidad con PostgreSQL.
2. **API Contract FIRST:** Escribe el Contrato API en el archivo `.agentic-sync/epic10b_gantt_api_contract.md`. Define los endpoints:
   - `GET /api/v1/execution/projects/{id}/gantt-tree`
   - `PUT /api/v1/execution/projects/tasks/{taskId}/assign`
   - `POST /api/v1/execution/projects/{id}/baseline` (Botón Maestro)
3. **Big Bang Transaccional (AC-2):** El endpoint `/baseline` debe estar marcado con `@Transactional`. Debe instanciar la(s) primera(s) tarea(s) en Camunda usando **EXCLUSIVAMENTE** `RuntimeService.startProcessInstanceByKey()`. **PROHIBIDO USAR HTTP/REST CLIENTS** hacia nuestro propio motor. Si el insert en DB falla, Camunda debe hacer rollback instantáneo.
4. **SSE para el AC-3:** Crea el controlador para Server-Sent Events (NO WebSockets) que empujará notificaciones al Frontend cuando una tarea cambie de estado desde el Workdesk. Usa `SseEmitter` o `Flux<ServerSentEvent>`.
5. **Handoff:** Confirma en este chat tu finalización publicando el Contrato API e indicando al Frontend que inicie.

### 🎨 TAREAS DEL AGENTE FRONTEND (Vue 3 + Tailwind + Vite)
1. **Espera de Contrato:** No inicies hasta que el Backend exponga su `.agentic-sync/epic10b_gantt_api_contract.md`.
2. **Setup del MSW:** Al recibir el contrato, mapea el Mock Service Worker (`mockAdapter.ts`) antes de programar las vistas UI para simular el Backend localmente (Defensive Mock-First Development).
3. **El Motor Gantt (AC-1):** **PROHIBIDO** usar librerías bajo licencias GPL-3.0 completas o comerciales cerradas. Integra `frappe-gantt` instalándolo en tu `package.json` (`npm i frappe-gantt`). Crea un Componente Vue Wrapper que consuma el JSON serializado del Backend para pintar las barras.
4. **UX del Cajón (Drawer):** Crea el componente `ResourcePanel.vue` (Side-Drawer derecho) para asignar usuarios mediante un dropdown (Autocomplete simulado) y capturar el input numérico del Presupuesto. Todo esto llama a tu Mock de `PUT /assign`.
5. **Consumo SSE:** Implementa un `EventSource` pasivo apuntando al endpoint que defina el Backend para reaccionar al pintar las barras verdes (100% progreso).

### 🧪 ⚙️ TAREAS DEL AGENTE QA/DEVOPS (Calidad e Infraestructura Azure)
1. **Backend Tests:** Valida con un Unit Test (RestTemplate/MockMvc) que el endpoint de `/baseline` ejecute su bloque `@Transactional` logrando exitosamente invocar el mock del `RuntimeService`.
2. **Frontend Tests:** Levanta un test con `Vitest` para el componente Vue Wrapper de `frappe-gantt`. Asegúrate de que el botón `[ 🚀 FIJAR LÍNEA BASE ]` esté bloqueado (Disabled) hasta que el MSW no garantice que se han llenado presupuestos y asignaciones requeridas de la US-031.
3. **Reporte:** Escribe el resultado exitoso (Exit Code 0) en `.agentic-sync/epic10b_qa_report.md`.
4. **Infraestructura:** Valida que nuestro cuestionario `ibpms-platform/docs/architecture/azure_cicd_questionnaire.md` haya sido entregado.
5. **Configuración DevOps:** Añade este nuevo controlador SSE de Planner a la lista de chequeo de *Health Check* del pipeline potencial Azure Docker-Compose, garantizando que el Nginx/Ingress pueda sostener conexiones Keep-Alive para EventSource sin tirar Timeout a los clientes.

---
**Protocolo de Ejecución:**
Ejecuten sus labores en Secuencia 🔀: Backend -> (ESPERAR) -> Frontend -> (ESPERAR) -> QA/DevOps.
Reporten a la nave madre al conluir. ¡Inicien! 🟢
