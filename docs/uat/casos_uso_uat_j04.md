# Casos de Uso UAT — Journey J-04 (v2 — Certificación E2E Operario)

> **Journey:** Recepción → Claim → Formulario → Completar → Kanban → Delegación → Skipeo → Resiliencia  
> **Perspectiva:** Operario / Analista / Perito / Director — Workdesk & Kanban  
> **Proceso BPMN:** Reutiliza instancias de `insurance_claims_complex.bpmn` creadas en J-02  
> **Criticidad:** 🔴 ALTA — Si el Workdesk no funciona, nadie trabaja  
> **Épicas cruzadas:** Workdesk (É1) → Formularios (É2) → Kanban (É3) → CQRS (É16) → SLA (É8)  
> **US involucradas:** US-001, US-002, US-008, US-017, US-029, US-036, US-039, US-043, US-051  
> **Fecha:** 2026-04-19 (v2 — Reescritura Certificación E2E)  
> **Autor:** Agente PO + Antigravity

> [!IMPORTANT]
> **v2 — Reescritura Integral:** Este documento reemplaza J-04 v1. Cambios principales:
> - Reutiliza instancias de J-02 (continuidad narrativa Arquitecto → Operario)
> - **4 usuarios** simultáneos: Analista N1, Perito A, Perito B, Director
> - **7 capacidades avanzadas** del Workdesk validadas (Delegación, Force Routing, Skipeo, Facetas, SLA Vivo, Ghost Deletion, Degradación BPMN)
> - **Kanban completo** con Drag & Drop, Bloqueo y Formulario Genérico
> - **2 navegadores simultáneos** para concurrencia y WebSocket
> - Autoguardado con **cierre real** del navegador
> - Panel de métricas reactivo (antes/después)
> - Inactividad + auto-refresco (CA-31)
> - CQRS con estado ❌ FALLA (US-017)
> - **7 escenarios negativos** (vs 3 del v1)

---

## Precondiciones

| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-01 | **J-02 ejecutado previamente** — 4 instancias del proceso de siniestros creadas y con tareas activas | Verificar ≥4 tareas en colas del Workdesk |
| PRE-02 | Docker Compose E2E activo (PG + Redis + Camunda + RabbitMQ) | `docker-compose.e2e.yml` running |
| PRE-03 | 4 usuarios autenticados con roles distintos: | JWT válidos |
| | — `analista_n1` → grupo `Adjusters`, rol `ROLE_OPERARIO` | |
| | — `perito_a` → grupo `Adjusters`, rol `ROLE_OPERARIO` | |
| | — `perito_b` → grupo `Adjusters`, rol `ROLE_OPERARIO` | |
| | — `director_1` → grupo `Directors`, rol `ROLE_SUPERVISOR` | |
| PRE-04 | Relación de delegación configurada: `director_1` es jefe de `analista_n1` | `delegatedAssistantId` configurado |
| PRE-05 | Feature Toggle `forceRouting` disponible y desactivado por defecto | Admin Console |
| PRE-06 | WebSocket STOMP activo en backend | `/ws/workdesk` endpoint |
| PRE-07 | Mock Workers registrados para `reserve-funds` y `rollback-funds` | Simulador Admin |
| PRE-08 | Kanban Board con ≥3 tareas en estado TODO | Seed data fixtures |
| PRE-09 | **2 navegadores** preparados: Chrome (usuario principal) + Incógnito/Edge (usuario secundario) | Multi-sesión |

---

## Inventario de Capacidades Workdesk a Validar

| # | Capacidad | CA | Escenario(s) |
|---|-----------|-----|-------------|
| W-1 | Delegación de Escritorio | CA-04/15 | CU-J04-20 a 22 |
| W-2 | Enrutamiento Forzoso | CA-08/16 | CU-J04-23 a 24 |
| W-3 | Skipeo Justificado | CA-21 | CU-J04-25 a 28 |
| W-4 | Filtros Facetados | CA-22/29 | CU-J04-05 |
| W-5 | Semáforo SLA Vivo | CA-05/11/24 | CU-J04-04 |
| W-6 | Ghost Deletion (WebSocket) | CA-06/13 | CU-J04-15 |
| W-7 | Degradación BPMN | CA-07/18 | CU-J04-35 a 37 |

---

## FASE 1: BANDEJA UNIFICADA — Vista del Analista N1

> **Objetivo:** Validar que el Analista ve sus tareas con SLA, filtros facetados, búsqueda, y panel de métricas.  
> **US:** US-001, US-043

---

### CU-J04-01: Analista N1 accede al Workdesk

**US:** US-001 | **CAs:** CA-01, CA-03

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista N1 | Navega a `/workdesk` | Pantalla P1: "Bandeja Unificada Workdesk" |
| 2 | Sistema | Renderiza DataGrid con 5+1 columnas | Nombre + SLA + Estado + Avance + Recurso + Acciones |
| 3 | Sistema | Carga tareas del grupo `Adjusters` | ≥1 tarea: "Auditar Información Siniestro" del proceso J-02 |
| 4 | Sistema | Toggle BPMN/KANBAN disponible | Filtro tipo: "Todos los Tipos", "Procesos (BPMN)", "Proyectos (Kanban)" |

**Estado esperado:** ✅ PASA  
**Criterio:** El Workdesk carga en ≤2s con las tareas del proceso de siniestros de J-02.

---

### CU-J04-02: Analista ve panel de métricas (ANTES de trabajar)

**US:** US-001 | **CA:** CA-05

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Verifica panel lateral derecho (25%) visible | Panel "Resumen Operativo" abierto |
| 2 | Sistema | Muestra: Total Tareas = N (≥1) | Número coherente con la grilla |
| 3 | Sistema | Muestra: Vencidas = X (badge pulsante si >0) | Badge rojo animado si hay SLA vencidos |
| 4 | Sistema | Muestra: Por Expirar = Y (< 24h) | Número coherente |
| 5 | Sistema | CQRS Engine: "🟢 ONLINE" o "🔴 OFFLINE" | WebSocket STOMP status visible |
| 6 | Analista | **Anota valores:** Total=___, Vencidas=___, Por Expirar=___ | Baseline para comparación post-trabajo |

**Estado esperado:** ✅ PASA

---

### CU-J04-03: Analista verifica ordenamiento por urgencia SLA

**US:** US-001 | **CAs:** CA-01, CA-05

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Observa columna SLA del DataGrid | Tareas ordenadas por urgencia: ⚫ vencidas primero → ⚡ críticas → ⏳ warning → ✔️ OK |
| 2 | Sistema | Cada fila muestra pill SLA con icono accesible (CA-11) | Colores + iconos: ⚫ gris, ⚡ rojo, ⏳ amarillo, ✔️ verde |
| 3 | Sistema | Formato de tiempo relativo | "Vencido hace 3 hrs", "Vence en 12 hrs", "Vence en 2 días" |

**Estado esperado:** ✅ PASA

---

### CU-J04-04: Semáforo SLA Vivo — 4 niveles simultáneos

**US:** US-043 | **CAs:** CA-05, CA-11, CA-24

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tasas con SLAs variados de J-02: | — |
| | | — Tarea con >50% tiempo restante | ✔️ Verde (`bg-emerald-50`) |
| | | — Tarea con 15-50% tiempo restante | ⏳ Amarillo (`bg-yellow-50`) |
| | | — Tarea con <15% tiempo restante | ⚡ Rojo (`bg-red-50`) |
| | | — Tarea con SLA ya vencido | ⚫ Gris (`bg-gray-200`) |
| 2 | Sistema | Heartbeat reactivo (timeStore) actualiza cada segundo | El contador "Vence en X hrs" cambia en tiempo real sin refresh |
| 3 | Analista | Espera 30 segundos observando una tarea ⏳ | El número de horas/minutos restantes disminuye visiblemente |
| 4 | Sistema | Badge "⚠️ SLA en Riesgo" en tareas con `isSlaAtRisk=true` | Badge amber visible en la fila correspondiente |

**Estado esperado:** ✅ PASA  
**Criterio:** El semáforo es vivo (actualización reactiva sin refresh manual) con 4 niveles visualmente distinguibles.

---

### CU-J04-05: Filtros facetados y búsqueda con debounce

**US:** US-001 | **CAs:** CA-22, CA-29

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Observa chips de facetas bajo el header | Chips con contadores: "CREATED (2)", "IN_PROGRESS (1)", etc. |
| 2 | Analista | Hace clic en faceta "CREATED" | Grilla filtra solo tareas en estado CREATED. Chip se ilumina indigo |
| 3 | Analista | Hace clic en "✕ Limpiar" | Filtro se resetea, todas las tareas visibles |
| 4 | Analista | Selecciona filtro tipo = "Procesos (BPMN)" | Solo tareas de procesos Camunda visibles (no Kanban) |
| 5 | Analista | Selecciona filtro SLA = "Vencido" | Solo tareas con SLA expirado visibles |
| 6 | Analista | En buscador, escribe "Auditar" | Debounce 500ms → grilla filtra mostrando solo "Auditar Información Siniestro" |
| 7 | Analista | Borra y escribe "XYZNOEXISTE" | Empty state gamificado: "🎉 ¡Bandeja Vacía!" con confetti icon |
| 8 | Analista | Borra búsqueda | Todas las tareas reaparecen |

**Estado esperado:** ✅ PASA

---

## FASE 2: CLAIM + EJECUCIÓN DE TAREA (Analista N1)

> **Objetivo:** El Analista reclama, llena el iForm Maestro, prueba autoguardado con cierre real, y completa la tarea.  
> **US:** US-002, US-029

---

### CU-J04-06: Analista reclama tarea de la cola grupal

**US:** US-002 | **CAs:** CA-01, CA-12

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Identifica "Auditar Información Siniestro" en la grilla | Badge "Sin Asignar" y badge `Adjusters` visible (CA-10) |
| 2 | Analista | Hace clic en botón "Atender" | POST `/api/v1/tasks/{taskId}/claim` → 200 OK |
| 3 | Sistema | Toast: "Tarea atendida con éxito" (green) | Toast slide-in animado |
| 4 | Sistema | Redirige a vista de formulario | `/workdesk/tasks/{taskId}` o FormDesigner mock |

**Estado esperado:** ✅ PASA

---

### CU-J04-07: Analista abre el iForm Maestro "Auditoría de Siniestro"

**US:** US-029 | **CAs:** CA-05, CA-10

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema BFF | Carga Mega-DTO | GET `/api/v1/tasks/{taskId}/form-data` retorna formulario + datos previos |
| 2 | Sistema | Renderiza "Auditoría de Siniestro" (FORM-01 de J-02) | 16 componentes: Stage INTAKE visible |
| 3 | Analista | Ve datos pre-llenados (si hay variables de inicio) | Campos con valores del Start Event |
| 4 | Sistema | Panel lateral con contexto: proceso, instancia, historial | Info contextual visible |
| 5 | Sistema | Carga en ≤2 segundos | NFR-PER-02 cumplido |

**Estado esperado:** ✅ PASA

---

### CU-J04-08: Analista llena formulario parcialmente y prueba autoguardado

**US:** US-029 | **CAs:** CA-03, CA-11

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Llena 5 campos: póliza="CO-98765432", nombre="María UAT", email, fecha, tipo="Robo" | Campos responden |
| 2 | Sistema | Indicador "Borrador guardado ✓" aparece tras ~30s | LocalStorage persistió datos |
| 3 | Analista | **Cierra la pestaña del navegador completamente** | Navegador cerrado |
| 4 | Analista | Reabre el navegador → navega a `/workdesk` → abre la misma tarea | — |
| 5 | Sistema | Banner: "Se encontró un borrador guardado. ¿Restaurar?" | Prompt de restauración |
| 6 | Analista | Confirma restauración | Los 5 campos reaparecen con los datos ingresados previamente |

**Estado esperado:** ✅ PASA  
**Criterio:** Cierre real del navegador NO pierde datos. Amnesia Cero (CA-85) funcional.

---

### CU-J04-09: Analista adjunta archivo de evidencia

**US:** US-029 | **CA:** CA-09

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | En campo "Evidencia Fotográfica" (FORM-01), clic en "Adjuntar" | Diálogo OS |
| 2 | Analista | Selecciona imagen .jpg (3MB) | — |
| 3 | Sistema | Upload-First: sube inmediatamente | Barra de progreso → POST `/api/v1/attachments/upload` |
| 4 | Sistema | Muestra thumbnail con nombre, tamaño, botón "Eliminar" | Archivo visible en la lista |
| 5 | Analista | Adjunta segunda imagen .png (5MB) | 2 archivos visibles (≤5 máx, ≤10MB cada uno) |

**Estado esperado:** ✅ PASA

---

### CU-J04-10: Analista completa la tarea con validación Zod isomórfica

**US:** US-029 | **CAs:** CA-01, CA-02, CA-03

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Completa todos los campos restantes (ANALYSIS + DECISION stages) | Formulario lleno |
| 2 | Analista | Presiona "Completar Auditoría" | — |
| 3 | Sistema (FE) | Validación Zod client-side | OK: todos los campos pasan |
| 4 | Sistema (BE) | Validación Zod isomórfica server-side | POST `/api/v1/tasks/{taskId}/complete` → 200 OK |
| 5 | Motor Camunda | Avanza al Parallel Gateway → crea tareas para peritos + espera mensaje | Flujo BPMN avanza |
| 6 | Sistema | Toast: "Tarea completada exitosamente ✓" | Redirección al Workdesk |

**Estado esperado:** ✅ PASA

---

### CU-J04-11: Confirmación RYOW (Read Your Own Writes)

**US:** US-029 | **CA:** CA-17

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tras completar la tarea, redirige a `/workdesk` | — |
| 2 | Analista | Ve que "Auditar Información Siniestro" **YA NO** está en su bandeja | Tarea desaparece inmediatamente |
| 3 | Sistema | Consistencia RYOW en ≤1s | Sin delay perceptible |

**Estado esperado:** ✅ PASA

---

### CU-J04-12: Panel de métricas reactivo (DESPUÉS de trabajar)

**US:** US-001 | **CA:** CA-05

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Verifica panel lateral derecho | — |
| 2 | Sistema | Total Tareas = N-1 (decrementó vs CU-J04-02) | Ejemplo: era 5, ahora 4 |
| 3 | Sistema | Vencidas y Por Expirar actualizados | Números coherentes con la grilla filtrada |
| 4 | Analista | Confirma: **la diferencia es exactamente -1 tarea** | Métrica reactiva verificada |

**Estado esperado:** ✅ PASA

---

## FASE 3: MULTI-INSTANCE — Perito A + Perito B (2 Navegadores)

> **Objetivo:** 2 peritos trabajan simultáneamente en evaluaciones de daños. Validar Ghost Deletion + Concurrencia.  
> **US:** US-002, US-029

---

### CU-J04-13: Perito A abre sesión en Navegador 1

**US:** US-002

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Perito A | Login en Chrome → navega a `/workdesk` | Workdesk carga con tareas del grupo `Adjusters` |
| 2 | Sistema | Tarea "Evaluar Daños Dinámicamente" visible (instancia Multi-Instance asignada a `perito_a`) | `camunda:assignee="${perito}"` → asignada directamente |
| 3 | Perito A | Reclama su tarea | POST `/tasks/{id}/claim` → 200 |

**Estado esperado:** ✅ PASA

---

### CU-J04-14: Perito B abre sesión en Navegador 2 simultáneamente

**US:** US-002

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Perito B | Login en Incógnito/Edge → navega a `/workdesk` | Workdesk carga con SU instancia MI de evaluación |
| 2 | Sistema | Tarea "Evaluar Daños Dinámicamente" para `perito_b` visible | Instancia MI separada |
| 3 | Perito B | Reclama su tarea | POST `/tasks/{id}/claim` → 200 |

**Estado esperado:** ✅ PASA

---

### CU-J04-15: Ghost Deletion — Perito A ve desaparecer tarea reclamada por otro

**US:** US-002 | **CAs:** CA-06, CA-13

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Setup | Crear una tarea adicional en cola grupal `Adjusters` (sin asignee específico) | Tarea visible para ambos peritos |
| 2 | Perito A | Ve la tarea adicional en su Workdesk (Navegador 1) | Fila visible en grilla |
| 3 | Perito B | Reclama la tarea adicional desde Navegador 2 | POST claim → 200 |
| 4 | Sistema | WebSocket STOMP envía evento al Navegador 1 | — |
| 5 | Perito A | **Sin refrescar:** la tarea desaparece de su grilla con animación slide-out | CSS: `opacity: 0; transform: translateX(-20px)` (CA-13) |
| 6 | Sistema | Clase `.is-ghost` aplicada → pointer-events: none → fila se desvanece | Transición en 0.8s |

**Estado esperado:** ✅ PASA  
**Criterio:** Ghost Deletion funciona en tiempo real vía WebSocket sin refresh manual.

---

### CU-J04-16: Ambos peritos completan sus evaluaciones simultáneamente

**US:** US-029

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Perito A | Abre su tarea → llena iForm "Evaluación de Daños" (FORM-03 de J-02) | GPS + fotos + grid ítems + monto=$120,000 |
| 2 | Perito B | Abre su tarea → llena su iForm (datos distintos) | Monto=$130,000, recomendación distinta |
| 3 | Perito A | Presiona "Enviar Evaluación" | Completa → su instancia MI cerrada |
| 4 | Perito B | Presiona "Enviar Evaluación" | Completa → ambas instancias MI cerradas |
| 5 | Motor | Multi-Instance convergge | Token disponible para Parallel Join |

**Estado esperado:** ✅ PASA

---

### CU-J04-17: Concurrencia atómica — Test Playwright automatizado

**US:** US-002 | **CA:** CA-12

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Ejecuta `us002-atomic-claim-concurrency.spec.ts` | 2 claims simultáneos vía API |
| 2 | Sistema | Solo 1 claim gana (200 OK) | Lock optimista/Redis |
| 3 | Sistema | El otro recibe 409 Conflict | No hay doble asignación |
| 4 | Resultado | Test Playwright green ✅ | Concurrencia validada programáticamente |

**Estado esperado:** ✅ PASA  
**Criterio:** Nunca hay doble asignación bajo ninguna circunstancia (NFR-SEC-05).

---

## FASE 4: DELEGACIÓN DE ESCRITORIO

> **Objetivo:** El Director ve y supervisa el escritorio del Analista N1.  
> **US:** US-001 | **CAs:** CA-04, CA-15

---

### CU-J04-20: Director activa modo Delegación

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Director | Login → navega a `/workdesk` | Su bandeja personal (tareas de `Directors`) |
| 2 | Director | Hace clic en toggle "👤 Tareas de mi Asistente" | Toggle cambia a amber |
| 3 | Sistema | Fetch con `assistantId` del Analista N1 | GET `/api/v1/tasks?delegatedUser=analista_n1` |
| 4 | Sistema | Banner amber: "Estás viendo el escritorio de **analista_n1**" | Banner con botón "Volver a mis tareas" |
| 5 | Director | Ve las tareas del Analista N1 (no las suyas propias) | Grilla muestra tareas del grupo `Adjusters` |

**Estado esperado:** ✅ PASA

---

### CU-J04-21: Director puede operar tareas delegadas

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Director | En modo delegación, ve una tarea pendiente del Analista | Botón "Atender" visible |
| 2 | Director | Hace clic en una tarea para ver detalles | Modal de tarea se abre |
| 3 | Director | Puede ver el contexto completo (formulario, historial, SLA) | Información visible en modo lectura o acción según permisos |

**Estado esperado:** ✅ PASA

---

### CU-J04-22: Director regresa a su escritorio propio

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Director | Hace clic en "↩️ Volver a mis tareas" en el banner | — |
| 2 | Sistema | Banner amber desaparece (Transition slide-up) | Toggle regresa a "📋 Mis Tareas" (indigo) |
| 3 | Director | Ve SUS tareas de nuevo (grupo `Directors`) | Tarea "Firma Final Director" visible (del Sub-Process de J-02) |

**Estado esperado:** ✅ PASA

---

## FASE 5: ENRUTAMIENTO FORZOSO

> **Objetivo:** Un administrador activa el feature toggle que fuerza al operario a atender la tarea más urgente.  
> **US:** US-001 | **CAs:** CA-08, CA-16

---

### CU-J04-23: Admin activa Force Routing

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin | En consola Admin, activa Feature Toggle `forceRouting = true` | Toggle guardado |
| 2 | Analista N1 | Navega a `/workdesk` | — |
| 3 | Sistema | `store.checkForceRouting()` detecta toggle activo | `forceRoutingEnabled = true` |
| 4 | Sistema | **Grilla OCULTA.** Pantalla de enrutamiento forzoso visible | Icono ⚡ bolt, texto "Modo Enrutamiento Forzoso", botón "Atender Siguiente Tarea" |
| 5 | Analista | NO puede elegir qué tarea atender | Solo ve el botón "🚀 Atender Siguiente Tarea" |

**Estado esperado:** ✅ PASA

---

### CU-J04-24: Analista atiende tarea forzada

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Presiona "🚀 Atender Siguiente Tarea" | Spinner "Asignando..." |
| 2 | Sistema | `store.attendNext()` → asigna tarea más urgente (SLA más crítico) | Claim atómico |
| 3 | Sistema | Toast: "¡Tarea Asignada Atómicamente!" | Modal de tarea se abre automáticamente |
| 4 | Analista | Ve el formulario de la tarea asignada | Formulario funcional |
| 5 | Admin | Desactiva toggle `forceRouting = false` | — |
| 6 | Analista | Recarga `/workdesk` | Grilla normal visible de nuevo |

**Estado esperado:** ✅ PASA  
**Criterio:** El operario NO tiene control de selección cuando Force Routing está activo.

---

## FASE 6: SKIPEO JUSTIFICADO (4 Motivos)

> **Objetivo:** Validar los 4 motivos de skipeo con su audit trail inmutable.  
> **US:** US-002 | **CA:** CA-21

---

### CU-J04-25: Skipeo — Motivo 1: "Cliente no responde"

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Abre una tarea → ve formulario → presiona "⏭ Skipeo Justificado" | Modal amber se abre |
| 2 | Sistema | Info banner: "Esta acción quedará inmutablemente registrada en el Audit Log" | Warning visible |
| 3 | Analista | Selecciona: "El cliente no responde / No está disponible" | Motivo seleccionado |
| 4 | Analista | Presiona "Confirmar Salto" | `store.skipAndNext()` ejecuta |
| 5 | Sistema | Skipeo registrado + nueva tarea asignada automáticamente | Toast: "Skipeo registrado. Nueva Tarea Asignada." |
| 6 | Sistema | Modal se cierra, nueva tarea abierta en el viewer | Flujo continuo |

**Estado esperado:** ✅ PASA

---

### CU-J04-26: Skipeo — Motivo 2: "Requiere documentación adicional"

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Abre tarea → Skipeo → selecciona "Requiere documentación adicional externa" | — |
| 2 | Analista | Confirma | Skipeo registrado |
| 3 | Sistema | Audit trail: `{ reason: "REQUIERE_DOCUMENTACION", user: "analista_n1", timestamp: ... }` | Inmutable |

**Estado esperado:** ✅ PASA

---

### CU-J04-27: Skipeo — Motivo 3: "Fuera de mi área"

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Skipeo → selecciona "Fuera de mi área de especialidad" | — |
| 2 | Analista | Confirma | Skipeo registrado + nueva tarea |

**Estado esperado:** ✅ PASA

---

### CU-J04-28: Skipeo — Motivo 4: "Otro" con validación de detalle

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Skipeo → selecciona "Otro (Especificar)" | Campo textarea aparece |
| 2 | Analista | Escribe "abc" (3 chars) | Error: "El detalle debe tener al menos 10 caracteres" |
| 3 | Sistema | Botón "Confirmar Salto" **deshabilitado** | `isSkipFormInvalid = true` |
| 4 | Analista | Escribe "El sistema ERP externo está en mantenimiento programado" (52 chars) | Error desaparece |
| 5 | Analista | Presiona "Confirmar Salto" | Skipeo registrado con detalle libre |

**Estado esperado:** ✅ PASA  
**Criterio:** Motivo "Otro" exige ≥10 caracteres obligatorios. Sin detalle → botón bloqueado.

---

## FASE 7: KANBAN BOARD

> **Objetivo:** Validar Drag & Drop completo, Bloqueo con motivo, y Formulario Genérico en tarea Kanban.  
> **US:** US-008, US-039

---

### CU-J04-29: Operario navega al tablero Kanban

**US:** US-008

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | En Workdesk, filtra tipo = "Proyectos (Kanban)" | Solo tareas Kanban visibles |
| 2 | Analista | Navega al tablero Kanban interactivo | Pantalla con columnas drag & drop |
| 3 | Sistema | Columnas con ≥3 tareas en TODO | Tareas precargadas del seed data |

**Estado esperado:** ✅ PASA

---

### CU-J04-30: Kanban — Flujo completo de estados con bloqueo

**US:** US-008

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Arrastra tarea de TODO → IN_PROGRESS | `kanbanStore.moveTask(id, 'IN_PROGRESS')` → sync OK |
| 2 | Sistema | Sync status: "Guardando..." → "OK" | Animación de sincronización |
| 3 | Analista | Arrastra tarea de IN_PROGRESS → BLOCKED | Modal de bloqueo se abre |
| 4 | Sistema | "Por favor, especifica el Motivo de Bloqueo" | Textarea obligatorio |
| 5 | Analista | Escribe: "Esperando respuesta del perito externo" | Motivo válido |
| 6 | Analista | Presiona "Bloquear" | `moveTask(id, 'BLOCKED', reason)` → tarea en columna BLOCKED |
| 7 | Analista | Arrastra de BLOCKED → IN_PROGRESS | Desbloqueo, tarea regresa al flujo |
| 8 | Analista | Arrastra de IN_PROGRESS → DONE | Tarea completada |

**Estado esperado:** ✅ PASA  
**Criterio:** Secuencia completa: TODO → IN_PROGRESS → BLOCKED (motivo) → IN_PROGRESS → DONE.

---

### CU-J04-31: Kanban — Happy path directo

**US:** US-008

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Arrastra otra tarea: TODO → IN_PROGRESS → DONE | 2 movimientos |
| 2 | Sistema | Cada movimiento sincroniza con API | "Guardando" → "OK" |

**Estado esperado:** ✅ PASA

---

### CU-J04-32: Formulario Genérico en tarea Kanban (sys_generic_form)

**US:** US-039 | **CAs:** CA-01, CA-02, CA-03

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Abre una tarea Kanban que no tiene formulario diseñado | — |
| 2 | Sistema | Carga `sys_generic_form` automáticamente | MetadataGrid + Resultado + Observaciones + Adjuntos |
| 3 | Analista | Resultado="Aprobar", Observaciones="Tarea manual completada satisfactoriamente" (≥10 chars) | Validación Zod OK |
| 4 | Analista | Presiona "✅ Aprobar" en PanicButtonBar | POST complete |
| 5 | Sistema | Tarea completada en Kanban | Estado actualizado |

**Estado esperado:** ✅ PASA

---

## FASE 8: DEGRADACIÓN BPMN

> **Objetivo:** Simular caída de Camunda y verificar resiliencia del Workdesk.  
> **US:** US-001 | **CAs:** CA-07, CA-18

---

### CU-J04-35: Detener Camunda — Activar degradación

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin | `docker stop camunda-container` | Camunda offline |
| 2 | Analista | Navega a `/workdesk` o refresca | — |
| 3 | Sistema | Banner amber: "⚠️ Sincronización BPMN degradada temporalmente" | Banner con icono pulsante |
| 4 | Sistema | Subtexto: "Las tareas de procesos automatizados podrían no estar actualizadas. Las tareas Kanban operan con normalidad." | Mensajería clara |
| 5 | Sistema | CQRS Engine → "🔴 OFFLINE" en panel métricas | Estado degradado visible |

**Estado esperado:** ✅ PASA

---

### CU-J04-36: Kanban sigue operando durante degradación

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Filtra tipo = "Proyectos (Kanban)" | Tareas Kanban visibles |
| 2 | Analista | Arrastra tarea en Kanban: TODO → IN_PROGRESS | — |
| 3 | Sistema | Movimiento exitoso (Kanban usa API interna, no Camunda) | Sync OK |
| 4 | Criterio | **Kanban NO se ve afectado** por la caída de Camunda | Resiliencia confirmada |

**Estado esperado:** ✅ PASA

---

### CU-J04-37: Reiniciar Camunda — Banner desaparece

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin | `docker start camunda-container` | Camunda reinicia |
| 2 | Sistema | Reconecta WebSocket / health check | — |
| 3 | Analista | Refresca `/workdesk` o espera auto-refresco | Banner amber desaparece |
| 4 | Sistema | Tareas BPMN reaparecen en la grilla | Sincronización restaurada |
| 5 | Sistema | CQRS Engine → "🟢 ONLINE" | Estado recuperado |

**Estado esperado:** ✅ PASA  
**Criterio:** La degradación es transparente y transitoria. No hay pérdida de datos.

---

## FASE 9: INACTIVIDAD + AUTO-REFRESCO

> **Objetivo:** Validar el auto-refresco silencioso al regresar de inactividad (CA-31).  
> **US:** US-001 | **CA:** CA-31

---

### CU-J04-38: Inactividad de 5+ minutos con auto-refresco

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | Deja el Workdesk abierto en la pestaña | — |
| 2 | Analista | Cambia a otra pestaña (Gmail, Slack, etc.) | `document.visibilityState = 'hidden'` |
| 3 | Sistema | Cuenta inactividad vía `timeStore.getInactivityMs()` | — |
| 4 | Analista | Regresa a la pestaña del Workdesk después de 5+ minutos | `visibilityState = 'visible'` |
| 5 | Sistema | Detecta inactividad > `INACTIVITY_THRESHOLD_MS` (5 min) | — |
| 6 | Sistema | Ejecuta `loadData()` silenciosamente | Refresco automático de la grilla |
| 7 | Analista | Ve datos actualizados sin haber presionado nada | Nuevas tareas o cambios de SLA reflejados |

**Estado esperado:** ✅ PASA  
**Criterio:** El operario siempre ve datos frescos al regresar de una pausa, sin acción manual.

---

## FASE 10: DIRECTOR — FIRMA FINAL (Sub-Process)

> **Objetivo:** El Director completa su tarea del Sub-Process (Firma Final) desde el Workdesk.  
> **US:** US-029

---

### CU-J04-39: Director reclama y completa Firma Final

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Director | En su Workdesk (modo "Mis Tareas"), ve "Firma Final (Director)" | Tarea del Sub-Process de J-02 |
| 2 | Director | Reclama → abre formulario Simple FORM-04 | 7 componentes, campos prefilled |
| 3 | Sistema | Póliza (disabled) + pagoFinal=$10,000 (disabled, currency) | P-20 prefill + P-15 disabled |
| 4 | Director | Resumen ejecutivo (≥10 chars) + Decisión="Aprobar Liquidación" + Firma Digital | CA-31 canvas firma |
| 5 | Director | Presiona "Firmar y Autorizar" | POST complete → Sub-Process cierra |
| 6 | Motor | Avanza a Service Tasks de Finanzas | Mock Workers ejecutan |

**Estado esperado:** ✅ PASA

---

## FASE 11: CQRS EVENT STORE

> **Objetivo:** Verificar intento de persistencia CQRS tras completar tareas.  
> **US:** US-017

---

### CU-J04-40: Sistema intenta persistir evento inmutable

**US:** US-017 | **CA:** CA-01

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tras CU-J04-10 (Analista completa tarea) | — |
| 2 | Sistema (CQRS) | Intenta INSERT en `form_event_store` | `eventId`, `taskId`, `formData`, `timestamp`, `userId` |
| 3 | Sistema | Publica evento a RabbitMQ | Exchange `ibpms.cqrs`, routing `form.completed` |

> **Detalle completo:** Ver [J-08](./casos_uso_uat_j08.md)

**Estado esperado:** ❌ FALLA — US-017 no implementada (tabla `form_event_store` no existe)  
**Brecha:** B-16, B-17

---

## FASE 12: OBSERVABILIDAD

> **Objetivo:** Verificar trazabilidad de las acciones del operario.  
> **US:** US-001, US-005

---

### CU-J04-41: Historial del motor muestra tareas completadas

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista | GET `/api/v1/engine-rest/history/task` | Tareas completadas por el operario con timestamps |
| 2 | Sistema | Incluye: claim time, complete time, assignee, formKey | Trazabilidad completa |

**Estado esperado:** ✅ PASA

---

### CU-J04-42: Audit trail de skipeos verificable

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin | Consulta registros de skipeo vía API o BD | 4 registros de CU-J04-25 a 28 |
| 2 | Sistema | Cada registro incluye: userId, taskId, reason, detail, timestamp | Inmutable (NFR-OBS-01) |

**Estado esperado:** ✅ PASA

---

## Escenarios Negativos

---

### CU-J04-NEG-01: Envío de formulario con campos obligatorios vacíos

**US:** US-029

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Analista deja campos required vacíos y presiona "Enviar" |
| 2 | Validación Zod client-side bloquea el envío |
| 3 | Campos con borde rojo + mensaje descriptivo |
| 4 | Formulario NO se envía hasta corregir |

**Estado esperado:** ✅ PASA

---

### CU-J04-NEG-02: Timeout de red al enviar formulario

**US:** US-029

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario presiona "Enviar" pero la red falla |
| 2 | After 30s timeout: "No se pudo enviar. Intente de nuevo." |
| 3 | Borrador permanece en LocalStorage |
| 4 | Operario puede reintentar sin perder datos |

**Estado esperado:** ✅ PASA

---

### CU-J04-NEG-03: Upload de archivo que excede límite

**US:** US-029

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario intenta adjuntar archivo >50MB |
| 2 | Validación client-side bloquea el upload |
| 3 | Mensaje: "El archivo excede el límite" |

**Estado esperado:** ✅ PASA

---

### CU-J04-NEG-04: Delegación IDOR — Operario sin relación jerárquica

**US:** US-001 | **CAs:** CA-04, CA-15

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Perito A intenta activar modo delegación hacia `director_1` |
| 2 | Backend valida jerarquía: Perito NO es jefe de Director |
| 3 | HTTP 403 Forbidden |
| 4 | Sistema revierte a modo "Mis Tareas" |
| 5 | Alert: "No tiene permisos para ver el escritorio de este usuario" |

**Estado esperado:** ✅ PASA

---

### CU-J04-NEG-05: Skipeo sin motivo seleccionado

**US:** US-002 | **CA:** CA-21

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Analista abre modal de skipeo |
| 2 | No selecciona motivo (select vacío "") |
| 3 | Botón "Confirmar Salto" **deshabilitado** (`isSkipFormInvalid = true`) |
| 4 | No se puede confirmar el skipeo |

**Estado esperado:** ✅ PASA

---

### CU-J04-NEG-06: Kanban bloqueo sin motivo

**US:** US-008

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario arrastra tarea a columna BLOCKED |
| 2 | Modal de bloqueo se abre con textarea vacío |
| 3 | Botón "Bloquear" **deshabilitado** (`:disabled="!blockReasonInput.trim()"`) |
| 4 | No se puede bloquear sin motivo |

**Estado esperado:** ✅ PASA

---

### CU-J04-NEG-07: Operario sin rol accede a `/workdesk`

**US:** US-001 | **CAs:** US-036, US-051

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Usuario sin `ROLE_OPERARIO` ni `ROLE_SUPERVISOR` navega a `/workdesk` |
| 2 | Router Guard evalúa permisos del JWT |
| 3 | **NO muestra 403** (Gaslighting anti-enumeración) |
| 4 | Muestra **404 genérico**: "Página no encontrada" |
| 5 | No se revela la existencia de la ruta protegida |

**Estado esperado:** ✅ PASA

---

## Brechas Descubiertas en esta Certificación

| # | Brecha | Severidad | US | Sprint |
|---|--------|:---------:|:--:|:------:|
| B-J04-01 | `form_event_store` no existe → CQRS FALLA | 🔴 P0 | US-017 | 6.2+ |
| B-J04-02 | Viewer de tarea es mock (dashed border, no renderiza formulario real) | 🟠 P1 | US-029 | 6.1 |
| B-J04-03 | Delegación usa `assistantId` hardcoded placeholder (`101edfe`) | 🟠 P1 | US-001 | 6.2 |
| B-J04-04 | `forceRouting` toggle falta endpoint real en Admin API | 🟡 P2 | US-001 | 6.2 |
| B-J04-05 | WebSocket STOMP para Ghost Deletion necesita validación E2E | 🟡 P2 | US-002 | 6.1 |
| B-J04-06 | Kanban `moveTask` falta validación de transiciones de estado válidas | 🟡 P2 | US-008 | 6.2 |
| B-J04-07 | Skipeo `skipAndNext` falta endpoint backend real | 🟠 P1 | US-002 | 6.1 |

---

## Matriz de Trazabilidad

| Escenario | Fase | US | CAs | Capacidad | Prioridad | Estado |
|-----------|:----:|:--:|:---:|:---------:|:---------:|:------:|
| CU-J04-01 | Bandeja | US-001 | CA-01,03 | — | MUST | ✅ |
| CU-J04-02 | Bandeja | US-001 | CA-05 | Métricas | MUST | ✅ |
| CU-J04-03 | Bandeja | US-001 | CA-01,05 | SLA | MUST | ✅ |
| CU-J04-04 | Bandeja | US-043 | CA-05,11,24 | W-5 | MUST | ✅ |
| CU-J04-05 | Bandeja | US-001 | CA-22,29 | W-4 | MUST | ✅ |
| CU-J04-06 | Claim | US-002 | CA-01,12 | — | MUST | ✅ |
| CU-J04-07 | Ejecución | US-029 | CA-05,10 | — | MUST | ✅ |
| CU-J04-08 | Ejecución | US-029 | CA-03,11 | Autoguardado | MUST | ✅ |
| CU-J04-09 | Ejecución | US-029 | CA-09 | Upload | MUST | ✅ |
| CU-J04-10 | Ejecución | US-029 | CA-01,02,03 | Zod | MUST | ✅ |
| CU-J04-11 | Ejecución | US-029 | CA-17 | RYOW | MUST | ✅ |
| CU-J04-12 | Bandeja | US-001 | CA-05 | Métricas | MUST | ✅ |
| CU-J04-13 | Multi-Inst | US-002 | — | — | MUST | ✅ |
| CU-J04-14 | Multi-Inst | US-002 | — | — | MUST | ✅ |
| CU-J04-15 | Multi-Inst | US-002 | CA-06,13 | W-6 | MUST | ✅ |
| CU-J04-16 | Multi-Inst | US-029 | — | — | MUST | ✅ |
| CU-J04-17 | Concurrencia | US-002 | CA-12 | Playwright | MUST | ✅ |
| CU-J04-20 | Delegación | US-001 | CA-04,15 | W-1 | MUST | ✅ |
| CU-J04-21 | Delegación | US-001 | CA-04 | W-1 | SHOULD | ✅ |
| CU-J04-22 | Delegación | US-001 | CA-04,15 | W-1 | MUST | ✅ |
| CU-J04-23 | Force Route | US-001 | CA-08,16 | W-2 | MUST | ✅ |
| CU-J04-24 | Force Route | US-001 | CA-08 | W-2 | MUST | ✅ |
| CU-J04-25 | Skipeo | US-002 | CA-21 | W-3 | MUST | ✅ |
| CU-J04-26 | Skipeo | US-002 | CA-21 | W-3 | MUST | ✅ |
| CU-J04-27 | Skipeo | US-002 | CA-21 | W-3 | MUST | ✅ |
| CU-J04-28 | Skipeo | US-002 | CA-21 | W-3 | MUST | ✅ |
| CU-J04-29 | Kanban | US-008 | — | — | MUST | ✅ |
| CU-J04-30 | Kanban | US-008 | — | D&D+Block | MUST | ✅ |
| CU-J04-31 | Kanban | US-008 | — | D&D | MUST | ✅ |
| CU-J04-32 | Kanban | US-039 | CA-01,02,03 | GenForm | MUST | ✅ |
| CU-J04-35 | Degradación | US-001 | CA-07,18 | W-7 | MUST | ✅ |
| CU-J04-36 | Degradación | US-001 | CA-07 | W-7 | MUST | ✅ |
| CU-J04-37 | Degradación | US-001 | CA-07,18 | W-7 | MUST | ✅ |
| CU-J04-38 | Inactividad | US-001 | CA-31 | Auto-refresh | SHOULD | ✅ |
| CU-J04-39 | Director | US-029 | CA-31 | Firma | MUST | ✅ |
| CU-J04-40 | CQRS | US-017 | CA-01 | EventStore | MUST | ❌ |
| CU-J04-41 | Observabilidad | US-001 | CA-42 | History | MUST | ✅ |
| CU-J04-42 | Observabilidad | US-002 | CA-21 | Skip Audit | MUST | ✅ |
| CU-J04-NEG-01 | Negativo | US-029 | CA-02 | Zod | MUST | ✅ |
| CU-J04-NEG-02 | Negativo | US-029 | CA-03 | Resiliencia | SHOULD | ✅ |
| CU-J04-NEG-03 | Negativo | US-029 | CA-09 | Upload | SHOULD | ✅ |
| CU-J04-NEG-04 | Negativo | US-001 | CA-04,15 | IDOR | MUST | ✅ |
| CU-J04-NEG-05 | Negativo | US-002 | CA-21 | Skip | MUST | ✅ |
| CU-J04-NEG-06 | Negativo | US-008 | — | Kanban | MUST | ✅ |
| CU-J04-NEG-07 | Negativo | US-001 | US-036,051 | RBAC | MUST | ✅ |

**Total: 45 escenarios UAT** (38 positivos + 7 negativos)  
**Cobertura: 7 capacidades Workdesk** + Kanban completo + Multi-instance + CQRS  
**Usuarios: 4** (Analista N1, Perito A, Perito B, Director)  
**Navegadores: 2** simultáneos para concurrencia y WebSocket

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-13 | Creación inicial: 13 escenarios UAT (v1) | Agente PO + Arquitecto Lead |
| 2026-04-19 | **v2: Certificación E2E Operario.** Reutiliza instancias J-02. 4 usuarios. 7 capacidades Workdesk (Delegación, Force Routing, Skipeo×4, Facetas, SLA Vivo 4 niveles, Ghost Deletion WS, Degradación BPMN). Kanban full (D&D + Block + GenForm). 2 navegadores simultáneos. Autoguardado con cierre real. Panel métricas antes/después. Inactividad 5min. Concurrencia Playwright. CQRS ❌. 45 escenarios. 7 brechas. 7 negativos. | Agente PO + Antigravity |
