# Casos de Uso UAT — Journey J-04

> **Journey:** Recepción de Tarea → Ejecución de Formulario → Completar → Persistencia CQRS  
> **Actor principal:** Operario / Analista  
> **Criticidad:** 🔴 ALTA — Si esto no funciona, nadie trabaja.  
> **Épicas cruzadas:** Workdesk (É1) → Formularios (É2) → CQRS (É16)  
> **US involucradas:** US-001, US-002, US-029, US-017, US-000  
> **Fecha:** 2026-04-13  
> **Autor:** Agente PO + Arquitecto Lead

---

## Precondiciones

| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-01 | Usuario autenticado con rol `ROLE_OPERARIO` o superior | Token JWT válido en localStorage |
| PRE-02 | Al menos 1 proceso BPMN desplegado en Camunda | GET `/api/v1/engine-rest/process-definition` retorna ≥1 |
| PRE-03 | Al menos 1 tarea activa asignada a la cola del operario | GET `/api/v1/tasks?assignee=null&candidateGroup=operarios` retorna ≥1 |
| PRE-04 | Formulario iForm publicado y asociado al user task | La tarea tiene `formKey` o `formId` configurado |
| PRE-05 | RBAC permite al operario acceder al Workdesk (P1) | US-036 configurado |

---

## Escenarios UAT

### CU-J04-01: Operario ve tareas ordenadas por urgencia SLA

**US:** US-001 (CA-01, CA-05)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Navega a `/workdesk` | Pantalla P1 carga correctamente |
| 2 | Sistema | Renderiza DataGrid de tareas | Lista de tareas visibles con columnas: ID, Proceso, Asunto, SLA, Estado |
| 3 | Operario | Observa la columna SLA | Las tareas están ordenadas por urgencia: 🔴 rojo primero, 🟡 amarillo después, 🟢 verde al final |
| 4 | Sistema | Semáforo SLA muestra colores | 🔴 = SLA vencido, 🟡 = próximo a vencer (<25% restante), 🟢 = dentro de plazo |

**Criterio de aceptación:** El operario puede distinguir visualmente la urgencia de cada tarea.

---

### CU-J04-02: Operario reclama tarea de cola grupal

**US:** US-002 (CA-01, CA-12)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Identifica tarea en cola grupal (sin asignar) | Tarea muestra badge "Sin asignar" o icono de cola |
| 2 | Operario | Hace clic en botón "Reclamar" (o "Claim") | Confirmación visual: la tarea ahora está asignada al operario |
| 3 | Sistema | Actualiza el `assignee` en Camunda | POST `/api/v1/tasks/{taskId}/claim` → 200 OK |
| 4 | Sistema | WebSocket notifica a otros operarios | La tarea desaparece del Workdesk de los compañeros |
| 5 | Operario | Ve la tarea en su bandeja personal | La tarea aparece con badge "Asignada a mí" |

**Criterio de aceptación:** La tarea se reclama atómicamente y otros operarios no pueden reclamarla simultáneamente.

---

### CU-J04-03: Operario abre detalle de tarea y ve el formulario

**US:** US-029 (CA-05, CA-10)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Hace clic en la tarea reclamada | Navega a `/workdesk/tasks/{taskId}` (Pantalla P2) |
| 2 | Sistema | BFF carga Mega-DTO | GET `/api/v1/tasks/{taskId}/form-data` retorna: formulario + datos previos + adjuntos |
| 3 | Sistema | Renderiza formulario iForm | Campos visibles según el `layoutConfig` (text, select, date, grid, etc.) |
| 4 | Operario | Ve datos pre-llenados (si los hay) | Los campos con valores previos aparecen rellenados |
| 5 | Sistema | Muestra info contextual | Panel lateral con: proceso, instancia, historial del caso |

**Criterio de aceptación:** El formulario carga en ≤2 segundos (NFR-PER-02) con todos los campos visibles.

---

### CU-J04-04: Operario llena formulario con autoguardado

**US:** US-029 (CA-03, CA-11)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Comienza a llenar campos del formulario | Campos responden a la entrada |
| 2 | Sistema | Autoguardado cada 30s en LocalStorage | Indicador "Borrador guardado ✓" visible en la UI |
| 3 | Operario | Cierra el navegador accidentalmente | — |
| 4 | Operario | Reabre la página del mismo task | — |
| 5 | Sistema | Restaura borrador desde LocalStorage | Datos aparecen pre-llenados con lo último guardado |
| 6 | Operario | Confirma restauración | Banner: "Se encontró un borrador guardado. ¿Restaurar?" |

**Criterio de aceptación:** Los datos del formulario NO se pierden ante cierre involuntario del navegador.

---

### CU-J04-05: Operario adjunta archivo al formulario

**US:** US-029 (CA-09)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Hace clic en "Adjuntar archivo" | Diálogo de selección de archivo del sistema operativo |
| 2 | Operario | Selecciona un archivo (PDF, ≤50MB) | — |
| 3 | Sistema | Upload-First: sube inmediatamente al storage | Barra de progreso visible, POST `/api/v1/attachments/upload` |
| 4 | Sistema | Muestra thumbnail o nombre del adjunto | Archivo aparece con ícono, nombre, tamaño, y botón "Eliminar" |
| 5 | Operario | (Opcional) Elimina el archivo adjunto | El archivo desaparece de la lista |

**Criterio de aceptación:** Los archivos se suben inmediatamente (patrón Upload-First), sin esperar al envío del formulario.

---

### CU-J04-06: Operario envía formulario completado

**US:** US-029 (CA-01, CA-02)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Presiona "Enviar" / "Completar Tarea" | — |
| 2 | Sistema (Frontend) | Validación Zod (client-side) | Si hay errores: campos con borde rojo + mensajes de error |
| 3 | Sistema | POST `/api/v1/tasks/{taskId}/complete` con payload | Request enviado al backend |
| 4 | Sistema (Backend) | Validación Zod isomórfica (server-side) | Si error 422: respuesta con detalles de validación |
| 5 | Sistema (Backend) | Persiste datos del formulario | INSERT en tabla de respuestas |
| 6 | Sistema | Señaliza a Camunda que la tarea fue completada | `taskService.complete(taskId, variables)` |
| 7 | Sistema | Muestra confirmación al operario | Toast: "Tarea completada exitosamente ✓" |

**Criterio de aceptación:** La validación es idéntica en frontend y backend (schemas Zod isomórficos).

---

### CU-J04-07: El sistema persiste el evento CQRS inmutable

**US:** US-017

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tras completar la tarea (CU-J04-06) | — |
| 2 | Sistema (CQRS) | Crea evento inmutable en `form_event_store` | INSERT con: eventId, taskId, formData, timestamp, userId |
| 3 | Sistema (CQRS) | Actualiza proyección de lectura | La vista materializada refleja el nuevo estado |
| 4 | Sistema | Publica evento a RabbitMQ | Exchange: `ibpms.cqrs`, routing: `form.completed` |
| 5 | Motor Camunda | Recibe señal y avanza al siguiente nodo BPMN | La siguiente tarea se genera (si aplica) |

**Criterio de aceptación:** El evento es inmutable (no puede ser modificado ni eliminado). El flujo BPMN avanza automáticamente.

---

### CU-J04-08: Confirmación RYOW (Read Your Own Writes)

**US:** US-029 (CA-17)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tras completar la tarea | — |
| 2 | Sistema | Redirige al Workdesk (P1) | Navega automáticamente a `/workdesk` |
| 3 | Operario | Ve que la tarea YA NO está en su bandeja | La tarea desaparece de la lista inmediatamente |
| 4 | Sistema | Consistencia RYOW | El operario ve el estado actualizado sin delay perceptible |

**Criterio de aceptación:** El operario confirma visualmente que su acción tuvo efecto inmediato (≤1s de latencia perceptible).

---

### CU-J04-09: Operario sin permisos no puede acceder al Workdesk

**US:** US-001 (depende US-036, US-051)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Usuario sin rol | Intenta navegar a `/workdesk` manualmente | — |
| 2 | Sistema (Router Guard) | Evalúa permisos del JWT | Redirección a página apropiada |
| 3 | Sistema | NO muestra 403 (Gaslighting) | Muestra 404 genérico (US-051: no revelar existencia de la ruta) |

**Criterio de aceptación:** Usuarios sin permisos no pueden inferir la existencia de rutas protegidas.

---

### CU-J04-10: Concurrencia — dos operarios intentan reclamar la misma tarea

**US:** US-002 (CA-12, concurrencia)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Hace clic en "Reclamar" en tarea T-001 | — |
| 2 | Operario B | (Simultáneamente) Hace clic en "Reclamar" en tarea T-001 | — |
| 3 | Sistema | Solo uno gana (lock optimista o distribuido con Redis) | Uno recibe 200 OK, el otro recibe 409 Conflict |
| 4 | Operario perdedor | Ve mensaje de error | Toast: "Esta tarea ya fue reclamada por otro operario" |
| 5 | Sistema (WebSocket) | Actualiza la lista del perdedor | T-001 desaparece de su Workdesk |

**Criterio de aceptación:** No se generan asignaciones dobles bajo ninguna circunstancia (NFR-SEC-05).

---

## Escenarios Negativos

### CU-J04-NEG-01: Envío de formulario con campos obligatorios vacíos

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario deja campos required vacíos y presiona "Enviar" |
| 2 | Validación Zod client-side bloquea el envío |
| 3 | Campos con error muestran borde rojo + mensaje descriptivo |
| 4 | El formulario NO se envía hasta corregir |

### CU-J04-NEG-02: Timeout de red al enviar formulario

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario presiona "Enviar" pero la red falla |
| 2 | After 30s timeout, muestra error: "No se pudo enviar. Intente de nuevo." |
| 3 | El borrador permanece en LocalStorage |
| 4 | Operario puede reintentar sin perder datos |

### CU-J04-NEG-03: Upload de archivo que excede 50MB

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario intenta adjuntar un archivo >50MB |
| 2 | Validación client-side bloquea el upload |
| 3 | Mensaje: "El archivo excede el límite de 50MB" |

---

## Matriz de Trazabilidad

| Escenario | US | CAs Cubiertos | Prioridad |
|-----------|:--:|:------------:|:---------:|
| CU-J04-01 | US-001 | CA-01, CA-05 | MUST |
| CU-J04-02 | US-002 | CA-01, CA-12 | MUST |
| CU-J04-03 | US-029 | CA-05, CA-10 | MUST |
| CU-J04-04 | US-029 | CA-03, CA-11 | MUST |
| CU-J04-05 | US-029 | CA-09 | MUST |
| CU-J04-06 | US-029 | CA-01, CA-02 | MUST |
| CU-J04-07 | US-017 | — | MUST |
| CU-J04-08 | US-029 | CA-17 | MUST |
| CU-J04-09 | US-001 | (US-036/051) | MUST |
| CU-J04-10 | US-002 | CA-12 | MUST |
| CU-J04-NEG-01 | US-029 | CA-02 | MUST |
| CU-J04-NEG-02 | US-029 | CA-03 | SHOULD |
| CU-J04-NEG-03 | US-029 | CA-09 | SHOULD |

**Total: 13 escenarios UAT** (10 positivos + 3 negativos)

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-13 | Creación inicial: 13 escenarios UAT para J-04 | Agente PO + Arquitecto Lead |
