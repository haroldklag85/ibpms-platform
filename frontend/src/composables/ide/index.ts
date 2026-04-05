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
