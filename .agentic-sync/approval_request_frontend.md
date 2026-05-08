# 🛡️ Solicitud de Aprobación: US-036 (CA-29 a CA-32)

**Agente:** David (Frontend Developer)
**Rama:** `DevDavid`
**Objetivo:** Finalizar la UI de Gobernanza y la dinámica del Menú Maestro.

## Resumen del Plan Propuesto

1.  **Rediseño de Modal (CA-29)**: Se añadirán los prefijos numéricos ("Tab 1", "Tab 2") a las pestañas del modal de gestión de roles en `IdentityGovernance.vue` para mayor claridad administrativa.
2.  **Sidebar 100% Dinámico (CA-31)**: Se purificará `MainLayout.vue` para que la navegación lateral se base exclusivamente en el `menuStore`, eliminando validaciones redundantes de roles en el cliente.
3.  **Gobernanza de Caché (CA-32)**: Se integrará en `apiClient.ts` la purga automática del `menuStore` mediante `$reset()` cuando el backend emita un 403, garantizando que el usuario visualice sus permisos actualizados instantáneamente.

## Puntos de Control de Arquitectura
- **Anti-JWT Bloat**: No se lee información de menús desde el token.
- **Zero-Trust**: El fallo de red o permisos bloquea la UI (CA-26/32).
- **Clean Code**: Se implementará `$reset` manual en el store para mantener el estándar Pinia.

**Humano, por favor entrega este mensaje al Arquitecto Líder y regrésame su veredicto.**
