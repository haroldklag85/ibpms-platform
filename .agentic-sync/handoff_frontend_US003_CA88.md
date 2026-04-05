# 📦 Handoff Frontend — US-003 / CA-88 (GAP-88)
## Separación Arquitectónica de Contextos IDE vs Workdesk

| Metadato | Valor |
|---|---|
| **Emisor** | Arquitecto Líder (Orquestador) |
| **Destino** | Agente Desarrollador Frontend (Vue 3 / TypeScript) |
| **Fecha** | 2026-04-05 |
| **Rama de trabajo** | `sprint-3/informe_auditoriaSprint1y2` |
| **Prioridad** | 🟡 Alta |
| **GAP de origen** | GAP-88-01 y GAP-88-02 (Auditoría Iteración 69-DEV) |

---

## 1. Contexto del Problema

El CA-88 del SSOT (`v1_user_stories.md`, líneas 849-856) exige una **separación física de módulos** entre la lógica del IDE (Pantalla 7, diseño de formularios) y la lógica del Workdesk (Pantalla 2, operación de tareas).

Teniendo en cuenta los avances recientes, **lo que YA existe:**
- ✅ `frontend/src/composables/workdesk/` — Directorio con composables operativos (`useTasks.ts`, `useZodFactory.ts`, `useSudo.ts`).
- ✅ Componentes de Workdesk: `WorkdeskFormRenderer.vue`, `FormReadOnlyView.vue`, `WorkdeskFormField.vue`.
- ✅ Servicio Garbage Collector: `LocalStorageGarbageCollector.ts`.

**Lo que FALTA (tu responsabilidad para GAP-88):**
- ❌ El directorio `frontend/src/composables/ide/` está VACÍO o los composables del IDE no están correctamente exportados.
- ❌ Falta el archivo `index.ts` (barrel export) en `composables/workdesk/` con la documentación TSDoc sobre la separación arquitectónica.
- ❌ Falta el archivo `index.ts` (barrel export) en `composables/ide/` con la documentación TSDoc respectiva indicando qué lógica debe vivir allí.

---

## 2. Especificación Técnica Exacta

### 2.1. Auditar y Migrar Composables del IDE

Busca dentro del Modeler (ej. `BpmnDesigner.vue`, validadores Zod de diseño AST) si hay lógica extraíble como composables (`useMonaco.ts`, `useFormCanvas.ts`, etc).
Si descubres lógica extraíble que pertenece al IDE de formularios, extrae ese código a `frontend/src/composables/ide/`.

**Si NO existen composables IDE extraíbles en este momento (todo está embebido):**
No es necesario forzar la refactorización si la complejidad no lo amerita ahora. Pero **SÍ** es obligatorio crear el namespace y la documentación.

### 2.2. Crear Barrel Exports con TSDoc OBLIGATORIO

Debes crear dos archivos `index.ts` para sellar la convención arquitectónica.

**Archivo 1: `frontend/src/composables/workdesk/index.ts`**
```typescript
/**
 * @module composables/workdesk
 * @description Composables exclusivos del contexto Workdesk (Pantalla 2).
 * Contienen lógica de validación Zod operativa (@blur / Lazy Validation),
 * gestión de tareas CQRS y privilegios elevados (SU/Sudo).
 * 
 * @governance CA-88 (US-003): PROHIBIDO importar composables de `@/composables/ide/`.
 * La separación es arquitectónica y previene regresiones cruzadas.
 */
export { useTasks } from './useTasks';
export { useZodFactory } from './useZodFactory';
export { useSudo } from './useSudo';
// Agrega cualquier otro si aplica. Exporta de forma explícita, sin "export *".
```

**Archivo 2: `frontend/src/composables/ide/index.ts`**
```typescript
/**
 * @module composables/ide
 * @description Composables exclusivos del contexto IDE de Formularios (Pantalla 7).
 * Contienen lógica del Mónaco Editor, parsing AST del Canvas,
 * Language Servers (CA-17) y manejo de errores de sintaxis (CA-84).
 *
 * @governance CA-88 (US-003): PROHIBIDO importar composables de `@/composables/workdesk/`.
 * La separación es arquitectónica y previene regresiones cruzadas.
 */
// TODO: Migrar composables del IDE a este módulo en futuras refactorizaciones del BpmnDesigner.vue u otros visores.
```

### 2.3. Validar Aislamiento Cruzado

Ningún archivo del workdesk puede importar archivos del ide y viceversa. Valida que el bundler (Vite) no explote. Ejecuta:
```bash
npm run build
```

---

## 3. Contrato de No-Regresión

> [!CAUTION]
> - **PROHIBIDO** modificar archivos de backend/Java.
> - **PROHIBIDO** alterar el archivo `apiClient.ts` o los flujos E2E de Testing si no tienen relación con el refactor de `ide/` y `workdesk/`.
> - **PROHIBIDO** generar errores de importación en los componentes Vue al agrupar mediante barrel exports. Si actualizas a barrel exports, no olvides actualizar `import { useTasks } from '@/composables/workdesk/useTasks'` por `import { useTasks } from '@/composables/workdesk'` donde aplique, o simplemente deja los imports directos existentes y solo expón el index para futuras normativas.

---

## 4. Criterio de Aceptación Técnico (Definition of Done)

- [ ] `frontend/src/composables/workdesk/index.ts` ha sido creado y documentado con TSDoc (CA-88 Governance).
- [ ] `frontend/src/composables/ide/index.ts` ha sido creado y documentado con TSDoc (CA-88 Governance).
- [ ] Cualquier referencia extraíble de IDE que se haya migrado funciona bien.
- [ ] Compilación verde sin fallos de importaciones.
- [ ] Stash / Push documentado con la corrección del GAP-88.

### Pasos Operativos para el Agente Frontend:
- Crea/edita los archivos mencionados.
- Valida la compilación (`npm run build` en `frontend`).
- Redacta el `approval_request_frontend.md` y solicítale tu aprobación al Humano indicándole que se ejecute la validación final y luego procede a empaquetar con `git stash save "temp-frontend-US003-CA88"`.
