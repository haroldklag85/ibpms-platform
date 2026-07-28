# 🎨 Handoff Frontend — US-017 STABILIZE (Sprint PM-01, Slot 5)

> **Fecha**: 2026-06-09  
> **Sprint**: PM-01 | **Slot**: 5 | **Cadena**: 2 — Core Workdesk  
> **US**: US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)  
> **Branch de trabajo**: `sprint-8/pm-01/us-017-stabilize`  
> **Rol**: Desarrollador Frontend  
> **Prerequisitos**: ✅ Backend DEBE haber hecho push a la rama antes de iniciar  
> **Dependencia**: Handoff Backend US-017 completado  

---

## Pre-Handoff Checklist — US-017

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | US en sprint actual (Roadmap PM-IA) | ✅ | Sprint PM-01, Cadena 2 |
| 2 | Endpoints definidos en API_CONTRACTS.md | ✅ | Sección 5.10 — Contratos CQRS |
| 3 | Prerrequisitos completados | ✅ | Backend estabilizado y pusheado |
| 4 | Matriz de cobertura actualizada | ✅ | Backend resolvió conflictos |

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

## ⚠️ POLÍTICA ANTIAMNESIA — RE-ENTRENAMIENTO OBLIGATORIO

Antes de escribir UNA SOLA línea de código, DEBES re-entrenar tu contexto leyendo:
1. **Arquitectura Core:** `docs/architecture/arquitecturar.md`
2. **Negocio US-017 (CAs Frontend):** `docs/requirements/epics/epic_A_motor_core.md` (líneas 1236-1282, CAs CA-19 a CA-26)
3. **Gobernanza PM-IA:** `docs/sprints/gobernanza_pm/GUIA_ARQUITECTO_LIDER.md`

---

## 1. CONTEXTO Y OBJETIVO

US-017 tiene **8 CAs de Frontend** (CA-19 a CA-26) que gobiernan el **Toast flotante de estado de conexión** — un componente UX que comunica al usuario el estado de sincronización CQRS sin usar jerga técnica.

**PROBLEMA DETECTADO**: Existen **DOS componentes Toast activos simultáneamente**, causando potencial renderizado duplicado:
- `src/components/common/ConnectionToast.vue` — Importado en `App.vue`
- `src/components/common/CQRSConnectionToast.vue` — Importado en `MainLayout.vue`

**TU MISIÓN**: Unificar los componentes duplicados, verificar que los 8 CAs Frontend funcionan, eliminar artifacts obsoletos, y asegurar que el build pasa sin errores y sin romper Workdesk ni Kanban.

---

## 2. TAREAS ORDENADAS POR PRIORIDAD

### TAREA 1 (P0): Sincronizar con Branch del Backend

```bash
git checkout sprint-8/pm-01/us-017-stabilize
git pull origin sprint-8/pm-01/us-017-stabilize
```

Verifica que los cambios del Backend están presentes antes de continuar.

---

### TAREA 2 (P0): Unificar Componentes Toast Duplicados

**Archivos involucrados:**

| Archivo | Tamaño | Importado en | Acción |
|---------|--------|-------------|--------|
| `src/components/common/ConnectionToast.vue` | Principal | `App.vue` | ✅ MANTENER — Este es el componente canónico |
| `src/components/common/CQRSConnectionToast.vue` | 1,365 bytes | `MainLayout.vue` | 🔴 ELIMINAR o CONSOLIDAR |

**Acciones:**
1. **Comparar** ambos componentes. Identificar si `CQRSConnectionToast.vue` tiene funcionalidad que `ConnectionToast.vue` no tiene.
2. **Si son redundantes**: Eliminar `CQRSConnectionToast.vue` y remover su import de `MainLayout.vue`.
3. **Si CQRSConnectionToast tiene lógica única**: Mergear esa lógica en `ConnectionToast.vue` y luego eliminar el duplicado.
4. **Verificar**: Solo DEBE existir UNA instancia del Toast en el DOM. Verificar que `App.vue` renderiza el Toast UNA sola vez.
5. **Verificar `NetworkRetryModal.vue`**: Está marcado como DEPRECATED — confirmar que no se usa en ningún lado y documentar.

---

### TAREA 3 (P1): Verificar los 8 CAs Frontend (CA-19 a CA-26)

| CA | Requisito | Qué Verificar | Archivos |
|----|-----------|---------------|----------|
| CA-19 | Debounce visual 5s — sincronizaciones < 5s son invisibles | ¿El toast solo aparece si la latencia supera 5 segundos? | `ConnectionToast.vue`, `connectionStore.ts` |
| CA-20 | Toast en esquina inferior izquierda | ¿Posición CSS correcta? ¿No colisiona con acciones críticas? | `ConnectionToast.vue` |
| CA-21 | Lenguaje de negocio (PROHIBIDA jerga: CQRS, STOMP, etc.) | ¿Los textos dicen "Guardando cambios...", "Sin conexión", etc.? | `ConnectionToast.vue` |
| CA-22 | No-bloqueante — usuario puede seguir interactuando | ¿El toast NO bloquea la UI? ¿No usa modales? | `ConnectionToast.vue` |
| CA-23 | Transición a Modo Degradado con icono 🔴/🟡 | ¿Se muestra indicador visual de modo degradado? | `connectionStore.ts` |
| CA-24 | Reconexión silenciosa en background (sin botón "Reintentar") | ¿Al volver la red, se sincroniza automáticamente? | `connectionStore.ts`, `useConnectionStatus.ts` |
| CA-25 | Feedback positivo verde 3s y desvanecimiento | ¿Tras reconexión, toast verde por 3s y luego desaparece? | `ConnectionToast.vue` |
| CA-26 | Error transaccional > toast de conexión (prioridad) | ¿Los errores HTTP 4xx/500 ocultan el toast de conexión? | `ConnectionToast.vue` |

**Verificación con tests existentes:**
- `src/tests/components/ConnectionToast.spec.ts` — ¿Pasa? ¿Verifica CA-21 (no jerga)?
- `src/tests/stores/connectionStore.spec.ts` — ¿Pasa?
- `src/tests/composables/useConnectionStatus.spec.ts` — ¿Pasa?

---

### TAREA 4 (P1): Verificar que Workdesk y Kanban NO tienen Regresiones

**Verificaciones críticas:**

1. **Workdesk.vue**:
   - ¿El panel CQRS status (`data-testid="cqrs-status"`) sigue funcional?
   - ¿Las tareas se cargan desde la API real (NO mock)?
   - ¿El claim/unclaim funciona? (integración con US-002)

2. **KanbanView.vue** (US-008 Slot 4):
   - ¿Las tarjetas Kanban se muestran con datos reales?
   - ¿Las operaciones de drag-drop funcionan?
   - ¿NO se usa `setTimeout` simulado ni datos hardcodeados?

3. **Stores Pinia afectados:**
   - `useWorkdeskStore.ts` — ¿Consume endpoints reales?
   - `connectionStore.ts` — ¿Sin conflictos con otros stores?

---

### TAREA 5 (P2): Limpieza de Artifacts Obsoletos

1. **Eliminar `frontend/out.txt`**: Es un build log antiguo que contiene evidencia de un conflicto de merge ya resuelto en `authStore.ts`. No tiene valor y ensucia el repositorio.

2. **Verificar `NetworkRetryModal.vue`**: Si no se usa en ningún import, agregar un comentario `@deprecated` o eliminarlo.

---

### TAREA 6 (P2): Actualizar Coverage Matrix (Frontend)

Actualizar `.agentic-sync/coverage_matrix.md` sección US-017:
- Marcar CA-19 a CA-26 con su estado real verificado
- Incluir commit hash
- Fecha de verificación

---

## 3. CONTRATOS API QUE CONSUMIR

Referencia: `docs/sprints/gobernanza_pm/API_CONTRACTS.md`, sección 5.10

| Endpoint | Método | Store/Composable que lo consume |
|----------|--------|---------------------------------|
| `/api/v1/workbox/tasks/{taskId}/complete` | POST | `useFormStore.ts` |
| `/api/v1/workbox/tasks/{taskId}/draft` | GET | `useFormStore.ts` |
| `/api/v1/workbox/tasks/{taskId}/draft` | PUT | `useFormStore.ts` (autoguardado) |

---

## ⚠️ IMPORTANTE

Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes (Workdesk, Kanban, Claim) será motivo de rechazo inmediato.

---

## 📋 Build obligatorio

Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
