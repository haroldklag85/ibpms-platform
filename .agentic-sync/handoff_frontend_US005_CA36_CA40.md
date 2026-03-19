# Contrato de Arquitectura Frontend (Iteración 23 | US-005: CA-36, CA-39, CA-40)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Interceptar el Panel de Propiedades nativo de BPMN para inyectar combos dinámicos que amarren el Modelador de Procesos al Creador de Formularios (US-028).

## 📋 Contexto y Órdenes de Implementación:

*(CA-37 y CA-38 están vetados bajo pena de rollback).*

### Tarea 1: Navegación de Elementos de Llamada (CA-36)
*   En el Properties Panel (o mediante un `Overlay` sobre el Canvas), cuando el usuario selecciona una `CallActivity` (Sub-Proceso), detecta si tiene seteado el atributo `calledElement`.
*   Si lo tiene, muestra un enlace/botón visual: `[🔗 Abrir Sub-Proceso: {calledElement}]`. (Por ahora, puede ser un simple `console.log` o un ruteo simulado `router.push('/admin/modeler/' + id)`).

### Tarea 2: Consistencia de Patrón (CA-40)
*   Cuando el arquitecto presiona `Nuevo Proceso`, el Modal (de CA-27) ahora debe exigir un radio button: *"Patrón A: Formularios Simples"* o *"Patrón B: iForm Maestro"*.
*   Este valor guárdalo en la raíz del XML (Ej: `<bpmn:process id="..." camunda:property name="formPattern" value="SIMPLE">`).

### Tarea 3: FormKey Dinámico y Validado (CA-39)
*   **LA TAREA MÁS CRÍTICA:** Debes interceptar el campo `Form Key` de las `UserTask` en el Properties Panel. 
*   **En lugar de ser un Input de Texto Libre**, debe ser un Dropdown (`<select>`).
*   Las opciones del Dropdown se llenan llamando a la API mockeada `GET /api/v1/forms`.
*   **Filtro:** Si el proceso tiene `formPattern="SIMPLE"`, el dropdown SOLO listará formularios de tipo Simple. Si es Maestro, solo Maestros.
*   Al seleccionar, el valor se graba nativamente en `<camunda:formKey>` dentro del XML de la UserTask.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Consolida tu inyección de Properties Panel y asegura tu código en stash:
`git stash save "temp-frontend-US005-ca36-ca40"`

Informa textualmente la confirmación del guardado.
