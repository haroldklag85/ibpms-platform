# 🎨 HANDOFF FRONTEND — Iteración 3 (Sprint 6.2)
## Auditoría Arquitectural: US-003 (iForm Maestro) + US-039 (Formulario Genérico)

**Fecha de Emisión:** 2026-04-20  
**Emitido por:** Arquitecto Líder (Antigravity)  
**Destinatario:** Equipo Frontend  
**Protocolo:** Zero-Hallucination | Gobernanza CA-88 (Composable Segregation)  

---

## 📋 Resumen Ejecutivo

Este handoff consolida **todas las tareas de frontend** derivadas de la auditoría forense de las US-003 y US-039. Cubre componentes de la Pantalla 7.B (Formulario Genérico), el IDE de formularios, y la gobernanza de composables.

---

## 1. Tareas CERRADAS (Ya Ejecutadas — Solo Verificación)

### ✅ REM-003-04: MAX_FORM_FIELDS Performance Lock

**Severidad:** 🟡 Media | **US:** US-003  
**Estado:** ✅ IMPLEMENTADO por Arquitecto

**Archivo:** `frontend/src/views/admin/Modeler/FormDesigner.vue`

**Qué se hizo:**
Se implementó la constante `MAX_FORM_FIELDS = 200` y un `computed` reactivo `isHighDensityForm` que emite una alerta visual cuando el formulario supera el 80% de capacidad.

**Verificación requerida por el equipo:**
- [ ] Crear un formulario de prueba con 170+ campos y validar que aparece el banner de advertencia.
- [ ] Verificar que con 200+ campos el botón de agregar se deshabilita.

---

### ✅ REM-003-06: LocalStorageGarbageCollector

**Severidad:** 🟢 Baja | **US:** US-003  
**Estado:** ✅ IMPLEMENTADO por Arquitecto

**Archivos:**
- `frontend/src/services/LocalStorageGarbageCollector.ts` (NUEVO)
- `frontend/src/App.vue` (MODIFICADO — onMounted hook)

**Qué se hizo:**
Servicio que purga automáticamente borradores expirados (>7 días) o cuando el bucket total supera 50MB. Se ejecuta al arrancar la SPA.

**Verificación requerida por el equipo:**
- [ ] En DevTools → Application → LocalStorage, insertar manualmente una clave `ibpms_draft_test` con timestamp viejo (>7 días). Recargar la app. Debe desaparecer.
- [ ] Verificar en consola el log `[GC] Purged N stale drafts`.

---

### ✅ REM-039-C: Banner de Restauración de Borrador

**Severidad:** 🟢 Baja | **US:** US-039  
**Estado:** ✅ IMPLEMENTADO por Arquitecto

**Archivos modificados:**
- `frontend/src/stores/genericFormStore.ts` — Agregados: `showDraftBanner`, `pendingDraft`, `restoreDraft()`, `dismissDraft()`
- `frontend/src/views/admin/GenericForm/GenericFormView.vue` — Agregado: banner visual amber con botones Restaurar/Descartar

**Verificación requerida por el equipo:**
- [ ] Abrir una tarea genérica, escribir observaciones, cerrar la pestaña sin enviar.
- [ ] Reabrir la misma tarea. Debe aparecer el banner amber: _"Se detectó un borrador no enviado. ¿Desea restaurarlo?"_
- [ ] Clic en "Restaurar" → Los campos se rellenan con el borrador. Banner desaparece.
- [ ] Repetir y clic en "Descartar" → Los campos quedan vacíos. Banner desaparece.

---

## 2. Tareas ABIERTAS (Requieren Acción del Equipo)

### 🔲 FRONT-001: Traducción de Etiquetas de Resultado de Gestión

**Origen:** CA-4 (US-039) | **Prioridad:** Alta

**Archivo:** `frontend/src/components/forms/generic/ManagementResultSelect.vue`

**Problema:** Las opciones del `<select>` se muestran exactamente como vienen del backend (`APPROVED`, `REJECTED`, `PENDING_INFO`, `ESCALATED`). El usuario final de negocio no entiende estos labels técnicos.

**Prescripción:**
```vue
<script setup lang="ts">
const RESULT_LABELS: Record<string, string> = {
  'APPROVED': 'Aprobado',
  'REJECTED': 'Rechazado',
  'PENDING_INFO': 'Pendiente de Información',
  'ESCALATED': 'Escalado a Superior'
}

const getLabel = (key: string) => RESULT_LABELS[key] || key
</script>

<template>
  <!-- Reemplazar la línea actual -->
  <option v-for="option in store.allowedResults" :key="option" :value="option">
    {{ getLabel(option) }}
  </option>
</template>
```

**Criterio de Aceptación:**
```
DADO un operador viendo el Formulario Genérico
CUANDO despliega el dropdown de "Resultado de la Gestión"
ENTONCES las opciones se muestran en español: "Aprobado", "Rechazado", etc.
Y el value enviado al backend sigue siendo el enum original en inglés.
```

---

### 🔲 FRONT-002: Indicador Visual de Modo Solo-Lectura en MetadataGrid

**Origen:** CA-2 (US-039) | **Prioridad:** Media

**Archivo:** `frontend/src/components/forms/generic/MetadataGrid.vue`

**Problema:** La cuadrícula de metadatos usa el ícono de candado pero no tiene un tooltip accesible que explique por qué el campo es de solo lectura.

**Prescripción:**
```html
<!-- Agregar aria-label y tooltip al ícono de candado -->
<div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
     title="Este campo es de solo lectura. Su valor proviene de las variables del proceso BPMN.">
```

**Criterio de Aceptación:**
```
DADO un operador haciendo hover sobre el ícono de candado
CUANDO pasa el cursor por encima
ENTONCES aparece un tooltip explicativo sobre la naturaleza del campo.
```

---

### 🔲 FRONT-003: Implementar Confirmación de Navegación con Cambios No Guardados

**Origen:** CA-7 (US-039) | **Prioridad:** Media

**Archivo:** `frontend/src/views/admin/GenericForm/GenericFormView.vue`

**Problema:** Si el operador navega fuera de la pantalla con un formulario parcialmente lleno sin guardar, los datos se pierden sin aviso.

**Prescripción:**
```typescript
// En el <script setup> de GenericFormView.vue
import { onBeforeRouteLeave } from 'vue-router'

onBeforeRouteLeave((to, from, next) => {
  if (store.observations || store.result) {
    const answer = window.confirm('Tiene cambios sin guardar. ¿Desea salir sin guardar?')
    if (!answer) return next(false)
  }
  next()
})
```

**Criterio de Aceptación:**
```
DADO un operador que ha escrito observaciones en el formulario genérico
CUANDO intenta navegar a otra pantalla usando el sidebar
ENTONCES aparece un diálogo nativo: "Tiene cambios sin guardar. ¿Desea salir?"
Y si cancela, permanece en el formulario.
```

---

### 🔲 FRONT-004: Validación de Tamaño de Archivo en EvidenceDropzone

**Origen:** CA-4 (US-039) | **Prioridad:** Media

**Archivo:** `frontend/src/components/forms/generic/EvidenceDropzone.vue`

**Problema:** El componente limita a 5 archivos (`MAX_FILES = 5`) pero **no valida el tamaño individual** de cada archivo. La etiqueta dice "hasta 10MB" pero no se implementa la restricción.

**Prescripción:**
```typescript
const MAX_FILE_SIZE_MB = 10
const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'application/pdf']

const addFiles = (newFiles: File[]) => {
  const remaining = MAX_FILES - store.files.length
  if (remaining <= 0) return
  
  const validFiles = newFiles.filter(f => {
    if (f.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
      console.warn(`Archivo ${f.name} excede ${MAX_FILE_SIZE_MB}MB`)
      return false
    }
    if (!ALLOWED_TYPES.includes(f.type)) {
      console.warn(`Tipo de archivo ${f.type} no permitido`)
      return false
    }
    return true
  })
  
  const allowed = validFiles.slice(0, remaining)
  store.files = [...store.files, ...allowed]
}
```

**Criterio de Aceptación:**
```
DADO un operador que arrastra un archivo de 15MB al dropzone
CUANDO lo suelta
ENTONCES el archivo NO se agrega a la lista
Y aparece un mensaje visual advirtiendo que excede el límite de 10MB.
```

---

### 🔲 FRONT-005: FormReadOnlyView — Integración con Print Nativo

**Origen:** REM-003-07 (US-003) | **Prioridad:** Baja

**Archivo:** `frontend/src/components/workdesk/FormReadOnlyView.vue`

**Problema:** El componente ya existe con los modos `audit` y `print`, pero falta un botón que active `window.print()` y el CSS `@media print` necesita ocultar elementos del sidebar y navbar.

**Prescripción:**
```vue
<!-- Agregar botón de impresión cuando mode === 'print' -->
<button v-if="mode === 'print'" 
        @click="window.print()" 
        class="mb-4 bg-gray-800 text-white px-4 py-2 rounded-md text-sm print:hidden">
  🖨️ Imprimir Expediente
</button>
```

```css
@media print {
    .print-mode { page-break-inside: avoid; }
    nav, aside, .sidebar, .navbar { display: none !important; }
}
```

---

## 3. Inventario de Componentes (US-039)

```
frontend/src/
├── views/admin/GenericForm/
│   └── GenericFormView.vue          ← Vista principal (Pantalla 7.B)
├── components/forms/generic/
│   ├── GenericFormBody.vue           ← Orquestador del cuerpo
│   ├── MetadataGrid.vue             ← Cuadrícula solo-lectura (whitelist)
│   ├── ObservationsField.vue        ← Textarea con Zod (10-2000 chars)
│   ├── ManagementResultSelect.vue   ← Dropdown de resultado
│   ├── EvidenceDropzone.vue         ← Drag & Drop (max 5 archivos)
│   ├── PanicButtonBar.vue           ← 3 botones de pánico
│   ├── PanicJustificationModal.vue  ← Modal con validación ≥20 chars
│   └── DraftSyncIndicator.vue       ← Indicador visual (4 estados)
└── stores/
    └── genericFormStore.ts          ← Pinia store centralizado
```

---

## 4. Reglas de Gobernanza Vigentes

> [!WARNING]
> **CA-88: Segregación de Composables.** Los composables en `src/composables/ide/` NO pueden importar de `src/composables/workdesk/` y viceversa. Cualquier violación será rechazada en Code Review.

> [!IMPORTANT]
> **Validación Dual Obligatoria.** Toda validación de formulario DEBE existir tanto en Frontend (Zod) como en Backend (Jakarta Validation). No confiar únicamente en la validación del cliente.

---

> [!TIP]
> **Priorización sugerida:** FRONT-001 (UX inmediata) → FRONT-004 (seguridad) → FRONT-003 (prevención pérdida datos) → FRONT-002 (accesibilidad) → FRONT-005 (print)
