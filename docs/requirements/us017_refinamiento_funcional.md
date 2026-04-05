# Refinamiento Funcional: US-017 — CQRS & Event Sourcing
## Cuestionario de 45 Preguntas Estratificadas

**Ejecutado por:** Product Owner Senior / QA Funcional  
**Fecha:** 2026-04-05  
**Historia:** US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)  
**CAs vigentes:** 8 (CA-01 a CA-08, post-remediación)  
**SSOT:** `docs/requirements/v1_user_stories.md` (líneas 5599–5715)  
**Referencia cruzada:** US-029 (34 CAs — historia gemela de Frontend)

---

## 1. Adecuación Funcional (20 Preguntas)

### Event Sourcing y Proyección (CA-01, CA-06)

**#1.** El CA-01 define un Worker asíncrono que proyecta eventos a tablas analíticas planas. Si el Worker falla durante la proyección de un evento (Ej: error de disco, OOM), ¿qué pasa con ese evento? ¿Se reintenta automáticamente la proyección? ¿Existe un mecanismo de Dead Letter para eventos que fallan repetidamente?

**#2.** El CA-06 define 4 tipos de eventos en V1: `FORM_SUBMITTED`, `FORM_DRAFT_SAVED`, `TASK_AUTO_CLAIMED`, `FORM_REJECTED`. ¿El evento `FORM_DRAFT_SAVED` se graba en el Event Store CADA VEZ que el Merge Commit del CA-07 (PUT /draft) se ejecuta, o solo se graba un snapshot final? Un formulario extenso con Debounce de 10s podría generar **300+ eventos por hora** por operario, saturando la tabla.

**#3.** ¿Se necesita algún evento adicional que NO esté en la lista de V1? Por ejemplo: `FILE_ATTACHED` (cuando un archivo Upload-First se vincula), `FORM_VALIDATION_FAILED` (cuando el Backend rechaza un 400), o `TASK_DELEGATED` (cuando un supervisor ejecuta Forced Unclaim + Re-Claim per US-029 CA-23)?

**#4.** La tabla `form_event_store` tiene PROHIBIDO el `UPDATE` y `DELETE`. ¿Cómo se manejan los casos regulatorios de "derecho al olvido" (GDPR/Ley 1581 de Colombia)? Si un cliente solicita la eliminación de sus datos personales, ¿se permite un `DELETE` controlado con auditoría especial, o se aplica un "soft-delete" mediante un evento `DATA_ERASURE_REQUESTED` que anula lógicamente el payload?

**#5.** ¿La columna `payload_json` almacena el formulario completo (todos los campos, incluyendo los de solo lectura/prefillData), o solo los campos que el operario MODIFICÓ (delta)? Almacenar todo el formulario simplifica las consultas analíticas, pero si un formulario pesa 100KB y hay 10,000 envíos diarios = 1GB/día solo en eventos. ¿Existe una política de retención?

### Exclusión Topológica de Camunda (CA-02)

**#6.** El CA-02 dice que a Camunda solo se le envía un DTO mínimo. ¿Quién decide qué variables van al DTO mínimo? ¿Lo define el Arquitecto al diseñar el BPMN (en la Pantalla 6 de US-005), o es una inferencia automática basada en los Gateways del proceso? Si es manual, ¿cómo se previene que un Arquitecto olvide incluir una variable requerida por un Gateway?

**#7.** ¿Qué pasa si el Gateway de un BPMN necesita una variable que NO fue incluida en el DTO mínimo? ¿Camunda falla silenciosamente tomando el camino default, o el proceso se detiene con un Incident? ¿El sistema detecta esta discrepancia preventivamente al desplegar el BPMN (Pre-Flight de US-005)?

### ACID Fallback y Saga Inversa (CA-03)

**#8.** El CA-03 define un Rollback Compensatorio cuando Camunda falla. ¿El Rollback elimina FÍSICAMENTE el evento `FORM_SUBMITTED` del Event Store, o genera un evento compensatorio `FORM_SUBMIT_ROLLED_BACK` que anula lógicamente el submit? Si lo elimina físicamente, viola la regla de inmutabilidad del CA-06. Si lo anula lógicamente, la proyección debe ser consciente de estos eventos de compensación.

**#9.** ¿Cuál es el timeout máximo que el Backend esperará la respuesta de Camunda antes de ejecutar el Rollback? ¿5 segundos? ¿10 segundos? ¿30 segundos? Este valor impacta directamente el tiempo de espera del operario que ve el spinner del US-029 CA-20.

**#10.** Si Camunda devuelve un error transitorio (Ej: HTTP 503 "Service Temporarily Unavailable" pero se recupera 2 segundos después), ¿el Backend reintenta automáticamente antes de ejecutar el Rollback, o ejecuta el Rollback inmediatamente? ¿Existe un circuito de retry (Ej: 3 intentos con backoff exponencial: 1s, 2s, 4s)?

### Auto-Claim Controlado (CA-04)

**#11.** El CA-04 dice que el Auto-Claim aplica a tareas de grupo sin assignee. ¿El operario PUEDE abrir el formulario de una tarea de grupo sin reclamarla? El flujo normal (US-002 → US-029) asume que la tarea fue reclamada ANTES de abrirla. ¿Se modifica el flujo de la Pantalla 1 para permitir abrir tareas no reclamadas, o el Auto-Claim es un caso excepcional donde el operario reclamó desde la grilla pero un Forced Unclaim ocurrió entre el reclamo y el submit?

**#12.** Si dos operarios abren SIMULTÁNEAMENTE la misma tarea de grupo (ambos ven el formulario), pero ninguno la ha reclamado aún: el PRIMERO en presionar [Enviar] gana el Auto-Claim. ¿Qué experiencia recibe el SEGUNDO operario? ¿Solo ve un error 409? ¿Se le notifica proactivamente que la tarea fue tomada por otro, ANTES de que intente enviar (Ej: WebSocket de invalidación)?

**#13.** ¿El evento `TASK_AUTO_CLAIMED` del CA-06 contiene suficiente información para una auditoría regulatoria? ¿Incluye el `groupId` del grupo de trabajo, la lista de candidatos que PODÍAN tomar la tarea, y una justificación de por qué se auto-asignó?

### Trazabilidad de Rechazos (CA-05)

**#14.** El `rejectionLogs` se inyecta en el `/form-context` del BFF. ¿Qué campos contiene cada entrada del array? ¿Mínimo: `rejectedBy` (quién rechazó), `rejectedAt` (cuándo), `reason` (dictamen textual), `taskId` (qué tarea rechazó), `formVersion` (con qué versión del formulario)?

**#15.** ¿Se limita la cantidad de rechazos visibles? Si una tarea fue rechazada 15 veces (ida y vuelta entre operarios), ¿el Alert en la Pantalla 2 mostrará los 15 rechazos completos (scrollable), solo el último, o los últimos 3?

**#16.** ¿El operario puede responder al rechazo dejando una nota de "corrección aplicada"? ¿O el Rechazo es solo informativo (solo lectura) y la corrección se infiere del nuevo submit?

### Endpoints de Borradores (CA-07)

**#17.** El `GET /draft` devuelve el borrador más reciente. ¿Se mantiene UN SOLO borrador por tarea en la tabla `task_drafts`, o se mantiene un historial de borradores (Ej: los últimos 5 snapshots) para permitir un "undo" de nivel borrador?

**#18.** El Cron Job de limpieza de borradores huérfanos elimina drafts con `updated_at > 72h`. ¿Este TTL se reinicia cuando el operario hace un GET (lectura) del borrador, o SOLO cuando hace un PUT (escritura)? Si la lectura reinicia el TTL, un operario que abre la tarea diariamente para "ver" sin editar mantendría el borrador vivo indefinidamente.

### Cierre de GAPs y Reconciliación (CA-08)

**#19.** El CA-08 establece la "Merge Commit Rule" para PRs que tocan ambas US. ¿Quién actúa como reviewer obligatorio de un PR que referencia ambas US? ¿El PO, el Arquitecto, o un reviewer automático (CODEOWNERS)?

**#20.** ¿Existe un proceso periódico de verificación que audite si algún CA de la US-017 ha "invadido" territorio de la US-029 o viceversa? ¿O la reconciliación del CA-08 es un acuerdo estático que no se verifica en runtime?

---

## 2. Seguridad y Hardening (10 Preguntas)

**#21.** La columna `payload_json` del Event Store almacena el formulario completo. Si el formulario contiene campos PII (Ej: número de cédula, teléfono), ¿el Event Store almacena estos datos en texto plano, o se aplica cifrado at-rest (AES-256) a nivel de columna JSONB? La US-029 cifra PII en el LocalStorage del navegador (CA-11), pero el Event Store del servidor podría tenerlos sin cifrar.

**#22.** El CA-07 expone tres endpoints de borradores. ¿El endpoint `GET /draft` devuelve campos PII en texto claro al Frontend? ¿O aplica la misma máscara de enmascaramiento de la US-000 (Ej: cédula `***456`)?

**#23.** El `idempotency_key` del CA-06 previene la grabación duplicada de eventos. ¿Cuánto tiempo se mantiene la llave en el índice UNIQUE? Si el operario cierra el formulario, espera 24 horas, y lo reabre con un NUEVO `Idempotency-Key`, ¿las llaves antiguas se purgan? ¿O permanecen indefinidamente consumiendo espacio de índice?

**#24.** El Rollback Compensatorio del CA-03 ejecuta un borrado en PostgreSQL. ¿Este borrado está protegido contra inyección SQL si el `taskId` viene del request del usuario? ¿Se usan prepared statements exclusivamente?

**#25.** ¿El endpoint `POST /complete` valida que el `task_id` del path pertenezca al `process_instance_id` del JWT del usuario, o solo valida el `assignee`? Un atacante podría intentar enviar un formulario a un `task_id` de OTRO proceso (no necesariamente el suyo) si solo se valida la asignación.

**#26.** ¿Los eventos del Event Store son consultables por API para operarios normales, o solo para usuarios con rol `ADMIN_IT`/`AUDITOR`? Si un operario puede hacer `GET /events?taskId=TK-100`, podría acceder a datos de tareas que no le pertenecen.

**#27.** El Worker de proyección asíncrona transforma eventos en tablas planas. ¿El Worker se ejecuta bajo un contexto de seguridad propio (service account), o hereda el contexto del usuario que generó el evento? Si hereda el contexto del usuario, un operario con roles limitados podría no poder proyectar sus propios eventos.

**#28.** ¿El CA-04 (Auto-Claim) verifica que el `userId` extraído del JWT sea miembro válido del `candidateGroup` de Camunda antes de ejecutar el `taskService.claim()`? Un atacante que conozca el `taskId` pero NO sea miembro del grupo podría intentar un Auto-Claim no autorizado.

**#29.** ¿La tabla `task_drafts` está protegida contra ataques de volumen (DoS)? Si un atacante envía 10,000 PUTs por segundo al endpoint `/draft`, ¿hay rate-limiting, o el Backend acepta todo y satura PostgreSQL?

**#30.** ¿Los `rejectionLogs` del CA-05 sanitizan el contenido del dictamen textual del rechazo? Si el analista de QC escribe `<script>alert('xss')</script>` como motivo de rechazo, ¿el Frontend lo renderiza como HTML o como texto plano?

---

## 3. Experiencia de Usuario - UX/UI (10 Preguntas)

**#31.** El CA-03 dice que se devuelve HTTP 500 "Motor No Disponible" al operario. ¿Qué experiencia visual recibe el operario en la Pantalla 2? ¿Solo un mensaje de error genérico, o un mensaje específico tipo: "Tu formulario fue correctamente guardado pero el motor de procesos no está disponible en este momento. Tus datos NO se perdieron. El sistema reintentará automáticamente en los próximos minutos"? (¿O es que el Rollback SÍ borra sus datos?)

**#32.** Cuando el operario abre una tarea que fue rechazada (CA-05), ¿el Alert de rechazo es un bloque colapsable/expandible (para no obstruir el formulario), o es un banner fijo que siempre ocupa espacio visual? En formularios cortos no hay problema, pero en formularios largos un banner de rechazo pesado puede empujar el formulario hacia abajo.

**#33.** Si el Auto-Claim (CA-04) falla con HTTP 409 porque otro operario se adelantó, ¿qué ve el segundo operario? ¿Solo un error modal, o se le redirige automáticamente al Workdesk con un Toast informativo: "Esta tarea fue tomada por [nombre_operario]. Se te ha redirigido a tu bandeja"?

**#34.** ¿El operario tiene alguna forma de saber que su envío generó un evento CQRS exitosamente? ¿O la confirmación del US-029 CA-21 (checkmark ✅ "Tarea completada") es suficiente? ¿Existe un número de referencia o ID de evento visible para el operario (Ej: "Referencia: EVT-abc123") que pueda citar si necesita reclamar a soporte?

**#35.** En el `GET /draft` para recuperar un borrador del servidor, ¿qué experiencia ve el operario si el borrador del servidor fue guardado con `schema_version: V2` pero el formulario actual ya es `V3`? ¿Se muestran campos en rojo tipo Lazy Patching (US-029 CA-08), o se descarta el borrador por incompatibilidad?

**#36.** ¿El operario a quien le rechazaron la tarea puede ver SOLO su formulario rechazado, o también puede ver quién lo rechazó (nombre completo) y cuándo? ¿Mostrar el nombre del revisor que rechazó genera conflictos interpersonales en la organización? ¿Se debe anonimizar?

**#37.** Cuando el Worker de proyección tiene retraso y un dashboard (US-009) muestra datos desactualizados, ¿el dashboard indica al usuario: "Última actualización: hace 5 minutos, datos posiblemente desactualizados"? ¿O muestra datos stale sin advertencia?

**#38.** Si el operario completa una tarea y el RYOW (US-029 CA-17) la elimina de Pinia, pero la proyección CQRS aún no ha terminado, ¿qué pasa si el operario navega a un dashboard que muestra "tareas completadas hoy" y su tarea no aparece aún? ¿Se muestra algún indicador de "procesando"?

**#39.** ¿El endpoint `DELETE /draft` se ejecuta ANTES o DESPUÉS del redirect al Workdesk (US-029 CA-21)? Si se ejecuta después y falla silenciosamente, ¿el borrador fantasma aparecerá mañana cuando el operario abra otra tarea?

**#40.** ¿Existe feedback visual cuando el Merge Commit del borrador (`PUT /draft`) se ejecuta exitosamente? ¿O el indicador de sincronización de US-029 CA-31 (☁️/💾) ya cubre este caso?

---

## 4. Eficiencia de Desempeño (5 Preguntas)

**#41.** ¿Cuál es el SLA de latencia máxima tolerada para el endpoint `POST /complete` completo (validación + Event Store + Camunda + response)? ¿2 segundos? ¿5 segundos? ¿10 segundos? El operario está viendo el spinner de US-029 CA-20 durante todo este tiempo.

**#42.** El Worker de proyección asíncrona del CA-01 procesa eventos. ¿Cuál es el SLA de latencia entre la grabación del evento y su disponibilidad en la tabla analítica? ¿1 segundo? ¿5 segundos? ¿30 segundos? Esto afecta la percepción de "tiempo real" en los dashboards de US-009.

**#43.** Si 200 operarios presionan [Enviar] simultáneamente (Ej: cierre de mes), ¿la tabla `form_event_store` soporta 200 INSERTs concurrentes sin degradación? ¿Se necesita particionamiento por `created_at` o por `process_instance_id`?

**#44.** El `payload_json` JSONB puede pesar hasta 100KB por evento (formularios extensos). En un año con 500,000 eventos, la tabla `form_event_store` pesaría ~50GB. ¿Existe una estrategia de archivado (Ej: mover eventos con `created_at > 1 año` a una tabla `form_event_store_archive` o a cold storage)?

**#45.** El endpoint `PUT /draft` se ejecuta cada 10 segundos de inactividad (Debounce US-029 CA-24). Con 100 operarios activos simultáneamente, esto genera **600 PUTs por minuto** al servidor. ¿El endpoint de drafts estará en una base de datos separada o en el mismo PostgreSQL del Event Store? ¿Se necesita un pool de conexiones dedicado para evitar que los drafts consuman las conexiones del flujo principal de `/complete`?

---

## Observaciones Anti-Alucinación

### ✅ Cumplimiento:
Se alcanzaron las 45 preguntas con calidad analítica. La US-017, a pesar de tener solo 8 CAs, es una historia de alta complejidad arquitectónica que genera preguntas profundas sobre consistencia transaccional, concurrencia, rendimiento y seguridad de datos.

### ⚠️ Advertencias:
1. **Las preguntas #31 a #40 (UX/UI) están en zona fronteriza** entre US-017 y US-029. Dado que la US-017 es una historia de Backend, sus implicaciones de UX son indirectas (el operario percibe los efectos del CQRS a través de la UI de la US-029). Las preguntas se formularon para descubrir si la US-017 necesita CAs adicionales que INFORMEN al Frontend sobre estados Backend, no para redefinir la UX que es territorio de la US-029.

2. **La pregunta #4 (GDPR/Ley 1581)** toca un tema legal que NO está definido en ninguna US del backlog. Si esta es una preocupación real para el producto, podría requerir una US independiente de "Gobernanza de Datos Personales" o una nota arquitectónica transversal.

3. **La pregunta #8 (naturaleza del Rollback)** es CRÍTICA: la respuesta determinará si el Event Store es realmente inmutable (append-only con compensación) o si tiene excepciones de borrado para Sagas. Esta decisión tiene implicaciones profundas para la auditoría forense.
