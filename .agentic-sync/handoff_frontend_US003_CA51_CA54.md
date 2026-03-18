# Contrato de Arquitectura Frontend (US-003 Iteración 11: CA-51 al CA-54)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Componentes y Formularios Zod).
**Objetivo:** Desarrollar feedback de carga asíncrona, enmascaramiento de contraseñas y profilaxis de variables "fantasma" en el AST.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Debes implementar las siguientes características de comportamiento avanzado en `FormDesigner.vue`:

*   **CA-51 (Protección Parcial de Grilla):** En la renderización visual de un `field_array` (Data Grid iterativo), mapea cada fila. Si la fila (desde el `prefillData`) contiene la llave secreta `_locked: true`, renderiza todos los inputs de *Esa* fila en específico con el atributo `:disabled="true"`.
*   **CA-52 (Feedback Visual en llamadas Async):** Vinculado al `async_select` (Typeahead API). Crea una variable reactiva global `isAsyncLoading = ref(false)`. Cuando el Typeahead esté resolviendo la API, ponla en `true`. Utiliza esta variable para deshabilitar temporalmente los Smart Buttons (`button_submit`, `button_draft`) agregándoles `:disabled="isAsyncLoading"`. 
*   **CA-53 (Inputs de Contraseña Sensibles):** Añade al toolbox un componente de tipo "Password" (icono 🔑). Renderízalo como `<input type="password">`. El Zod inferido debe seguir siendo `z.string()`.
*   **CA-54 (Limpieza Automática por Condicional - Phantom Data):** En el método pre-submit (o mediante Watchers profundos), si un campo estuvo visible y el usuario digitó algo, pero luego la *Condición de Visibilidad* (`visibilityCondition` del CA-25) o el Zod *Required-If* (CA-48) hacen desaparecer/desactivar al campo, el dato almacenado reactivamente en `formData` para esa variable debe fijarse a `null` o borrarse de la llave (`delete formData[key]`). Esto previene el envío de valores residuales ("Phantom Data") ocultos a Camunda.

## 📐 Reglas de Desarrollo:
1. Para el CA-54, la interceptación ideal de limpieza de Phantom Data es justo al pulsar el botón Enviar, recorriendo el AST visible vs el objeto `formData` reactivo, purificando lo que no esté visible.
2. Asegúrate de añadir la macro UI del Password tanto a la ToolBox como al Canvas Render `v-if="element.type === 'password'"`.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer un `git commit` directamente a principal.
Desarrolla las 4 implementaciones UI solicitadas. Usa Axios mock o variables dummy para probar los Spinners. Al conseguir un canvas estable, estanca el desarrollo usando:
`git stash save "temp-frontend-US003-ca51-ca54"`

Reporta con un mensaje de "Éxito Stash" exclusivo cuando finalices el comando git.
