# 🧠→🎨 Handoff: Arquitecto Líder → Frontend Vue
# T-21: Retro-Remediación ADR-006 (Smart Components y Fuga de Timers)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND - VUE
**Fecha:** 2026-05-12T19:30:00-05:00
**Sprint:** 7 — Iteración de Consolidación
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/ADR-006_Dumb_Components.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: Retro-Remediación ADR-006 CA-11`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

La Auditoría V2 dinámica reveló dos anti-patrones críticos que degradan el estado arquitectónico del Frontend:
1. **Fuga de Lógica de Red a la Vista (Smart Components):** 9 componentes visuales importan `apiClient` directamente y gestionan peticiones HTTP (GET, POST), rompiendo el aislamiento.
2. **Fugas de Timers y DOM-Thrashing (Regresión CA-11):** A pesar de centralizar el tiempo en `timeStore.currentTick`, varios módulos (`BpmnDesigner.vue`, `IntakeTriageView.vue`, `useFormStore.ts`) continúan utilizando `setInterval` nativo, creando timers zombies y bloqueos visuales.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Lógica de Red Directa | `IdentityGovernance.vue` | Múltiples `apiClient.post/put/delete` quemados. |
| Lógica de Red Directa | `RbacManagerView.vue`, `GlobalRolesTable.vue` | Invocaciones HTTP atadas a botones sin pasar por Pinia. |
| Lógica de Red Directa | `InstancesManager.vue`, `DlqDashboard.vue` | Peticiones HTTP en hooks `onMounted`. |
| Uso Ilegal de Timers | `BpmnDesigner.vue` | `heartbeatInterval` y `autoSaveInterval` usan `setInterval`. |
| Uso Ilegal de Timers | `IntakeTriageView.vue` | `pollingInterval` usado para consultar tareas crónicamente. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Centralización de Peticiones HTTP en Stores/Composables

**Archivo:** Todos los componentes `.vue` infectados con `import apiClient from '@/services/apiClient'`.
**Acción:** Eliminar la importación de `apiClient`. Mover la función asíncrona a un Pinia Store (ej. `useRbacStore`, `useIntegrationStore`) y consumir la acción desde el componente.

```typescript
// Snippet prescriptivo — Pinia Store (Ejemplo)
// @Traceability: Retro-Remediación ADR-006 - Migración de llamadas de red a Store
actions: {
  async killUserSession(userId: string) {
    await apiClient.post(`/kill-session`);
    // mutar el estado local aquí
  }
}
```

### Paso 2: Erradicación de `setInterval` (Integración con `useTimeStore`)

**Archivos:** `BpmnDesigner.vue`, `IntakeTriageView.vue`, `useFormStore.ts`.
**Acción:** Sustituir `setInterval` por lógica atada a `requestAnimationFrame` o, si es un caso de polling severo que no necesita reactividad UI hiper-rápida, considerar delegar el ciclo al `timeStore.currentTick` con un divisor lógico, o migrar a SSE/WebSockets de estar disponible.

```typescript
// Snippet prescriptivo — BpmnDesigner.vue
// @Traceability: Retro-Remediación CA-11 - Purgado de setInterval Zombie
import { useTimeStore } from '@/stores/timeStore';
const timeStore = useTimeStore();

// INCORRECTO: autoSaveInterval = setInterval(...)
// CORRECTO: Observar el latido global (cada 60 segundos por ejemplo)
watch(() => timeStore.currentTick, (newTick) => {
    if (newTick % 60000 < 1000) { // Disparo aproximao 1 vez por minuto
        executeAutoSave();
    }
});
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Ningún componente Vue consume `apiClient` | `grep -r "import.*apiClient" frontend/src/views/*.vue frontend/src/components/*.vue` devuelve 0 resultados. |
| 2 | Componentes purgados de `setInterval` | `grep -r "setInterval" frontend/src/views/*.vue` devuelve 0 resultados. |
| 3 | Trazabilidad Inversa Inyectada | Cada archivo modificado incluye el marcador `// @Traceability: Retro-Remediación ADR-006`. |
| 4 | Build Exitoso | `npm run build` en la carpeta `frontend` termina sin errores de TypeScript. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Aislar los componentes Vue que importan `apiClient`.
2. Mover la lógica asíncrona a los correspondientes Stores de Pinia.
3. Remplazar los `setInterval` con watchers sobre `timeStore.currentTick`.
4. Verificar inyección de Trazabilidad.
5. Ejecutar compilación: `npm run build` (en `frontend`).
6. Commit: `git add . && git commit -m "refactor(frontend): purgar smart components y timers zombies (ADR-006/CA-11)" && git push`
