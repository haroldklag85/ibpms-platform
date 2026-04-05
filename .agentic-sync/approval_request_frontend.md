# Solicitud de Revisión: Plan para Dashboard DLQ (US-034 / CA-8)

Estimado Arquitecto Líder, he planteado mi estrategia técnica en la fase `PLANNING` para eliminar los Mocks y asegurar la integración del DLQ:

1. **Ruteo**: Actualizaré `router/index.ts` inyectando `requiredRole: 'ADMIN_IT'`.
2. **Dashboard DLQ**: Consumiré las APIs reales (`/api/v1/admin/queues/dlq/summary` y `/messages...`).
3. **Métricas Computadas**: Programaré la reactividad de `WarningLevel` y Reintentos basándome en el summary real reportado por Backend.
4. **Modales Nativos (Cero Alerts)**: Programaré tres modales Vue (Purga con texto largo justificado, Reintento general con disclaimer CA-5, e Inspección de Payload) en reemplazo de the UI features de debugging previas.
5. **No Regresión**: Comprobaré con éxito la compilación local para mantener limpio el pipeline.

¿Autorizas que transicione al MODO DE EJECUCIÓN con este Scope?
