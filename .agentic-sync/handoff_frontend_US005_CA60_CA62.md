# 🎨 HANDOFF TÉCNICO V1: FRONTEND (Vue 3 / TypeScript) [RESTAURADO]
## 🎯 ITERACIÓN 30 - Módulo: Diseñador BPMN Avanzado (US-005)
**Criterios de Aceptación a Implementar:** CA-60 al CA-62 (Candados Visuales Restrictivos).

---

### 🛑 REGLAS DE GOBERNANZA (Scope V1 Strict)
> [!WARNING]
> En esta iteración limitaremos al Arquitecto Funcional en el UI. No hay V2. Buscamos extender el panel de propiedades nativas del `BpmnDesigner.vue` para forzar UX "a prueba de tontos" y un Pre-Flight Analyzer visual.

---

### 🛠️ INSTRUCCIONES DE DISEÑO TÉCNICO

El Agente Vue/UI debe expandir u optimizar la lógica de `BpmnDesigner.vue` (o los subcomponentes del panel de propiedades).

#### 1. CA-60: UI de Mapeo Obligatorio (Call Activity)
- Al seleccionar una `Call Activity` en el lienzo, mostrar prominentemente la sección para agregar Parámetros de Input/Output.
- Validar en el analizador Pre-Flight (justo antes o durante el clic de 'Deploy') que aquellas `Call Activities` sin `camunda:in` o `camunda:out` sean bloqueadas mostrando un error en pantalla: "El Subproceso nacerá ciego por falta de datos".

#### 2. CA-61: Control Selector de DMN (Business Rule Task)
- Al seleccionar una `Business Rule Task`, restringir la edición libre o de scripts para delegados.
- Reemplazarlo visualmente por un Dropdown obligatorio: `[ 🧠 Tabla de Decisión (Decision_Ref) ]`.
- Si el nodo no tiene DMN amarrado en el XML, abortar el Submit con el respectivo Toast de advertencia.

#### 3. CA-62: Candado de External Task (Service/Send Task)
- Al seleccionar `Service Task` o `Send Task`, inhabilitar visualmente opciones de "Java Class" o "Delegate Expression" del bpmn-js-properties-panel si es posible, o sobreescribirlas sistemáticamente al exportar el XML.
- Forzar la configuración como "**External**".
- Exigir un campo Input Texto obligatorio para el atributo `Topic`, representando la cola del External Worker.

---

### 🧪 Plan de Verificación Exigido
El Agente Frontal debe:
1. Asegurar en la UI que si el usuario pinta estos "Nodos Rebeldes", no pueda desplegarlos.
2. Comprobar la inyección de la vista del Dropdown DMN.
3. **Respetar Gatekeeper Stash:** Al concluir, usar `git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`.
