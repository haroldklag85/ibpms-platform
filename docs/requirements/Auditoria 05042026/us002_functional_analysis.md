# Análisis Funcional Definitivo: US-002 (Reclamar una Tarea de Grupo)

## 1. Resumen del Entendimiento
La US-002 diagrama el modelo de adquisición y expropiación de "Trabajo en Cola". Define cómo una tarea pública ("Cola de Grupo") pasa a ser propiedad inmutable temporal de un empleado individual, incluyendo los flujos colaterales de este suceso: devolverla al grupo, delegarla, verla sin tocarla (Exploración Segura) u obligar la expropiación si el usuario la abandona o se ausenta.

## 2. Objetivo Principal
Garantizar la "Propiedad Transaccional Exclusiva" (Zero Duplicidad). Evitar a nivel de motor de base de datos que la compañía incurra en el doble diligenciamiento de un caso por parte de dos analistas distintos en oficinas separadas.

## 3. Alcance Funcional Definido
**Inicia:** Cuando la tarea está en estado libre (`assignee = null` genéricamente) y el usuario visualiza el botón de `[Reclamar]`.
**Termina:** Cuando el usuario obtiene confirmación en BD de ser el poseedor legal de la tarea, mutando el `assignee`. Abarca también la amnesia del borrador al liberar la tarea y las trazas forenses de cambio de manos.

## 4. Lista de Funcionalidades Incluidas
- **Bloqueo Síncrono de Reclamación:** Prevención básica de colisiones.
- **Bulk Claim:** Reclamo de tareas en lote.
- **Peer-to-Peer Handoff:** Traspaso directo a otro operador con mención/comentario.
- **Safe Exploration:** Carga en Lectura pre-propietario.
- **Ghost Job Timeout:** Cronjob backend para rescate automático de tareas en mora de inactividad.
- **Amnesia Transaccional:** Limpieza del LocalStorage al liberar la tarea (Protege US-029).
- **Forced Unclaim:** Despojo de supervisor.
- **Resiliencia Local:** Offline Sync temporal en el Front para mentir visualmente ante microcortes.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Race Condition SQL (Split Brain):** Múltiples Request en el mismo milisegundo derivarán en falsos `HTTP 200` si el backend no usa un *Row-Level Lock (SELECT FOR UPDATE)* o el método atómico `TaskService.claim()` nativo de Camunda. El CA-1 es ambiguo transaccionalmente.
- **Ghost Clicking Mudo:** El CA-2 no instruye explícitamente disparar un `WebSocket Broadcast` al grupo operativo tras el éxito. Los demás usuarios verán la tarea libre por horas, cayendo repetitivamente en Falsos 409.
- **Escalación IDOR Horizontal (Despojo):** En CA-8, un Gerente con rol "Supervisor" global podría despojar tareas de otro equipo si intercepta el `TaskId`. Requiere *Cross-Tenant / Team_ID Validation* obligatoria.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Límite estructural de restricción de acaparamiento simultáneo ("Sólo puedes reclamar 10 hoy") (`Diferido a V2`).
- Guardado en Camunda de payloads JSON de borrador al oprimir "Liberar Tarea".

## 7. Observaciones de Alineación o Riesgos
**Riesgo Activo (Requiere Parches SRE):** Las omisiones en concurrencia (Sincronización vs Asincronía) causarán alta fricción operativa en Producción bajo estrés. Deben parcharse los criterios de aceptación CA-1, CA-2 y CA-8 dentro del SSOT (`v1_user_stories.md`).
