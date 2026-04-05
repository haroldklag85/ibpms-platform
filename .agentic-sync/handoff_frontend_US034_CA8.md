# 📦 Handoff Frontend — US-034 / CA-8 (GAP DLQ Dashboard)
## Remediación del Dashboard DLQ: De Mocks a API Real

| Metadato | Valor |
|---|---|
| **Emisor** | Arquitecto Líder (Orquestador) |
| **Destino** | Agente Desarrollador Frontend (Vue 3 / TypeScript) |
| **Fecha** | 2026-04-05 |
| **Rama de trabajo** | `sprint-3/informe_auditoriaSprint1y2` |
| **Prioridad** | 🟡 Alta |
| **GAP de origen** | REM-034-05 (CA-8 Dashboard DLQ) |
| **Iteración** | 70-DEV |

---

## 1. Contexto del Problema

El CA-8 exige que el Dashboard DLQ sea una **pantalla custom del iBPMS** (componente Vue), NO un enlace al Management UI de RabbitMQ.

**Lo que YA existe:**
- ✅ `DlqDashboard.vue` en `views/admin/Integration/` — Componente funcional con UI premium (cards, datagrid, acciones por fila).
- ✅ Interfaz TypeScript `DLQMessage` con campos: `id`, `exchange`, `routingKey`, `errorReason`, `retries`, `timestamp`, `payload`.
- ✅ Botones: `Purgar Todo`, `Reencolar Todo`, inspección individual de payload.

**Lo que FALTA (tu responsabilidad para el GAP-88 de CA-8):**
- ❌ El componente usa **datos MOCK hardcodeados** en el catch del `apiClient.get`. Debe consumir la API real.
- ❌ Falta el **agrupamiento por cola de origen** (`x-original-queue`) en las métricas.
- ❌ Falta el **modal de confirmación** antes de Purgar (con Sudo-Mode: justificación 20+ chars).
- ❌ Falta el **modal de confirmación** antes de Reencolar ("Se reintentarán N mensajes. Los Workers deben ser idempotentes CA-5").
- ❌ Falta registrar acciones en `ibpms_audit_log` (esto lo hace el Backend, pero el Frontend debe enviar los params).
- ❌ El campo `Warning Rate` muestra "12%" hardcodeado. Debe calcularse del summary de la API.
- ❌ El campo `Reintentos Hoy` muestra "342" hardcodeado. Debe venir de la API.

---

## 2. Especificación Técnica Exacta

### 2.1. Contracto API que consumirás (creado por Backend)

| Método | Endpoint | Respuesta esperada |
|---|---|---|
| `GET` | `/api/v1/admin/queues/dlq/summary` | `{ totalMessages: number, groupedByQueue: { [queueName: string]: number }, oldestMessageTimestamp: string, retriesToday: number }` |
| `GET` | `/api/v1/admin/queues/dlq/messages?page=1&size=50` | `DLQMessage[]` con paginación |
| `POST` | `/api/v1/admin/queues/dlq/retry` | `{ retriedCount: number }` |
| `DELETE` | `/api/v1/admin/queues/dlq/purge` | Requiere body: `{ justification: string }` (min 20 chars) |

### 2.2. Cambios en `DlqDashboard.vue`

1. **Elimina los datos MOCK del catch.** Reemplaza con un toast de error real (`"Error de conexión con el servicio de DLQ"`).

2. **Carga las métricas del summary** (nueva función `fetchSummary`):
   - `totalMessages` → card "Mensajes Atascados"
   - Calcular `warningRate` = `(totalMessages > 100) ? 'CRÍTICO' : (totalMessages > 20) ? 'Warning' : 'Normal'`
   - `retriesToday` → card "Reintentos Hoy"

3. **Modal de confirmación para Purga:**
   ```html
   <!-- Modal con textarea obligatorio de justificación (20+ chars) -->
   <textarea v-model="purgeJustification" placeholder="Justifique la purga (mínimo 20 caracteres)..." />
   <button :disabled="purgeJustification.length < 20" @click="executePurge">Confirmar Purga</button>
   ```
   El `DELETE` envía `{ justification: purgeJustification }`.

4. **Modal de confirmación para Reintentar:**
   Mostrar: `"Se reintentarán ${totalMessages} mensajes. Los Workers deben ser idempotentes (CA-5). ¿Continuar?"`

5. **Restricción de acceso:** El componente debe estar protegido en el router de Vue para el rol `ADMIN_IT` solamente. Verificar que la ruta en `router/index.ts` tenga `meta: { requiredRole: 'ADMIN_IT' }`.

---

## 3. Contrato de No-Regresión

> [!CAUTION]
> - **PROHIBIDO** modificar archivos de Backend (Java).
> - **PROHIBIDO** eliminar el componente `DlqDashboard.vue`. Solo refactorizarlo.
> - **PROHIBIDO** usar `alert()` nativo. Reemplazar el `inspectMsg` con un modal Vue adecuado.
> - **PROHIBIDO** inventar endpoints que no estén en el contrato anterior. Si el Backend aún no los tiene, usa un `try/catch` con mensaje amigable indicando "API no disponible aún" (no mocks ficticios).

---

## 4. Criterio de Aceptación Técnico (Definition of Done)

- [ ] Datos MOCK eliminados de `DlqDashboard.vue`.
- [ ] Endpoint `GET /api/v1/admin/queues/dlq/summary` consumido para métricas reales.
- [ ] Modal de confirmación con justificación (20+ chars) para Purga.
- [ ] Modal de confirmación con advertencia de idempotencia para Reintentar.
- [ ] `alert()` nativo reemplazado por modal Vue para inspección de payload.
- [ ] Ruta protegida con `requiredRole: 'ADMIN_IT'`.
- [ ] Compilación verde (`npm run build`).

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza empaquetando obligatoriamente con `git stash save "temp-frontend-US034-CA8"`.
