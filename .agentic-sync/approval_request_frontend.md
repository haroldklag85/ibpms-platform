# Solicitud de Revisión: US-017 CA-19 a CA-26 (Connection Toast)

**De:** Agente Frontend  
**Para:** Arquitecto Líder  

He completado el análisis y la elaboración del plan de implementación (`implementation_plan.md`) para el Monitoreo de Conexión No Intrusivo (Toast Flotante), adhiriendo a las reglas CA-19 a CA-26.

## Resumen del Plan:
1. **Store:** Se creará `connectionStore.ts` con los estados: `ONLINE`, `OFFLINE`, `RECONNECTING`, `DEGRADED`, `RESTORED`, `SILENCED`. Se migrará `requiresRetry` desde `useFormStore.ts`.
2. **Composable:** Se creará `useConnectionStatus.ts` gestionando los event listeners de red con el debounce requerido de 5s, coordinando la transición de OFFLINE a DEGRADED (15s), y el flujo de reconexión con desvanecimiento de 3s.
3. **Componente:** Se ensamblará `ConnectionToast.vue`, configurado en capa Z-9990, de tamaño acotado (max-width 320px) en la esquina inferior izquierda, no bloqueante (pointer-events-auto solo en él).
4. **App.vue:** Se montará el Toast globalmente y se llamará al composable.
5. **Deprecación:** Se vaciará `NetworkRetryModal.vue` para eliminar su comportamiento bloqueante (overlay negro).
6. **Testing:** Se incluirá `connectionStore.spec.ts` para testear las máquinas de estado bajo Vitest.

Solicito formalmente la revisión y el visto bueno para proceder a la etapa de `EXECUTION`.
