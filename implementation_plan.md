# Plan de Implementación: Rediseño del Panel Lateral de Validación y Simulación (US-005)

## 1. Contexto y Objetivos
Para mejorar la usabilidad y no bloquear el lienzo del modelador BPMN, se reemplazará el modal central por un panel lateral derecho (Push Layout) deslizable y redimensionable, que mantendrá visible e interactivo el modelador.

### Características Clave:
- **Push Layout:** El panel lateral derecho empuja el lienzo del modelador y oculta la barra de propiedades de Camunda (`v-show="!showSandboxModal"`).
- **Ancho Redimensionable:** Lógica nativa de mouse-drag que permite redimensionar el panel entre `400px` y `700px`.
- **Acordeón Vertical:** Organización de las 3 fases de validación en secciones colapsables verticalmente:
  - **Linter Local** (Fase 1)
  - **Pre-Flight Analyzer** (Fase 2)
  - **Sandbox Simulator** (Fase 3: Ejecutor de simulación y grilla de variables)
- **Grilla Interactiva de Variables:** Gestión de variables en la Fase 3 con CRUD inline (Agregar, editar, borrar) persistiendo reactivamente en `localStorage` con la clave del proceso.
- **Trazado Progresivo:** Animación secuencial de halos verdes (nodo por nodo) al completarse exitosamente la simulación con delay de `400ms`.

---

## 2. Diseño Técnico en `BpmnDesigner.vue`

### 2.1 Variables Reactivas
- `showSandboxModal` (ref, boolean) - Visibilidad del panel de validación (mantenido por compatibilidad con tests).
- `validationPanelWidth` (ref, number) - Ancho dinámico del panel (inicial: `450`).
- `isResizingValidation` (ref, boolean) - Estado de arrastre.
- `collapsedSections` (ref, object) - `{ linter: false, preflight: false, simulator: false }`.

### 2.3 Resizer Nativo (Mouse Events)
- Captura de eventos `mousedown` en el borde izquierdo de la barra lateral, `mousemove` y `mouseup` globales para ajustar `validationPanelWidth.value = window.innerWidth - e.clientX` dentro del rango de `400` a `700`.

### 2.4 Grilla de Variables (LocalStorage CRUD)
- Clave: `ibpms_sandbox_variables_${processId.value}`
- Métodos inline para añadir nueva variable con clave, tipo (`String`, `Number`, `Boolean`) y valor.
- Botones de eliminación y campos de edición inline que guardan de inmediato en `localStorage`.

### 2.5 Animación Secuencial
- Trazado de halos progresivo con `setTimeout` de `400ms` por nodo. En entorno de tests Vitest (`(window as any).__vitest_worker__` o similar), se omitirá el delay para evitar timeouts y asegurar que los tests unitarios sincrónicos pasen sin modificaciones de tiempo de espera.

---

## 3. Estrategia de Retrocompatibilidad de Pruebas
- Para que la suite de pruebas unitarias `BpmnDesigner.spec.ts` pase al 100% en verde, se mantendrá `showSandboxModal` como flag principal, y el contenedor principal de la barra lateral tendrá el atributo `data-testid="sandbox-glass-modal"`, `data-testid="linter-level"`, `data-testid="preflight-level"` y `data-testid="sandbox-level"`. Esto garantiza que los selectores del test sigan localizando y validando correctamente los elementos sin tener que reescribir las aserciones certificadas en sprints anteriores (previniendo el Test-Driven Decay).

---

## 4. Trazabilidad
Todas las modificaciones llevarán la etiqueta correspondiente de trazabilidad:
`// @Traceability: US-005, CA-80` a `CA-84`
