# Casos de Uso UAT — US-002: Task Claiming Completo

> **US:** US-002 — Reclamar una Tarea de Grupo (Claim Task)
> **Actor principal:** Operario / Analista / Supervisor
> **Criticidad:** 🔴 ALTA
> **Épica:** Motor Core BPMN & Workdesk (Épica A)
> **Fecha:** 2026-04-13
> **Autor:** Agente PO

---
## Precondiciones
| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-1 | Tarea sin asignar disponible en la Cola del Equipo | Vista Tab "Cola del Equipo" |
| PRE-2 | Operario autenticado y validado RBAC | JWT local y Backend OK |
| PRE-3 | Motor PostgreSQL y Camunda operativos o soportados por Offline Local Sync | Dependencia CA-10 / CA-21 |

---
## Escenarios UAT

### CU-US002-01: Reclamo Simultáneo y Atomicidad
**CA Mapeado:** CA-01, CA-11, CA-14
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A y B | Visualizan TK-099 en la Cola de su Grupo | — |
| 2 | Operario A y B | Pulsa el botón [Reclamar] en la misma fracción de segundo | Inicia petición `POST /tasks/TK-099/claim` |
| 3 | Backend | Aplica bloqueo pesimista en PGSQL o Camunda | Uno es inscrito; el hilo 2 es rechazado |
| 4 | Sistema (Op A) | Recibe confirmación exitosa y mueve tarea a Tab "Mi Bandeja" (CA-22) | — |
| 5 | Sistema (Op B) | Recibe `HTTP 409 Conflict` | Muestra Modal "Lo sentimos, Operario A se te adelantó" |
**Criterio de aceptación:** Ninguna tarea es secuestrada dos veces, garantizando data-integrity estricta en Base de Datos.

### CU-US002-02: Reclamo Masivo en Lote y Emisión Agregada (Bulk)
**CA Mapeado:** CA-02, CA-14, CA-23
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Entra a Tab "Cola del Equipo" y marca 10 casillas de tareas | — |
| 2 | Operario | Pulsa el botón flotante superior [Reclamar Seleccionadas] | Lanza `POST /api/v1/tasks/bulk-claim` |
| 3 | Backend | Ejecuta el commit del array, detectando que 1 ya fue tomada | Responde `{ claimed: 9, conflicts: 1 }` |
| 4 | Backend | Emite UN SOLO mensaje WebSocket tipo `BULK_REMOVE` | payload: `{ taskIds: [...las 9 exitosas] }` |
| 5 | UI Grupo | Desvanece las 9 tareas una tras otra de forma escalonada | Evita saltos de renderización confusos |
**Criterio de aceptación:** La red se oxigena limitando la cantidad de WebSockets emitidos por Bulk Claims protegiendo el throughput.

### CU-US002-03: Liberación Orgánica con Purgado Transitorio
**CA Mapeado:** CA-04, CA-07, CA-16, CA-17
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Posee una tarea bloqueada; decide liberarla usando botón [Liberar Tarea] | Despliega modal de confirmación |
| 2 | Operario | Rellena (opcional): "@Jefe, el cliente solicita un refrendo de usted" | — |
| 3 | Sistema | Ejecuta Amnesia Transaccional antes de purgar al assignee | Elimina el FormData parcial del LocalStorage |
| 4 | Backend | Marca archvos adjuntos de esta sesión huérfana como transitorios (`orphaned`) | Se autodestruyen en 24h cron job |
| 5 | Nuevo Op. | Reclama esa Tarea posteriormente | Aparece limpia; recibe Banner con la Nota Interna preservada (CA-16) |
**Criterio de aceptación:** Cero contaminación del Payload base en la tabla temporal al liberar. Mensajería interna sin chat.

### CU-US002-04: Exploración Segura y Alarma Cinetica
**CA Mapeado:** CA-05, CA-18
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Haz doble clic sobre TK-102 en la Cola del Equipo sin "Reclamar" | Abre vista Modal/Page de detalle |
| 2 | Sistema | Presenta modo de Sólo-Lectura de campos de UI | Prohíbe mutación activa |
| 3 | Otro Op. | Reclama agresivamente esa tarea en su computadora | Emite evento WS |
| 4 | Sistema (Lectura)| Detecta evento WS y levanta Banner sobre la vista de exploración | "⚠️ Esta tarea fue reclamada por compañero..." |
| 5 | Sistema (Lectura)| Desactiva el botón [Reclamar] en gris y expulsa al leer | Protegiendo el esfuerzo de completamiento ciego |
**Criterio de aceptación:** Nadie compite por una pantalla leyendo; se alerta inmediatamente sin patear violentamente al lector del Modal.

### CU-US002-05: Prevención Ghost Job (Timeout y Unclaim)
**CA Mapeado:** CA-06, CA-15, CA-19
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Deja olvidada reclamada una tarea difícil (Umbral config: 4 horas) | El Cron Job Backend evalúa última modificación transaccional |
| 2 | Cron Job | Determina que pasaron 3 horas (75% del SLA Inactivo) | — |
| 3 | Sistema | Despliega un Toast y Banner bloqueante al frente del Operario | "Tu tarea TK será devuelta en 1hr. Haz algo." |
| 4 | Operario | Omite el aviso y se va a almorzar | Cumple las 4 horas absolutas |
| 5 | Cron Job | Ejecuta Auto-Unclaim completo | Purga al `assignee`, genera asiento Audit ("AUTO_UNCLAIMED"), dispara WS hacia Cola Grupal (`ADD`) |
**Criterio de aceptación:** Auto-higiene paramétrica de las colas operativas protegiendo las tareas frente a abandono de puesto.

### CU-US002-06: Despojo Forzoso del Supervisor Perimetralmente Validado
**CA Mapeado:** CA-08, CA-13
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Mgr. Finanzas| Desde la Vista Monitor, localiza TK de "María" que está incapacitada | — |
| 2 | Mgr. Finanzas| Trata de "Forced Unclaim" una TK que pertenece al Dpto IT (Diferente Team_ID) | — |
| 3 | Backend | Coteja `team_id` entre Mgr y Tarea desde JWT (RBAC) | Retorna `HTTP 403 Forbidden` directo |
| 4 | Mgr. Finanzas| Intenta "Forced Unclaim" sobre TK válida de "María" de Finanzas | — |
| 5 | Backend | Valida ok; libera la tarea, remueve a María; genera log de auditoría | Crea asiento "FORCE_UNCLAIMED por Nombre Supervisor" |
**Criterio de aceptación:** Despojo quirúrgico validado perimetralmente para prevención Anti-IDOR cruzado por gerentes entrometidos.

---
## Escenarios Negativos

### CU-US002-NEG-01: Auto-Unclaim Fracasa por Corte Masivo DB
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Cron job dispara las Liberaciones Fantasmas (4 horas) |
| 2 | PostgreSQL experimenta partición o deadlocks en este segundo |
| 3 | Cron aborta la transacción pero empuja la ejecución a cola de reintentos |
| 4 | Retrasa el Despojo Ghost sin corromper la auditoria |

### CU-US002-NEG-02: Micro-Corte de Red en Claim Optimista (Fallo Fijo)
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario presiona "Reclamar TK" (CA-10 / Optimistic UI) |
| 2 | Wifi local se corta. Sistema simula que está en "Mi Bandeja" |
| 3 | Comienza backend Backoff exponencial: 2s, 4s, 8s (Total 14s CA-21) |
| 4 | Conexión nunca vuelve. UI dispara Extractor de Engaño |
| 5 | Tarea desaparece de la bandeja personal devolviéndola al limbo |
| 6 | Modal: "No pudimos confirmar tu reclamo. Sigue en cola de equipo." |

---
## Matriz de Trazabilidad
| Escenario | US | CAs Cubiertos | Prioridad |
|-----------|:--:|:------------:|:---------:|
| CU-US002-01 | US-002 | CA-01, CA-11, CA-14, CA-22 | MUST |
| CU-US002-02 | US-002 | CA-02, CA-14, CA-23 | MUST |
| CU-US002-03 | US-002 | CA-04, CA-07, CA-16, CA-17 | MUST |
| CU-US002-04 | US-002 | CA-05, CA-18 | MUST |
| CU-US002-05 | US-002 | CA-06, CA-15, CA-19, CA-20 | MUST |
| CU-US002-06 | US-002 | CA-08, CA-13, CA-20 | MUST |
| CU-US002-NEG-01 | US-002 | CA-06, CA-15 | MUST |
| CU-US002-NEG-02 | US-002 | CA-10, CA-21 | MUST |
