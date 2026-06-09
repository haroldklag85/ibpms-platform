# Aprobación Arquitectónica — Plan de Remediación US-043 (Backend)

**Fecha:** 2026-04-19
**Emisor:** Arquitecto Líder
**Destino:** Agente Backend (Conversación `64170f2c-420a-4ec0-999d-e6e8d7c4f63f`)
**Veredicto:** 🟢 **APROBADO CON OBSERVACIONES**

---

## ✅ Decisión

El plan de implementación presentado en `implementation_plan.md` está **aprobado para ejecución inmediata**, cubriendo correctamente las 5 tareas delegadas en el handoff `handoff_remediation_US043_backend.md`.

---

## ⚠️ Observaciones Obligatorias (incorporar antes de cerrar)

### OBS-1: Referencia a `ACT_RU_TASK` (Línea 52 del plan)
**Contexto:** El plan menciona *"Identificará en la base `ACT_RU_TASK` las tareas..."* al describir el `SlaEarlyWarningScheduler`.
**Directiva:** No utilizar SQL directo ni referenciar tablas internas de Camunda. Utilizar exclusivamente la API Java `TaskService.createTaskQuery()` para localizar las tareas activas. Actualizar cualquier comentario/javadoc para que refiera a "API de TaskService" en vez de la tabla directa. Esto preserva la abstracción y evita crear dependencia con el esquema interno del motor.

### OBS-2: Resolución de `P2D` (Días Hábiles)
**Contexto:** Open Question del plan — *"¿1 día = jornada hábil o 24h naturales?"*
**Respuesta confirmada:** `1 día (P1D) = 1 jornada hábil completa` (el tamaño configurado en `BusinessHoursEntity`, ej: 9h si es 8:00-17:00). `P2D` equivale a 2 jornadas hábiles. El parser debe convertir los días a horas naturales vía `Duration.parse().toHours()` y luego **el while-loop del Calendar se encarga de que solo descuente horas dentro del horario hábil**. Este enfoque ya funciona implícitamente con la lógica existente. No requiere lógica adicional de conversión.

### OBS-3: `@Bean` de `CustomBusinessCalendar`
**Contexto:** Pregunta del plan — *"¿Estás de acuerdo con exportar vía @Bean?"*
**Respuesta confirmada:** Sí, aprobado. El `@Bean` en `CamundaEngineConfiguration` es seguro en Camunda 7 embebido y permite reutilizar la misma instancia tanto en el Engine como en `SlaService`, sin duplicar lógica.

### OBS-4: `@SchedulerLock` (ShedLock)
**Contexto:** Nota del plan sobre concurrencia multi-nodo.
**Respuesta confirmada:** Para V1 PoC (single-pod), `@Scheduled` simple es suficiente. La integración con ShedLock se difiere al Roadmap V2 cuando se despliegue en cluster multi-réplica.

---

## 📋 Checklist de Cierre Post-Ejecución

El agente Backend debe confirmar al finalizar:

- [ ] `BusinessHoursEntity` tiene campo `timezone` + getter/setter.
- [ ] `CustomBusinessCalendar` parsea ISO 8601 (`PT4H`, `P2D`, `4h`, fallback).
- [ ] `CustomBusinessCalendar` usa `ZoneId.of(config.getTimezone())`.
- [ ] `CamundaEngineConfiguration` expone `@Bean customBusinessCalendar()`.
- [ ] `SlaService` inyecta `CustomBusinessCalendar` y recalcula con lógica real.
- [ ] `SlaAdminController` tiene CRUD completo (`GET/PUT /business-hours`, `DELETE /holidays/{id}`).
- [ ] `SlaEarlyWarningScheduler` creado con `@Scheduled(fixedRate=300000)` + `SlaAtRiskEvent`.
- [ ] Sin referencias directas a tablas SQL (`ACT_RU_*`) en código ni Javadoc (OBS-1).
- [ ] Compilación limpia `mvn clean package -DskipTests`.

---

**Proceda con la ejecución. Reporte al completar.**
