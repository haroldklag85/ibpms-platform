# Blueprint de Ingeniería (Auditado por Arquitectura): Pantalla 10.B (Planner Tradicional - Gantt)

Este documento contiene las especificaciones técnicas definitivas para construir la **Pantalla 10.B**, incorporando las directrices de Arquitectura de la V1 para garantizar transaccionalidad, cumplimiento legal (Open Source) y eficiencia en infraestructura. La P10.B es donde el PM toma un "Molde" de proyecto tradicional, le pone fechas, costos, recursos y **presiona Play**.

## 1. El Ciclo de Vida del Dato (El "Handoff")
1.  **Origen (P9):** El PM entra a la Pantalla 9, dice "Nuevo Proyecto", selecciona Metodología="Tradicional" y escoge una Plantilla `ib_project_template_id=X`.
2.  **Clonación Asíncrona:** El backend recibe ese POST, clona todo el árbol teórico de la BD (Fases > Hitos > Tareas de la P8) y crea copias específicas (instancias) atadas al `ib_project_metadata_id=Y` recién nacido.
3.  **Aterrizaje (P10.B):** El PM es redirigido a la Pantalla 10.B. El backend le envía su árbol instanciado. Todas las tareas nacen con el estado genérico *"No Iniciado"* y con la duración pre-cargada, pero sin fechas calendario fijas aún.

## 2. Modelo de Datos Adicional (Ejecución)
Las tablas de la P8 eran teóricas. Para la P10.B necesitamos añadir la "Realidad" sobre la tabla de Proyecto Central:

*   `ib_project_task_execution`: Instancia viva de una tarea.
    *   `(id, project_id, wbs_task_template_id, status [PENDING, IN_PROGRESS, DONE, BLOCKED], assignee_user_id, actual_budget, start_date_plan, end_date_plan, start_date_actual, end_date_actual, ...)`
*   `ib_project_baseline`: Fotografía de la planificación original inmutable.
    *   `(id, project_id, total_budget, created_at, created_by, is_active)`

## 3. API Contract (Backend Spring Boot)
El `ProjectExecutionController` expondrá:

1.  `GET /api/v1/execution/projects/{id}/gantt-tree`: Devuelve el árbol serializado, inyectando dependencias procesadas.
2.  `PUT /api/v1/execution/projects/tasks/{taskId}/assign`: Actualiza el `assignee_user_id` y `actual_budget`.
3.  `POST /api/v1/execution/projects/{id}/baseline`: **El Endpoint Maestro.** Congela el modelo matemático, fija la línea base y dispara la Orquestación.

## 4. Arquitectura Frontend (Vue 3)

### 4.1 UI Components Principales
*   **Motor Gantt Externo (⚠️ RIESGO LEGAL MITIGADO):** Queda **ESTRICTAMENTE PROHIBIDO** instanciar librerías de pago como `dhtmlxGantt` sin licencia explícita comprada. El Front Lead DEBE integrar una opción Open Source (MIT/Apache 2.0) comprobada para Vue 3. 
    *   *Opciones Aprobadas por Arquitectura:* `frappe-gantt` (wrapeado para Vue 3) o alternativas como `gantt-elastic` (verificando licencia). Cero-tear un Gantt desde cero tomaría meses, usen una librería OS.
*   **`ResourcePanel.vue` (Side-Drawer):** Al hacer doble clic en una barra del Gantt, emerge un Drawer derecho para:
    1.  Buscador de Usuarios (Autocomplete contra LDAP/EntraID simulado en V1) para asignar el recurso final.
    2.  Input numérico para inyectar Presupuesto ($).
    3.  Ajuste manual numérico de `Lag` o retraso si existe dependencia.

## 5. Acceptance Criteria Especiales: La Orquestación

*   **AC-1 (Fijar Línea Base):** Hasta que el PM no oprima el botón `[ 🚀 FIJAR LÍNEA BASE ]`, NINGÚN empleado es notificado, y el motor de procesos Camunda no sabe que este proyecto existe.
*   **AC-2 (El "Big Bang" de Camunda - 🛡️ TRANSACCIONALIDAD V1):** Cuando se oprime "Fijar Línea Base", el backend debe:
    1. Evaluar matemáticamente cuáles son las "Primeras Tareas" (sin dependencias entrantes).
    2. **[REGLA DE ARQUITECTURA]:** INVOCAR directamente el Java API local de Camunda (`RuntimeService.startProcessInstanceByKey()`) dentro de la misma transacción MySQL `@Transactional`. **PROHIBIDO usar llamadas HTTP/REST al propio localhost** para evitar fallas parciales sin Rollback. 
    3. Depositar mágicamente estas tareas en las bandejas (Workdesk - Pantalla 1) de los asignados seleccionados por el PM.
*   **AC-3 (Progreso Automático del Gantt - ⚡ EFICIENCIA V1):** La P10.B es solo lectura una vez fijada la Línea Base. Si en la Pantalla 1 un obrero marca "Excavación" al 100%, el sistema notificará a la P10.B para pintar la barra de verde.
    *   **[REGLA DE ARQUITECTURA]:** Queda **PROHIBIDO usar WebSockets** bidireccionales por el overhead en el APIM. El Agente Backend implementará **SSE (Server-Sent Events / text/event-stream)** mediante Spring WebFlux (`Flux<ServerSentEvent>`) o Emitter sincrónico para empujar la actualización visual de forma unilateral y ligera.
