# Contrato de Arquitectura Frontend (Iteración 19 | US-005: CA-16 a CA-20)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Integrar herramientas avanzadas en `BpmnDesigner.vue` de concurrencia, simulación e inteligencia artificial para el Arquitecto BPMN.

## 📋 Contexto y Órdenes de Implementación:

### Tarea 1: Auto-Save de Borrador (CA-19)
Implementa un `setInterval` cada 30 segundos en el `BpmnDesigner.vue`. Si el XML cambió, haz auto-save contra un endpoint genérico (ej. `POST /api/v1/design/processes/draft`).
Muestra un discreto indicador en la barra lateral o superior: `"✅ Guardado"`.

### Tarea 2: Bloqueo Pesimista (CA-16)
Al cargar el modelador, lanza un request para obtener el Local/Lock del proceso. (Ej. `POST .../lock`). Si el servidor retorna HTTP 423 (Locked), bloquea el Modelador (Read-Only) y despliega el Toast: `"🔒 Este proceso está siendo editado por [User] desde las [Time]"`.

### Tarea 3: Copiloto IA (CA-17)
Crea el botón `[🧠 Consultar Copiloto IA]`. Al presionarlo, toma el XML y envíalo a `POST /api/v1/design/processes/ai-copilot`. Despliega la respuesta (Mockeada de sugerencias ISO 9001) en un panel o Drawer lateral inferior.

### Tarea 4: Simulador Sandbox (CA-20)
Crea el botón `[🧪 Probar en Sandbox]`. En un escenario real esto consumiría la API, pero en Frontend mockearemos la "iluminación":
1. Llama a `POST /api/v1/design/processes/sandbox-simulate` (esperando un Array de Node IDs).
2. Usa la API de `canvas.addMarker(nodeId, 'highlight-green')` de bpmn-js iterativamente con un `setTimeout` de 1 segundo para iluminar visualmente el camino sugerido por la tabla Sandbox devolviendo la experiencia al usuario.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Aplica estas funciones en el componente Vue y congela todo tu avance localmente:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente la confirmación del guardado.
