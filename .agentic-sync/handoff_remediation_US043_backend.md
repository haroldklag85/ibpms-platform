# Handoff de Remediación: US-043 (SLA & Business Calendar) — Agente Backend

**Fecha:** 2026-04-19
**Emisor:** Arquitecto Líder (Auditoría Iteración 2)
**Destino:** Agente Backend (Experto Data & BPM Engine)
**Prioridad:** ALTA — Cierre de Iteración 2

---

## 📋 Contexto Ejecutivo

La US-043 implementa el Motor del Tiempo Corporativo. Tras la auditoría forense se confirma que la implementación existente es **viable para V1** (no usa SQL directo contra `ACT_RU_JOB` como temía el análisis funcional original — el código usa `TaskService.createTaskQuery()` que es compatible con nuestra arquitectura Camunda 7 embebida).

Sin embargo, existen **5 brechas de implementación** que requieren remediación para cerrar los 6 CAs.

## 📍 Inventario de Artefactos Existentes (NO MODIFICAR sin justificar)
| Artefacto | Estado |
|---|---|
| `CustomBusinessCalendar.java` (`infrastructure/bpm/calendar/`) | ✅ Funcional parcial |
| `CamundaEngineConfiguration.java` (`infrastructure/bpm/config/`) | ✅ Correcto |
| `SlaService.java` (`application/service/bpm/`) | ⚠️ Stub |
| `SlaAdminController.java` (`infrastructure/web/bpm/`) | ⚠️ Incompleto |
| `BusinessHoursEntity.java` (`infrastructure/jpa/entity/bpm/`) | ⚠️ Falta campo |
| `HolidayEntity.java` (`infrastructure/jpa/entity/bpm/`) | ✅ Correcto |
| `SlaTimerEngineIntegrationTest.java` (`test/.../camunda/`) | ✅ 2 tests OK |

---

## 🛠️ Directivas de Remediación

### TAREA 1 — CA-1: Parser ISO 8601 en `CustomBusinessCalendar`
**Archivo:** `infrastructure/bpm/calendar/CustomBusinessCalendar.java`
**Brecha:** La línea 75 tiene `int horasSla = 4;` hardcodeado. Debe parsear la duración real del BPMN.
**Acción:**
- Reemplazar el hardcode por un parser `java.time.Duration.parse(duedateDescription)`.
- Soportar formatos: `PT4H`, `PT8H`, `P2D`, `PT30M`, `4h`, número suelto (asumido como horas).
- Si el parsing falla, mantener fallback de 4 horas.

### TAREA 2 — CA-3: Recálculo Real en `SlaService`
**Archivo:** `application/service/bpm/SlaService.java`
**Brecha:** Línea 50: `new Date(task.getDueDate().getTime() + (3600 * 1000))` es un stub de +1 hora.
**Acción:**
- Inyectar `CustomBusinessCalendar` en el constructor de `SlaService`.
- **Prerequisito:** Exponer `CustomBusinessCalendar` como `@Bean` en `CamundaEngineConfiguration` para que Spring lo inyecte.
- En el loop, calcular la duración original (`dueDate - createTime`) y reconvertirla a ISO 8601 (`"PT" + hours + "H"`).
- Llamar `customCalendar.resolveDuedate(durationIso, task.getCreateTime())` para recalcular correctamente.

### TAREA 3 — CA-4: Campo `timezone` en `BusinessHoursEntity`
**Archivo:** `infrastructure/jpa/entity/bpm/BusinessHoursEntity.java`
**Brecha:** No tiene campo timezone. El `CustomBusinessCalendar` usa `ZoneId.systemDefault()` (hardcoded al server).
**Acción:**
- Agregar columna `@Column(name = "timezone", length = 50) private String timezone = "America/Bogota";` + getter/setter.
- En `CustomBusinessCalendar.resolveDuedate()`: leer `config.getTimezone()` y usar `ZoneId.of(timezone)` en vez de `ZoneId.systemDefault()`.
- **Nota V2:** La resolución per-user (timezone del Assignee individual) se difiere al roadmap V2.

### TAREA 4 — CA-4/CA-5: Endpoints CRUD faltantes en `SlaAdminController`
**Archivo:** `infrastructure/web/bpm/SlaAdminController.java`
**Brecha:** Faltan endpoints de Business Hours y Delete de Holidays.
**Acción:**
- Inyectar `BusinessHoursRepository` en el constructor.
- Agregar `GET /api/v1/admin/sla/business-hours` → devuelve el primer registro o defaults.
- Agregar `PUT /api/v1/admin/sla/business-hours` → persiste `startTime`, `endTime`, `workOnWeekends`, `timezone`.
- Agregar `DELETE /api/v1/admin/sla/holidays/{id}` → elimina por UUID.

### TAREA 5 — CA-6: `SlaEarlyWarningScheduler` (NUEVO ARCHIVO)
**Paquete:** `infrastructure/bpm/scheduler/`
**Brecha:** El test valida la lógica del 80% pero no existe un scheduler que la ejecute en runtime.
**Acción:**
- Crear clase `SlaEarlyWarningScheduler` con `@Component` y `@Scheduled(fixedRate = 300000)` (cada 5 min).
- Inyectar `TaskService` y `ApplicationEventPublisher`.
- Iterar tareas activas con `dueDate != null`, calcular `% consumido = (now - createTime) / (dueDate - createTime)`.
- Si `>= 0.80 && < 1.0` y no está ya flaggeada → `taskService.setVariable(taskId, "isSlaAtRisk", true)`.
- Publicar un `SlaAtRiskEvent extends ApplicationEvent` como bisagra para US-049 (Motor de Notificaciones V2).

---

## ✅ Criterio de Aceptación Backend (DoD)
- [ ] `CustomBusinessCalendar` parsea `PT4H`, `P2D`, `4h` correctamente.
- [ ] `SlaService.recalculateActiveSlas()` invoca al Calendar real.
- [ ] `BusinessHoursEntity` tiene campo `timezone` persistido.
- [ ] CRUD completo: `GET/PUT /business-hours` + `DELETE /holidays/{id}`.
- [ ] `SlaEarlyWarningScheduler` detecta tareas al 80% y las flaggea.
- [ ] Compilación limpia (`mvn package -DskipTests`).
