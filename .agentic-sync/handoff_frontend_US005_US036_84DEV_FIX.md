# 🔧 HANDOFF FRONTEND — Iteración Correctiva 84-DEV-LANE-ROLE-FIX

> **Tipo:** Corrección de defectos post-auditoría PM-IA
> **Fecha de emisión:** 2026-07-14
> **Prioridad:** MEDIA

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE-FIX` |
| **US** | US-005 (Motor BPMN) + US-036 (RBAC) — Extensión Lane-Role Assignment |
| **CAs** | Corrección de defectos D-08 (tipos TS duplicados), D-09 (sin toasts de error) |
| **Rama Git** | `DevDavid` |
| **SSOT** | `docs/requirements/epics/epic_B_formularios_bpmn.md` (US-005) + `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (US-036) |
| **Secuencia** | Frontend (MC-2) en PARALELO con Backend (MC-1) ▸ Gobernanza (MC-3) |

> ⚠️ **CONTEXTO CRÍTICO:** Esta es una iteración CORRECTIVA. NO estás construyendo funcionalidad nueva. Estás corrigiendo 2 defectos detectados por auditoría forense en código YA EXISTENTE. El blast radius es ESTRICTO — solo puedes tocar los 2 archivos listados abajo. Cualquier cambio fuera de estos archivos será rechazado.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---

## 2. Alineación Arquitectónica

### ADRs Aplicables
| ADR | Impacto |
|-----|---------|
| ADR-002 (Vue 3) | Los toasts de error deben usar el sistema de notificaciones existente del componente (`showToast`). NO crear un sistema nuevo. |

### Stack Frontend Confirmado
- Vue 3 + Composition API + TypeScript
- Sistema de toasts: `showToast()` — **ya existe** dentro de `IdentityGovernance.vue`
- Tipos API: Generados por OpenAPI en `api-schema.d.ts` — las interfaces manuales duplicadas deben eliminarse

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivo 1: `IdentityGovernance.vue`
- **Ruta:** `frontend/src/views/admin/Security/IdentityGovernance.vue`
- **Estado actual:** Tiene 4 bloques `catch` relacionados con la API de lanes que solo hacen `console.error()` sin feedback al usuario:
  - **L1161-1163** (`toggleProcessExpand`): Catch al cargar lanes de un proceso → solo `console.error()`, sin toast
  - **L1191** (`openRoleModal`): Catch al cargar asignaciones lane-role → solo `console.error()`, inline
  - **L1210-1213** (`deleteRole`): **⚠️ PELIGROSO** — en el catch, ELIMINA EL ROL DE LA UI LOCAL y muestra toast de ÉXITO: `'Fallback local: Rol eliminado.'`. Esto oculta el fallo real de la API.
  - **L1268-1270** (`saveRole` inner): Catch al guardar asignaciones lane-role → solo `console.error()`. NOTA: El catch EXTERNO (L1274-1277) SÍ tiene toast de error — es solo el bloque interno el que falla.
- **Función `showToast` ya disponible:** Verificar la signature exacta leyendo el componente. Probablemente `showToast(message: string, type: 'success' | 'error' | 'warning')` o similar.

### Archivo 2: `api-schema.d.ts`
- **Ruta:** `frontend/src/types/api-schema.d.ts`
- **Estado actual:** Archivo grande (18565+ líneas), generado por OpenAPI con adiciones manuales al final.
  - **L3013-3029** (auto-generado, dentro de `components.schemas`): Definiciones de `BpmnLaneDTO` y `LaneRoleAssignmentDTO` con todos los campos **opcionales** (`?`).
  - **L18543-18565** (manual, al final del archivo): Definiciones DUPLICADAS de:
    - `BpmnLaneDTO` (L18544-18551) — campos **requeridos** (sin `?`) — **ELIMINAR**
    - `LaneRoleAssignmentDTO` (L18553-18559) — campos **requeridos** (sin `?`) — **ELIMINAR**
    - `LaneRoleAssignmentRequest` (L18561-18565) — **CONSERVAR** (no tiene contraparte auto-generada)
- **DECISIÓN ARQUITECTÓNICA DA-04:** Eliminar solo `BpmnLaneDTO` y `LaneRoleAssignmentDTO` manuales. Conservar `LaneRoleAssignmentRequest`.

---

## 4. Correcciones Prescriptivas

### D-09: Agregar toasts de error en catch blocks de API lanes

**CORRECCIÓN 1 — `toggleProcessExpand` (L1161-1163):**

Buscar el bloque catch existente:
```typescript
} catch (error) {
  console.error('Error loading lanes:', error);
}
```

Reemplazar con:
```typescript
} catch (error: any) {
  console.error('Error loading lanes:', error);
  showToast('Error al cargar los carriles del proceso: ' + (error?.response?.data?.message || error.message || 'Error desconocido'), 'error');
}
```

**CORRECCIÓN 2 — `openRoleModal` (L1191):**

Buscar el catch inline y expandirlo para incluir toast:
```typescript
} catch (error: any) {
  console.error('Error loading lane assignments:', error);
  showToast('Error al cargar asignaciones de carriles: ' + (error?.response?.data?.message || error.message || 'Error desconocido'), 'error');
}
```

**CORRECCIÓN 3 — `deleteRole` (L1210-1213) — CORRECCIÓN CRÍTICA:**

Este es el caso más peligroso: el catch actual OCULTA el error y muestra éxito falso.

Buscar el bloque actual:
```typescript
} catch (error) {
  console.error('Error deleting role from API:', error);
  // Fallback: eliminar del estado local
  roles.value = roles.value.filter(r => r.id !== roleId);
  showToast('Fallback local: Rol eliminado.', 'success');
}
```

Reemplazar con:
```typescript
} catch (error: any) {
  console.error('Error deleting role from API:', error);
  showToast('Error al eliminar el rol: ' + (error?.response?.data?.message || error.message || 'Error desconocido'), 'error');
}
```

> ⚠️ **ELIMINAR** el fallback local (`roles.value = roles.value.filter(...)`) — si la API falla, el rol NO debe desaparecer de la UI. Eso es mentirle al usuario.

**CORRECCIÓN 4 — `saveRole` inner catch (L1268-1270):**

Buscar:
```typescript
} catch (error) {
  console.error('Error saving lane assignments:', error);
}
```

Reemplazar con:
```typescript
} catch (error: any) {
  console.error('Error saving lane assignments:', error);
  showToast('Error al guardar asignaciones de carriles: ' + (error?.response?.data?.message || error.message || 'Error desconocido'), 'error');
}
```

---

### D-08: Eliminar tipos TS duplicados en `api-schema.d.ts`

**Acción:** Ir al FINAL del archivo (aprox. L18543-18565) y:

1. **ELIMINAR** la interfaz `BpmnLaneDTO` manual (aprox. L18544-18551):
```typescript
// ELIMINAR ESTO:
export interface BpmnLaneDTO {
  id: string;
  processKey: string;
  laneName: string;
  laneId: string;
  processDesignId: string;
  assignedRoles?: string[];
}
```

2. **ELIMINAR** la interfaz `LaneRoleAssignmentDTO` manual (aprox. L18553-18559):
```typescript
// ELIMINAR ESTO:
export interface LaneRoleAssignmentDTO {
  laneId: string;
  laneName: string;
  roleId: string;
  canInitiate: boolean;
  canExecute: boolean;
}
```

3. **CONSERVAR** `LaneRoleAssignmentRequest` (L18561-18565) — este tipo NO tiene contraparte auto-generada:
```typescript
// CONSERVAR ESTO:
export interface LaneRoleAssignmentRequest {
  laneId: string;
  canInitiate: boolean;
  canExecute: boolean;
}
```

4. **VERIFICAR** que el código frontend que importa `BpmnLaneDTO` o `LaneRoleAssignmentDTO` ahora use las definiciones de `components["schemas"]["BpmnLaneDTO"]` (dentro del namespace auto-generado). Si hay imports directos de las interfaces eliminadas, actualizarlos para que referencien el tipo correcto dentro de `components.schemas`.

---

## 5. Matriz de Verificación

| # | Defecto | Verificación | Método |
|---|---------|-------------|--------|
| D-09.1 | Toast en toggleProcessExpand | Simular error de red (desconectar backend) → expandir proceso → toast rojo visible | Manual / DevTools |
| D-09.2 | Toast en openRoleModal | Simular error → abrir modal de rol → toast rojo visible | Manual / DevTools |
| D-09.3 | deleteRole no miente | Simular error API → intentar eliminar rol → toast rojo Y rol PERMANECE en la lista | Manual / DevTools |
| D-09.4 | Toast en saveRole | Simular error → guardar asignaciones → toast rojo visible | Manual / DevTools |
| D-08 | Solo 1 definición por tipo | `grep -c "BpmnLaneDTO" api-schema.d.ts` → 1 bloque (auto-generado) | Terminal |
| BUILD | npm run build exitoso | Sin errores TypeScript de tipos faltantes o incompatibles | Terminal |

---

## 6. Instrucciones Operativas y de Comunicación

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.
