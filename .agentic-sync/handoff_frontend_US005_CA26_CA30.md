# Contrato de Arquitectura Frontend (Iteración 21 | US-005: CA-26, CA-27, CA-29, CA-30)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Elevar la productividad del diseñador implementando Dual Naming, advertencias arquitectónicas, inyección de plantillas y portapapeles.

## 📋 Contexto y Órdenes de Implementación:

*   **CA-28 (Diff Visual):** EXCLUIDO ESTRICTAMENTE. No escribir código sobre esto.

### Tarea 1: Naming Dual y Auto-Slug (CA-26)
*   **Funcionalidad:** Intercepta el evento de cambio de nombre (`element.changed` sobre la propiedad `name` de ServiceTasks o UserTasks).
*   **Regla:** Si el usuario tipea el Nombre de Negocio (Ej: "Revisar Documento"), el sistema debe auto-generar la propiedad ID `id` (Technical Name) convirtiendo a slug (Ej: `revisar_documento`) siempre y cuando el usuario no haya forzado un ID manual previamente. Actualiza el Modelador usando `modeling.updateProperties(element, { id: 'nuevo_slug' })`.

### Tarea 2: Modal de Plantillas "Nuevo Proceso" (CA-27)
*   Crea un botón/modal `[Nuevo Proceso]`.
*   Ofrece: "Comenzar en Blanco" o una lista de Plantillas consumidas de `GET /api/v1/design/processes/templates`.
*   Al elegir plantilla, el Frontend inyecta el XML devuelto llamando a `modeler.importXML(...)`.

### Tarea 3: Portapapeles Cross-Process (CA-29)
*   Para que el Arquitecto pueda copiar nodos (Ctrl+C) e ir a otra pestaña (Otro modelo) y pegarlos (Ctrl+V), debes asegurarte explícitamente de que los módulos `keyboard` y `copyPaste` de bpmn-js estén importados e inicializados en tu Factory del Modeler (`keyboard: { bindTo: document }`). El motor nativo remapeará los IDs internamente al pegar.

### Tarea 4: Alerta de Complejidad (Mala Práctica) (CA-30)
*   Consulta el límite en `GET /api/v1/admin/settings/bpmn-complexity-limit`.
*   Suscríbete al evento `commandStack.elements.create.postExecute` o `shape.added`.
*   Cuenta `modeler.get('elementRegistry').getAll().length`. Si supera el umbral (Ej: 100), lanza un Toast amarillo de alerta (No bloqueante): *"⚠️ Mala Práctica: Diagrama excede [100] nodos. Riesgo de mantenimiento y rendimiento motor."*

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Congela tu código en stash:
`git stash save "temp-frontend-US005-ca26-ca30"`

Informa textualmente la confirmación del guardado.
