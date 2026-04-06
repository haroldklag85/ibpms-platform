# Análisis Funcional y de Entendimiento: US-002

## Historia Analizada
**US-002: Reclamar una Tarea de Grupo (Claim Task)**

---

### 1. Resumen del Entendimiento

La US-002 define la **mecánica completa de propiedad del trabajo** en el iBPMS. Mientras la US-001 MUESTRA las tareas en la bandeja, la US-002 resuelve la pregunta: "¿Cómo paso una tarea de la cola compartida del equipo a MI bandeja personal?" — y todo el ciclo de vida que sigue: reclamar, explorar sin reclamar, liberar, transferir a un compañero, y la intervención forzosa de un jefe.

La historia se estructura en **5 dominios funcionales**:

1. **Reclamo y Concurrencia (CA-01, CA-02):** Resolución de condición de carrera cuando dos personas reclaman la misma tarea al mismo milisegundo (el primero gana, el segundo recibe un aviso amable). Reclamo masivo de hasta 10 tareas en un solo clic con reporte parcial de éxito/fallo.

2. **Liberación y Transferencia (CA-04, CA-07):** El operario puede devolver una tarea a la cola del grupo con un mensaje opcional para un compañero ("@Pedro, te liberé este caso"). Al liberar, se borran todos los datos parciales del formulario para que el siguiente compañero la reciba limpia (Amnesia Transaccional).

3. **Exploración Segura (CA-05):** Modo "Solo Lectura" que permite ver el formulario y los anexos de una tarea SIN reclamarla. La tarea sigue en la cola del grupo hasta que el botón físico [Reclamar] sea presionado.

4. **Gobernanza y Control (CA-06, CA-08, CA-09):** Auto-liberación automática de tareas abandonadas por inactividad (Ghost Job Timeout). El supervisor puede despojar tareas de un operario ausente (Forced Unclaim). Bitácora forense de toda la rotación de asignaciones.

5. **Resiliencia ante Desconexiones (CA-10):** Si la red cae al momento de reclamar, el Frontend simula la asignación visualmente (Optimistic UI) y reintenta la petición al servidor automáticamente.

---

### 2. Objetivo Principal

Garantizar **cero duplicidad operativa**: que dos personas NUNCA trabajen el mismo caso al mismo tiempo. Y garantizar **gobernanza total de la propiedad**: que si alguien reclama y abandona una tarea, el sistema la rescate automáticamente para que otro la atienda, preservando la limpieza de los datos del formulario.

---

### 3. Alcance Funcional Definido

| Dimensión | Hasta Dónde Llega | Dónde Termina |
|---|---|---|
| **Reclamo Individual** | Un clic en [Reclamar] asigna la tarea al usuario. HTTP 409 si alguien se adelantó (CA-01) | No define el mecanismo atómico de BD (bloqueo pesimista o claim nativo de Camunda) |
| **Reclamo Masivo** | Selección múltiple + [Reclamar Seleccionadas] con reporte parcial (CA-02) | No define un máximo de tareas por lote |
| **Liberación** | Devuelve la tarea a la Cola Grupal con mensaje opcional (CA-04) | No define si la liberación masiva existe |
| **Exploración** | Modo Solo Lectura con doble clic (CA-05) | No define si el modo lectura bloquea la tarea temporalmente para otros exploradores |
| **Auto-Unclaim** | Cron Job que detecta inactividad superior al SLA (CA-06) | No define el umbral exacto ni qué significa "inactividad" |
| **Amnesia Transaccional** | Al liberar, se purga el LocalStorage y Camunda NO recibe datos parciales (CA-07) | No define qué pasa con archivos adjuntos ya subidos |
| **Despojo Forzoso** | El Supervisor puede despojar manualmente al operario ausente (CA-08) | No define validación de perímetro organizacional |
| **Trazabilidad** | Pop-Up con historial completo de rotación del assignee (CA-09) | No define si el historial incluye el motivo de cada cambio |
| **Resiliencia Offline** | Optimistic UI con reintento automático (CA-10) | No define qué pasa si el reintento falla repetidamente |

---

### 4. Lista de Funcionalidades Incluidas

#### A. Reclamo y Concurrencia
1. Reclamo individual con resolución First-Writer-Wins (CA-01)
2. Respuesta HTTP 409 Conflict con modal amable al perdedor (CA-01)
3. Reclamo masivo (Bulk Claim) con reporte parcial de éxito/fallo (CA-02)
4. Transacción Batch para asignación en lote (CA-02)

#### B. Liberación y Transferencia
5. Botón [Liberar Tarea] en la Pantalla 5 (CA-04)
6. Campo opcional para adjuntar Mensaje Interno al liberar (CA-04)
7. Modal bloqueante de advertencia al liberar con datos parciales (CA-07)
8. Purga de LocalStorage de la tarea al confirmar la liberación (CA-07)
9. Prohibición estricta de enviar datos parciales a Camunda al liberar (CA-07)
10. Garantía de formulario limpio para el siguiente reclamante (CA-07)

#### C. Exploración Segura
11. Doble clic para abrir detalle en Modo Solo Lectura (CA-05)
12. No altera el assignee hasta presionar [Reclamar] explícitamente (CA-05)
13. Renderizado completo de formulario y anexos en lectura (CA-05)

#### D. Gobernanza y Control
14. Cron Job backend que detecta tareas con inactividad superior al SLA (CA-06)
15. Auto-Unclaim: purga del assignee inactivo y devolución a la Cola Grupal (CA-06)
16. Controles de Supervisor con privilegios elevados en vista de monitoreo (CA-08)
17. Forced Unclaim: despojo inmediato y devolución a disponibilidad pública (CA-08)
18. Botón de Bitácora "Ver Trazabilidad" con Pop-Up de historial cronológico (CA-09)
19. Historial completo de rotación del assignee desde la BD de Auditoría (CA-09)

#### E. Resiliencia
20. Reclamo Optimistic UI: el Frontend coloca la tarea visualmente en "Mi Bandeja" (CA-10)
21. Ruteo/re-intento sincrónico automático hasta confirmación del Motor (CA-10)
22. Degradación controlada ante micro-cortes de red (CA-10)

---

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas

#### GAP-1: Ausencia del Mecanismo Atómico de BD para Reclamo Simultáneo (CA-01)

El CA-01 dice que cuando dos personas reclaman la misma tarea al mismo instante, "el primero gana y el segundo recibe un aviso 409". Pero NO dice CÓMO el servidor garantiza que solo uno gane. Sin un candado en la base de datos, podrían ocurrir situaciones donde AMBOS creen que ganaron ("Split-Brain").

Dos soluciones posibles:
- Opción A: Usar el comando nativo de Camunda `TaskService.claim()`.
- Opción B: Forzar un bloqueo pesimista en PostgreSQL (`SELECT FOR UPDATE`).

Riesgo si no se cierra: Dos personas terminan trabajando el mismo caso. Duplicidad total.

#### GAP-2: Notificación WebSocket No Definida tras Reclamo/Liberación

Cuando una persona reclama una tarea, los demás compañeros que ven esa misma tarea en su pantalla NO son notificados. La US-001 CA-06/CA-13 define que las tareas "desaparecen" instantáneamente por WebSocket, pero la US-002 NO define la obligación de EMITIR ese evento WebSocket desde el Backend al momento del reclamo/liberación.

Consecuencia: Los compañeros siguen viendo la tarea como "disponible" durante minutos u horas. Cientos de errores 409.

#### GAP-3: Escalación de Privilegios Horizontal en Despojo Forzoso (CA-08)

El CA-08 dice que "un gerente con Rol de Supervisor" puede despojar tareas a cualquier operario. Pero NO dice si el supervisor de un departamento puede despojar tareas de OTRO departamento.

Resolución: El Backend debe cruzar el team_id del supervisor contra el team_id de la tarea.

#### GAP-4: Contrato API No Definido para las Operaciones de Reclamo

La US-002 NO define los endpoints REST:
- POST /api/v1/tasks/{taskId}/claim
- POST /api/v1/tasks/bulk-claim
- POST /api/v1/tasks/{taskId}/release
- POST /api/v1/tasks/{taskId}/force-unclaim
- GET /api/v1/tasks/{taskId}/audit-trail

#### GAP-5: Umbral del Cron Job "Ghost Job Timeout" sin Definir (CA-06)

El CA-06 dice que un proceso automático detectará tareas con "inactividad superior al SLA" y las liberará. Pero:
- ¿Qué significa "inactividad"? ¿Cero clics? ¿Cero actualizaciones de estado?
- ¿El umbral es fijo o configurable por proceso/tenant?
- ¿Se avisa al operario ANTES del auto-unclaim?
- ¿El auto-unclaim también activa la Amnesia Transaccional del CA-07?

---

### 6. Lista de Exclusiones (Fuera de Alcance V1)

1. Límite simultáneo de tareas por operario (CA-03 diferido a V2).
2. Reasignación directa entre pares (solo liberar a cola grupal).
3. Reclamo automático por el sistema (Auto-Assignment pertenece a US-001 CA-16/CA-21).
4. Ejecución/completitud de la tarea (pertenece a US-029).
5. Liberación masiva (no existe simétrico al Bulk Claim).
6. Notificaciones externas (email/push) al despojar.
7. Historial de motivos en la trazabilidad (CA-09 solo muestra rotación, no motivos).
8. Protección de archivos adjuntos al liberar.

---

### 7. Observaciones de Alineación o Riesgos para Continuar

**Riesgo Crítico: GAP-1 + GAP-2 combinados.** Si el reclamo no tiene bloqueo atómico en BD Y no emite notificación WebSocket, el resultado es un sistema donde múltiples personas creen tener la misma tarea Y no se enteran de que otros la reclamaron.

**Dependencias Externas de la US-002:**
- **US-001 (Workdesk / Pantalla 1):** La grilla donde se ven las tareas y el botón [Reclamar]. Los WebSockets de desaparición dependen de que US-002 emita el evento al hacer Commit.
- **US-029 (Completar Tarea):** El formulario se trabaja en US-029. La Amnesia Transaccional (CA-07) depende del patrón de LocalStorage de US-029.
- **US-036 (RBAC / Pantalla 14):** La validación perimetral del despojo forzoso (GAP-3) consume la jerarquía organizacional de US-036.
- **US-001 CA-28:** El bloqueo pesimista de "Atender Siguiente" es análogo al GAP-1 de US-002.

**Fortalezas Sobresalientes:**
1. Amnesia Transaccional (CA-07) = protección del siguiente operario contra datos basura.
2. Modo Solo Lectura (CA-05) = reduce reclamos innecesarios.
3. Optimistic UI (CA-10) = UX de élite ante latencia.
4. Mensaje Interno en Liberación (CA-04) = comunicación asíncrona intra-equipo.
5. Trazabilidad Forense (CA-09) = esencial para auditoría regulatoria.
