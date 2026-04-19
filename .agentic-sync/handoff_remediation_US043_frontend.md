# Handoff de Remediación: US-043 (SLA & Business Calendar) — Agente Frontend

**Fecha:** 2026-04-19
**Emisor:** Arquitecto Líder (Auditoría Iteración 2)
**Destino:** Agente Frontend (Experto UI/UX Vue)
**Prioridad:** ALTA — Cierre de Iteración 2

---

## 📋 Contexto Ejecutivo

La `PmoSettings.vue` (Pantalla 19) está implementada visualmente al 90%. Tiene el formulario de Horas Hábiles (CA-3), el Toggle Retroactivo, el Modal HTTP 202 Anti-Deadlock, y el Grid de Feriados (CA-5). Sin embargo, opera en modo **local/mock** — no interactúa con los endpoints reales del Backend.

## 📍 Artefacto a Remediar
| Archivo | Ruta | Estado |
|---|---|---|
| `PmoSettings.vue` | `views/admin/PMO/PmoSettings.vue` | ⚠️ Desconectado del API |

---

## 🛠️ Directivas de Remediación

### TAREA 1 — CA-3: Corregir URL del endpoint de recálculo
**Línea 192 del `PmoSettings.vue`**
**Brecha:** La URL apunta a `'/api/v1/admin/pmo/sla/recalculate'` que **NO EXISTE** en el backend. El controller real es `SlaAdminController` en `/api/v1/admin/sla/apply`.
**Acción:**
```javascript
// ANTES (incorrecto):
const response = await apiClient.post('/api/v1/admin/pmo/sla/recalculate', slaForm.value);

// DESPUÉS (correcto — el baseURL del apiClient ya incluye /api/v1):
const response = await apiClient.post('/admin/sla/apply', null, { 
  params: { applyRetroactively: slaForm.value.applyRetroactive } 
});
```
**Nota:** El backend espera `applyRetroactively` como **query param**, NO como body JSON.

### TAREA 2 — CA-4: Hidratar formulario desde API en `onMounted`
**Brecha:** Los valores de `slaForm` (startHour, endHour, timezone) son estáticos en el componente. No se cargan desde la BD.
**Acción:**
- En `onMounted`, llamar `GET /admin/sla/business-hours` para hidratar `slaForm`.
- Mapear: `response.data.startTime` → `slaForm.startHour`, `response.data.endTime` → `slaForm.endHour`, `response.data.timezone` → `slaForm.timezone`.
- Agregar un botón o lógica de guardar los Business Hours con `PUT /admin/sla/business-hours` (enviar el body con `startTime`, `endTime`, `workOnWeekends`, `timezone`).

### TAREA 3 — CA-5: Conectar CRUD de Feriados al API real
**Brecha:** `holidays` es un `ref` con datos hardcodeados (`HOL1`, `HOL2`). `addHoliday()` y `removeHoliday()` solo mutan el estado local.
**Acción:**
- En `onMounted`, cargar feriados reales: `GET /admin/sla/holidays`.
- En `addHoliday()`:
  ```javascript
  const response = await apiClient.post('/admin/sla/holidays', {
    holidayDate: newHoliday.value.date,
    description: newHoliday.value.name
  });
  holidays.value.push({ id: response.data.id, date: response.data.holidayDate, name: response.data.description, scope: newHoliday.value.scope });
  ```
- En `removeHoliday(id)`:
  ```javascript
  await apiClient.delete(`/admin/sla/holidays/${id}`);
  holidays.value = holidays.value.filter(h => h.id !== id);
  ```

### TAREA 4 — CA-6: Badge `isSlaAtRisk` en Workdesk/Inbox
**Archivos:** `Workdesk.vue`, `InboxView.vue` 
**Brecha:** El DTO de tarea puede incluir `isSlaAtRisk: true` (inyectado por el `SlaEarlyWarningScheduler` del Backend). El frontend debe consumir este flag visualmente.
**Acción:**
- Verificar que el DTO de tarea que llega del API contenga `variables.isSlaAtRisk`.  
- En la fila/tarjeta de tarea, si `isSlaAtRisk === true`, agregar un badge/tooltip: `"⚠️ SLA en Riesgo (<20% restante)"` con estilo `bg-amber-500/text-white`.
- Las tareas ya vencidas (`dueDate < now`) deben mostrar badge rojo existente; `isSlaAtRisk` es el estado **previo** (naranja/amarillo warning).

---

## ✅ Criterio de Aceptación Frontend (DoD)
- [ ] `PmoSettings.vue` interactúa con endpoints reales del API (no mocks).
- [ ] Formulario de Business Hours se hidrata en `onMounted` y se persiste con `PUT`.
- [ ] Feriados se cargan, crean y eliminan vía API.
- [ ] El botón "Actualizar SLA" con toggle retroactivo llama a `/admin/sla/apply?applyRetroactively=true`.
- [ ] Badge `isSlaAtRisk` visible en Workdesk/Inbox.
- [ ] Compilación limpia (`npm run build`).
