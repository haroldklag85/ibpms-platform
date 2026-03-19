# Contrato de Arquitectura Frontend (Iteración 20 | US-005: CA-21 a CA-25)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Transformar el Lienzo Técnico (bpmn-js) en un IDE Ergonómico (Custom Palette, Minimap, Sidebar Explorador).

## 📋 Contexto y Órdenes de Implementación:

### Tarea 1: Control de Acero sobre el Botón Desplegar (CA-21)
*   **Funcionalidad:** Implementa un Mock-Estado en la UI (Ej: un dropdown/switch superior) para alternar el rol del usuario de prueba entre `"BPMN_Designer"` y `"BPMN_Release_Manager"`.
*   **Regla Visual:** Mapea el botón `[🚀 DESPLEGAR]` para que esté `disabled` y grisáceo a menos que el estado/rol sea el de *Release Manager*.

### Tarea 2: Catálogo de Procesos (CA-23)
*   Crea un Drawer/Sidepanel llamado **"Explorador de Procesos"**.
*   Allí, dispara un `GET /api/v1/design/processes` y lista los modelos devueltos. 

### Tarea 3: Ergonomía Visual - Zoom y Minimap (CA-25)
*   Instala o importa el módulo `diagram-js-minimap` y agrégalo a los modulos adicionales en la inicialización de tu instancia `new BpmnModeler(...)`.
*   Crea una botonera Flotante (Floating Action Buttons UI) en la esquina inferior izquierda con: `[+] (Zoom In)`, `[-] (Zoom Out)`, `[O] (Fit to Screen)`. Llama internamente a `modeler.get('canvas').zoom(...)`.

### Tarea 4: Custom Palette (CA-22, CA-24)
*   Anula la Paleta Izquierda por defecto e inyecta un `CustomPaletteProvider` de bpmn-js.
*   **Visibles Principales:** Solo debes dejar StartEvent, EndEvent, UserTask, ServiceTask, ExclusiveGateway, ParallelGateway y TextAnnotation (CA-24).
*   *Nota Arquitectónica: Si construir el CustomProvider exige demasiado tiempo JS de bajo nivel en el subagente y pone en riesgo compilar, el Arquitecto admite ocultar los elementos no deseados vía CSS puro `.djs-palette .[ClaseHija] { display: none !important; }` agrupando los permitidos al inicio.*

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Codifica la vista, conecta al apiClient.ts y lanza tus herramientas a un stash de Git:
`git stash save "temp-frontend-US005-ca21-ca25"`

Informa textualmente la confirmación del guardado.
