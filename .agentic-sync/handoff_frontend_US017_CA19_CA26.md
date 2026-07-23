# 🛠️ HANDOFF FRONTEND — US-017 CA-19 a CA-26
# Monitoreo de Conexión No Intrusivo (Toast Flotante)

> **De:** Arquitecto Líder (Orquestador)
> **Para:** Agente Frontend (Desarrollador Vue 3 / Pinia / CSS)
> **Fecha:** 2026-04-22
> **Iteración:** Cierre Deuda Técnica Sprint 6.2
> **Rama Git:** `sprint-6/uat-certification`
> **US:** US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)
> **CAs:** CA-19, CA-20, CA-21, CA-22, CA-23, CA-24, CA-25, CA-26
> **Épica:** A — Motor Core, Orquestación & Persistencia
> **Fuente SSOT:** `docs/requirements/epics/epic_A_motor_core.md` (Líneas 1229–1281, Sección E)
> **Handoff Origen PO:** `.agentic-sync/handoff_frontend_US017_CA19_CA26.md` (PO → Arquitecto)

---

## 1. Contexto y Objetivo

El dashboard del Workdesk expone actualmente un componente técnico fijo titulado _"CQRS Engine / Sync Eventual"_ que viola las heurísticas de simplicidad (Ley de Hick) y expone jerga arquitectónica al usuario de negocio.

Se requiere implementar un **componente Toast Flotante no intrusivo** que reemplace esa exposición con semántica de negocio pura, cubriendo 8 CAs (CA-19 a CA-26) que formalizan:
- Detección de desconexión con debounce de 5s
- Toast flotante en esquina inferior izquierda
- Textos orientados a negocio (prohibida jerga técnica)
- Operatividad pasiva durante desconexión
- Transición a modo degradado
- Reconexión silenciosa en background
- Feedback positivo con desvanecimiento automático (3s)
- Prevención de colisiones visuales con ErrorStateGlobal

**Responsabilidad:** 100% Frontend. **Backend: CERO cambios.**

---

## 2. Alineación Arquitectónica

### ADRs Consultados
| ADR | Impacto |
|-----|---------|
| `adr-002-vue3-microfrontends.md` | Componente Vue 3 SFC con Pinia para estado. Composable para lógica reactiva. |
| `adr_010_testing_pyramid_governance.md` | Test Vitest obligatorio para el store (transiciones de estado). |

### Stack Confirmado
- **Vue 3** + Composition API (`<script setup>`)
- **Pinia** para `connectionStore.ts`
- **CSS puro** (transiciones, animations, z-index)
- **Vitest** para tests unitarios del store

### Riesgos Arquitectónicos
- **Ninguno identificado.** Tarea 100% de presentación sin contratos REST nuevos.

---

## 3. Especificación Técnica por CA

### CA-19: Monitoreo Asíncrono No Intrusivo (Debounce Visual 5s)
**Artefactos:** `useConnectionStatus.ts` [NUEVO], `connectionStore.ts` [NUEVO]

- Implementar composable que escuche `window.addEventListener('offline'/'online')`.
- Debounce de 5 segundos: si la desconexión dura <5s, el usuario NUNCA ve nada.
- Si supera 5s, activar el Toast (CA-20).

### CA-20: Anatomía y Posicionamiento del Toast Flotante
**Artefactos:** `ConnectionToast.vue` [NUEVO]

- Esquina inferior izquierda: `position: fixed; bottom: 1.5rem; left: 1.5rem;`
- `z-index: 9990` (debajo de ErrorStateGlobal z-9998, encima del contenido)
- `max-width: 320px`
- NO colisionar con botón de Fuga (Cerrar Sesión) que está en `bottom-4 right-4 z-[10001]`

### CA-21: Lenguaje Orientado a Negocio (Prohibición de Jerga)
| Estado Interno | Texto Visible | Ícono | Color |
|---------------|--------------|-------|-------|
| `ONLINE` | _(invisible)_ | — | — |
| `OFFLINE` | "Trabajando sin conexión" | `wifi_off` | 🔴 Rojo suave |
| `RECONNECTING` | "Reconectando..." | spinner | 🟡 Ámbar |
| `DEGRADED` | "Modo sin conexión — los cambios se guardarán localmente" | `cloud_off` | 🟠 Naranja |
| `RESTORED` | "Conexión restaurada" | `check_circle` | 🟢 Verde |

**PROHIBIDO:** `CQRS`, `STOMP`, `Event Sourcing`, `WebSocket`, `Sync Eventual`, `Engine`.

### CA-22: Interfaz Cinética y Operatividad Pasiva en Desconexión
- Toast SERÁ NO-BLOQUEANTE (`pointer-events: auto` solo en el Toast, no overlay full-screen).
- El usuario puede continuar interactuando con modales abiertos, copiar texto, desplazarse.

### CA-23: Transición Predictiva a Modo Degradado
- Si la desconexión persiste >15s, el Toast muta a estado `DEGRADED`.
- Indicar que los datos se guardarán en borrador local (LocalStorage de US-029).

### CA-24: Reconexión Silenciosa en Background
- Al detectar `online`, el composable ejecutará sincronización pendiente automáticamente.
- NO exigir botones de "Reintentar" ni modales bloqueantes.

### CA-25: Feedback Positivo y Desvanecimiento de Éxito (3s)
- Ciclo: OFFLINE (>5s) → Toast aparece → Red vuelve → RECONNECTING (1-3s) → RESTORED (verde, 3s) → Fade-out CSS (500ms) → v-if=false

### CA-26: Prevención Contra Colisiones Visuales en Error Fuerte
- Jerarquía z-index:
  ```
  z-[10001] — Botón de Fuga (Logout)
  z-[9999]  — Skeleton Loader
  z-[9998]  — NotFound404 / ErrorStateGlobal
  z-[9990]  — ConnectionToast ← NUEVO
  z-[100]   — NetworkRetryModal (DEPRECAR)
  ```
- Si `ErrorStateGlobal` está visible, el `ConnectionToast` entra en estado `SILENCED` (no se renderiza).
- Exponer computed `isSilenced` en `connectionStore`.

---

## 4. Artefactos a Crear / Modificar

### [NUEVO] `frontend/src/stores/connectionStore.ts`
- Store Pinia con estados: `ONLINE`, `OFFLINE`, `RECONNECTING`, `DEGRADED`, `RESTORED`, `SILENCED`
- Acciones: `setStatus()`, `silence()`, `unsilence()`
- Computed: `isSilenced`, `isVisible`, `currentLabel`, `currentIcon`, `currentColor`
- Absorber `requiresRetry` y `retryCount` de `useFormStore.ts`

### [NUEVO] `frontend/src/composables/useConnectionStatus.ts`
- Composable que inicializa listeners `offline`/`online`
- Implementa debounce de 5s
- Gestiona transición OFFLINE → RECONNECTING → RESTORED → invisible
- Auto-cleanup en `onUnmounted`

### [NUEVO] `frontend/src/components/common/ConnectionToast.vue`
- Componente SFC con `<script setup>` + `<template>` + `<style scoped>`
- Renderiza según estado del `connectionStore`
- CSS animations para fade-in/fade-out
- Íconos Material Design (wifi_off, cloud_off, check_circle)

### [MODIFICAR] `frontend/src/App.vue`
- Montar `<ConnectionToast />` como componente global (análogo a `ErrorStateGlobal`)
- Importar y usar `useConnectionStatus()` en el setup

### [DEPRECAR] `frontend/src/components/NetworkRetryModal.vue`
- Envolver o reemplazar. El modal bloqueante (`fixed inset-0`) viola CA-22.
- El nuevo Toast absorbe su funcionalidad.

### [MODIFICAR] `frontend/src/stores/useFormStore.ts`
- Migrar `requiresRetry` y `retryCount` al nuevo `connectionStore`.

---

## 5. Vertical Slice (Orden Obligatorio de Implementación)

```
connectionStore.ts → useConnectionStatus.ts → ConnectionToast.vue → App.vue (mount) → Vitest
```

---

## 6. DoD (Definition of Done)

- [ ] `ConnectionToast.vue` creado en `components/common/`
- [ ] `connectionStore.ts` creado en `stores/`
- [ ] `useConnectionStatus.ts` creado en `composables/`
- [ ] Componente montado en `App.vue`
- [ ] `NetworkRetryModal.vue` deprecado o envuelto
- [ ] Estado `requiresRetry` migrado al nuevo store
- [ ] 4 estados visuales verificados: OFFLINE → RECONNECTING → RESTORED → invisible
- [ ] Debounce de 5s funcional
- [ ] Texto libre de jerga técnica
- [ ] Z-index no colisiona con ErrorStateGlobal ni botón de Fuga
- [ ] `npm run build` exitoso
- [ ] Test Vitest mínimo para el store

---

## 7. Compilación y Gobernanza

**Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---

## 8. Dependencias Cruzadas

| Dependencia | Historia | Impacto |
|-------------|----------|---------|
| US-029 (Frontend UX) | CA-24 (Autoguardado LocalStorage) | El Toast de Modo Degradado (CA-23) referencia el sistema de borradores de US-029. |
| US-000 (Resiliencia) | CA-1 (ErrorStateGlobal) | CA-26 exige prioridad visual para errores transaccionales sobre el Toast. |
| US-001 (Workdesk) | CA-18 (Degradación Elegante) | El Toast es complementario al Banner de degradación BPMN. NO duplicar. |

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
