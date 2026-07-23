# Handoff de Corrección Post-Auditoría — US-043 Backend (Cleanup)

**Fecha:** 2026-04-19
**Emisor:** Arquitecto Líder (Auditoría Post-Ejecución)
**Destino:** Agente Backend
**Prioridad:** MEDIA — Cleanup pre-merge
**Referencia:** `audit_US043_backend.md`

---

## Contexto

La ejecución del handoff principal fue aprobada (5/5 tareas, 4/4 observaciones). Este micro-handoff corrige **4 issues de higiene** detectados en la auditoría para garantizar la calidad del merge a `main`.

---

## ISSUE-1: Eliminar `SlaAtRiskEvent` duplicada (P1)

**Problema:** Existen dos clases con el mismo nombre en paquetes distintos:
- `infrastructure/bpm/scheduler/SlaAtRiskEvent.java` — ✅ Correcta (extends `ApplicationEvent`, usada por el Scheduler)
- `application/event/SlaAtRiskEvent.java` — ❌ Código muerto (POJO plano, nadie la importa)

**Acción:** Eliminar el archivo `application/event/SlaAtRiskEvent.java`.

**Verificación:** `grep -r "application.event.SlaAtRiskEvent" backend/` no debe devolver resultados.

---

## ISSUE-2: Eliminar import `Duration` no utilizado en `SlaService` (P2)

**Archivo:** `application/service/bpm/SlaService.java`
**Línea 12:** `import java.time.Duration;`

**Problema:** El import está presente pero `Duration` no se usa en ninguna parte del cuerpo de la clase.

**Acción:** Eliminar la línea 12.

---

## ISSUE-3: Actualizar comentarios PoC obsoletos en `CustomBusinessCalendar` (P2)

**Archivo:** `infrastructure/bpm/calendar/CustomBusinessCalendar.java`
**Líneas 49-52:**
```java
// Fallback/Sobrecarga nativa en Camunda 7.20+ requiere a veces parametros extra pero estos 2 son obligatorios.
// Camunda parsea internamente ISO-8601, pero en nuestra arquitectura abstraemos la resolución
// Puesto que es una Prueba de Concepto (PoC) implementaremos un warp simbólico asumiendo +N horas de SLA
// Aquí interceptamos el Time-Warp.
```

**Problema:** Estos comentarios describen el stub original de 4 horas hardcodeado. Ahora el código parsea ISO 8601 realmente, por lo que los comentarios son engañosos.

**Acción:** Reemplazar los 4 comentarios por un Javadoc actualizado:
```java
/**
 * CA-1: Resolución principal de Due Dates con soporte ISO 8601.
 * CA-2: Bypass para timers sistémicos (SYSTEMIC_24_7).
 * CA-4: Resolución de timezone desde config corporativa persistida.
 * Soporta formatos: PT4H, P2D, Xh, número suelto. Fallback: 4h default.
 */
```

---

## ISSUE-4: Documentar falta de paginación en `SlaEarlyWarningScheduler` (P2)

**Archivo:** `infrastructure/bpm/scheduler/SlaEarlyWarningScheduler.java`
**Líneas 31-33:**
```java
List<Task> activeTasks = taskService.createTaskQuery()
        .active()
        .list();
```

**Problema:** `.list()` sin paginación carga todas las tareas activas en memoria. En producción con miles de instancias podría causar OOM.

**Acción (mínima V1):** Agregar un comentario `// TODO V2: Implementar paginación .listPage() para entornos con alto volumen de tareas activas` encima de la query. No se requiere refactorizar para V1 PoC.

---

## Criterio de Cierre

- [ ] `application/event/SlaAtRiskEvent.java` eliminado.
- [ ] Import `java.time.Duration` removido de `SlaService.java`.
- [ ] Javadoc del método `resolveDuedate(String, Date, long)` actualizado en `CustomBusinessCalendar.java`.
- [ ] Comentario `TODO V2` añadido en `SlaEarlyWarningScheduler.java`.
- [ ] Compilación limpia `mvn clean package -DskipTests`.

**Tiempo estimado:** < 10 minutos. Proceda de inmediato.
