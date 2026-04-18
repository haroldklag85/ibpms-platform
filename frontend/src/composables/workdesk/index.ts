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
