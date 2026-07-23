# Handoff de Remediación y Cierre — US-043 (SLA & Business Calendar)
**Emisor:** Arquitecto Líder (Auditoría Iteración 2)
**Destino:** Equipo Backend, Frontend y QA
**Prioridad:** ALTA — Cierre de Iteración 2

---

## 1. Contexto y Diagnóstico del GAP

La US-043 define el **Motor del Tiempo Corporativo** para el iBPMS. La auditoría forense detectó un **GAP Arquitectónico Crítico (CA-4061)**: los handoffs originales asumían acceso directo a la tabla relacional `ACT_RU_JOB` (Camunda 7), la cual **no existe en Camunda 8 (Zeebe/RocksDB)**.

### Estado Actual tras Investigación Forense

Sin embargo, tras la inspección exhaustiva del código fuente, se confirma que la implementación real **NO utiliza `ACT_RU_JOB`**. El equipo utilizó la **API Java nativa de Camunda 7 (`TaskService.createTaskQuery()`)**, que es compatible con nuestra arquitectura actual (Camunda 7 Run embedded). **El GAP original se declara como FALSO POSITIVO a nivel de código**, ya que el análisis funcional mencionaba el SQL directo, pero la implementación real lo abstrae correctamente.

> [!IMPORTANT]
> **VEREDICTO ARQUITECTÓNICO:** El GAP CA-4061 se reclasifica de "LETAL" a "INFORMATIVO". La implementación existente es viable para V1 con las remediaciones menores detalladas abajo.

---

## 2. Inventario de Artefactos Existentes

### Backend (ya implementado)
| Artefacto | Ruta | Estado |
|---|---|---|
| `CustomBusinessCalendar.java` | `infrastructure/bpm/calendar/` | ✅ Funcional (CA-1, CA-2, CA-4 parcial) |
| `CamundaEngineConfiguration.java` | `infrastructure/bpm/config/` | ✅ Inyectado en `dueDate`, `duration`, `cycle` |
| `SlaService.java` | `application/service/bpm/` | ✅ Recálculo `@Async` paginado (CA-3) |
| `SlaAdminController.java` | `infrastructure/web/bpm/` | ✅ Endpoints `/apply` y `/holidays` CRUD |
| `BusinessHoursEntity.java` | `infrastructure/jpa/entity/bpm/` | ✅ Tabla `ibpms_business_hours` |
| `HolidayEntity.java` | `infrastructure/jpa/entity/bpm/` | ✅ Tabla `ibpms_holiday` |
| `SlaTimerEngineIntegrationTest.java` | `test/.../camunda/` | ✅ 2 tests (Weekend Skip + 80% Warning) |

### Frontend (ya implementado)
| Artefacto | Ruta | Estado |
|---|---|---|
| `PmoSettings.vue` | `views/admin/PMO/` | ✅ Pantalla 19 completa (Horas, Feriados, Modal 202) |

---

## 3. Matriz de Remediación Pendiente por CA

### CA-1 (Custom BusinessCalendar) → PARCIAL
**Brecha:** El parser de duración usa un hardcode de `4 horas SLA` fijo. Falta parsear la duración real desde el BPMN (`PT4H`, `PT8H`, `P2D`).

**Remediación Backend:**
- Modificar `CustomBusinessCalendar.resolveDuedate()` para parsear `duedateDescription` como ISO 8601 Duration (`java.time.Duration.parse()`).
- Fallback: Si el parsing falla, usar las 4 horas por defecto actuales.

### CA-2 (Exención Sistémica) → OK
Implementado. El marcador `"SYSTEMIC_24_7"` en la description bypasea el calendario. Compatible.

### CA-3 (Recálculo Retroactivo Anti-Deadlock) → PARCIAL
**Brecha Backend:** El `SlaService.recalculateActiveSlas()` suma `+1 hora` al dueDate existente como stub. Debe invocar al `CustomBusinessCalendar.resolveDuedate()` para recalcular correctamente.

**Brecha Frontend:** El endpoint llamado desde `PmoSettings.vue` apunta a `/api/v1/admin/pmo/sla/recalculate` pero el controller real está en `/api/v1/admin/sla/apply`. **Misalignment de ruta**.

**Remediación Backend:**
- En `SlaService.recalculateActiveSlas()`, reemplazar el stub `+1 hora` por invocación real: `customCalendar.resolveDuedate(originalDuration, task.getCreateTime())`.

**Remediación Frontend:**
- Corregir `PmoSettings.vue` línea 192: cambiar la URL de `'/api/v1/admin/pmo/sla/recalculate'` a `'/admin/sla/apply'` (el baseURL del apiClient ya incluye `/api/v1`).
- Enviar `applyRetroactively` como query param según el controller: `apiClient.post('/admin/sla/apply', null, { params: { applyRetroactively: slaForm.value.applyRetroactive } })`.

### CA-4 (Husos Horarios Híbridos) → PARCIAL
**Brecha:** El `CustomBusinessCalendar` usa `ZoneId.systemDefault()` y no consulta el timezone del Assignee.

**Remediación Backend (V1 Minimal):**
- Agregar campo `timezone` a `BusinessHoursEntity` (String, default `"America/Bogota"`).
- El `CustomBusinessCalendar` usará este timezone global configurado en vez de `systemDefault()`.
- **Nota V2:** La resolución per-user (timezone del Assignee) se difiere al roadmap V2.

**Remediación Backend (Controller):**
- Agregar endpoint `PUT /api/v1/admin/sla/business-hours` en `SlaAdminController` para persistir los cambios de horario (actualmente solo se guardan los feriados).
- Agregar endpoint `GET /api/v1/admin/sla/business-hours` para hidratar el formulario del frontend.

### CA-5 (API Feriados & Fallback) → PARCIAL
**Brecha:** Falta el endpoint `DELETE /api/v1/admin/sla/holidays/{id}` para eliminar feriados desde el frontend. El `PmoSettings.vue` tiene el botón de eliminar pero opera solo localmente (sin llamada API).

**Remediación Backend:**
- Agregar `@DeleteMapping("/holidays/{id}")` en `SlaAdminController`.

**Remediación Frontend:**
- Conectar `addHoliday()` al endpoint real `POST /admin/sla/holidays`.
- Conectar `removeHoliday(id)` al endpoint real `DELETE /admin/sla/holidays/{id}`.
- Cargar los feriados desde el API en `onMounted()` con `GET /admin/sla/holidays`.

### CA-6 (Early Warning 80%) → PARCIAL
**Brecha:** El test valida la lógica del 80% pero no existe un `Listener` o `Scheduled Job` que lo ejecute proactivamente en runtime.

**Remediación Backend (V1 Sufficient):**
- Crear `SlaEarlyWarningScheduler` con `@Scheduled(fixedRate = 300000)` (cada 5 min).
- Itera tareas activas, calcula `% consumido`, y si >= 80% → setea variable `isSlaAtRisk = true` vía `TaskService.setVariable()`.
- Publica `SlaAtRiskEvent` (Spring Event) como bisagra para futuro US-049 (Notificaciones).

**Remediación Frontend (Workdesk/Inbox):**
- Ya existe lógica de badges SLA en `InboxView.vue` y `Workdesk.vue`. Verificar que consuman el flag `isSlaAtRisk` del DTO de tarea.

---

## 4. Delegación por Rol

### 🔧 Agente Backend
1. Parsear ISO 8601 en `CustomBusinessCalendar` (CA-1).
2. Inyectar `CustomBusinessCalendar` en `SlaService.recalculateActiveSlas()` (CA-3).
3. Agregar campo `timezone` a `BusinessHoursEntity` + usarlo en Calendar (CA-4).
4. Agregar endpoints CRUD completos: `GET/PUT /business-hours`, `DELETE /holidays/{id}` (CA-4, CA-5).
5. Crear `SlaEarlyWarningScheduler` con `@Scheduled` (CA-6).

### 🖥️ Agente Frontend
1. Corregir URL de `PmoSettings.vue` → `/admin/sla/apply` (CA-3).
2. Conectar CRUD real de Feriados al API (CA-5).
3. Hidratar `slaForm` desde `GET /admin/sla/business-hours` en `onMounted` (CA-4).
4. Validar badge `isSlaAtRisk` en Workdesk/Inbox (CA-6).

### 🧪 Agente QA
1. Verificar que `SlaTimerEngineIntegrationTest` pasa con las correcciones de parsing.
2. Agregar test para `SlaEarlyWarningScheduler` (mock de `TaskService`).
3. Test E2E: Crear feriado → Verificar que aparece en grid → Eliminarlo.
4. Test E2E: Cambiar horario + toggle retroactivo → Confirmar modal HTTP 202.

---

## 5. Criterio de Cierre (Definition of Done)

- [ ] Los 6 CAs de la US-043 están cubiertos (Backend + Frontend integrados).
- [ ] El `CustomBusinessCalendar` parsea ISO 8601 correctamente.
- [ ] El CRUD de Feriados y Business Hours opera end-to-end.
- [ ] El Early Warning `@Scheduled` detecta tareas al 80%.
- [ ] Compilación limpia Backend (`mvn package -DskipTests`) y Frontend (`npm run build`).
