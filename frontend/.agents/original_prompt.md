## 2026-05-31T19:28:17Z

El objetivo es corregir el bypass de seguridad en la ruta del DLQ Dashboard (Hallazgo 1), asegurando que solo los roles autorizados ('ROLE_ADMIN_IT' y 'ROLE_SUPER_ADMIN') tengan acceso, guiado por TDD.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend
Integrity mode: development

## Requirements

### R1. Remediación de la Ruta DlqDashboard en router/index.ts
- Modificar la definición de la ruta `DlqDashboard` en `src/router/index.ts` para usar la propiedad `roles` en lugar de `requiredRole`, permitiendo únicamente a los roles `['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']`.

## Acceptance Criteria

### Verificación de Pruebas y Compilación
- [ ] Ejecutar `npx vitest run src/tests/regression_hallazgo1.spec.ts` y comprobar que pase exitosamente (verde).
- [ ] Ejecutar `npm run build` en el frontend y comprobar que compile sin errores.
- [ ] Garantizar que no se hayan modificado aserciones de pruebas históricas (Ley Global 4).

## 2026-06-01T04:50:36Z

El objetivo es realizar la reestructuración completa del árbol de páginas (enrutamiento y seguridad) de la plataforma iBPMS (Hallazgo 2), asignando los metadatos de rol correctos a cada una de las 32 pantallas principales y subcomponentes en router/index.ts, guiado por TDD.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend
Integrity mode: development

## Requirements

### R1. Remediación del Enrutamiento y Roles en router/index.ts
Modificar la definición de las rutas en `src/router/index.ts` para asignar las siguientes configuraciones de metadatos (incluyendo `requiresAuth: true` y roles específicos):
- **Grupo A: Operación Diaria (Buzón y Triaje)**:
  - `/` (`Portal`): General (requiresAuth: true, no roles array needed)
  - `workdesk` (`Workdesk`): General (requiresAuth: true)
  - `kanban` (`KanbanBoard`): General (requiresAuth: true)
  - `admin/customer360` (`Customer360`): General (requiresAuth: true)
  - `admin/projects/manager` (`ProjectManager`): General (requiresAuth: true)
  - `admin/projects/agile-hub/:projectId?` (`AgileHub`): General (requiresAuth: true)
  - `intake-triage` (`IntakeTriage`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN'] }
  - `admin/intake` (`IntakeManual`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN'] }
  - `admin/analytics/bam` (`DashboardBAM`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'Global Admin'] }

- **Grupo B: Gobierno, Seguridad e Incidentes**:
  - `admin/security/identity` (`IdentityGovernance`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin/pmo/settings` (`PmoSettings`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin` (`AdminSettings`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }

- **Grupo C: Diseño y Modelado Low-Code**:
  - `admin/modeler/bpmn` (`BpmnDesigner`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }
  - `admin/modeler/forms` (`FormList`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }
  - `admin/modeler/forms/designer` (`FormDesigner`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }
  - `admin/modeler/dmn` (`DmnIntelligence`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }
  - `ai/prompts` (`AI_PromptLibrary`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }
  - `admin/generic-form` (`GenericFormView`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] } (Registrar esta nueva ruta importando `@/views/admin/GenericForm/GenericFormView.vue`)
  - `admin/integration/mapper` (`VisualMapper`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }
  - `admin/project-builder` (`ProjectBuilder`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }

- **Grupo D: Integraciones y Automatización**:
  - `admin/integration/catalog` (`ConnectorCatalog`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin/integration/builder` (`ConnectorBuilder`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin/integration/dlq` (`DlqDashboard`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin/mailboxes` (`SacConfigManager`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `sgdea/vault` (`SGD_Vault`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin/incidents` (`IncidentCenter`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }
  - `admin/modeler/instances` (`InstancesManager`): meta: { requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] } (Registrar esta nueva ruta importando `@/views/admin/Modeler/InstancesManager.vue`)

*Nota: Para cualquier otra ruta no especificada arriba (como /login o tracking público), mantener requiresAuth: false o isPublic: true.*

## Acceptance Criteria

### Verificación de Pruebas y Compilación
- [ ] Ejecutar `npx vitest run src/tests/regression_hallazgo2.spec.ts` y comprobar que pase exitosamente (verde).
- [ ] Ejecutar `npm run build` en el frontend y comprobar que compile sin errores.
- [ ] Garantizar que no se hayan modificado aserciones de pruebas históricas (Ley Global 4).

## 2026-06-01T22:19:49Z

El objetivo es realizar el análisis de causa raíz y la remediación del bug que provoca que el lienzo central de la aplicación iBPMS quede completamente en blanco al navegar entre pantallas en el frontend.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend
Integrity mode: development

## Requirements

### R1. Análisis de Causa Raíz (RCA) del Lienzo en Blanco
Analizar el ciclo de vida y la reactividad en el layout principal `src/layouts/MainLayout.vue` durante la navegación y el cambio de roles. Identificar si el `:key` dinámico asignado al componente renderizado dentro de `<router-view>` y `<keep-alive>` provoca errores de tipo `TypeError` (por ejemplo, si `route` o `route.fullPath` son indefinidos al montarse el componente o en los tests) que abortan la renderización del lienzo.

### R2. Remediación y Blindaje de Renderizado
Modificar `src/layouts/MainLayout.vue` para:
- Usar de forma segura el objeto `route` inyectado localmente desde el slot scope de `<router-view>` (es decir, `v-slot="{ Component, route }"`).
- Implementar un enlace de `:key` robusto y defensivo que utilice encadenamiento opcional (`route?.fullPath`) y fallbacks en caso de valores nulos o indefinidos para evitar fallos de renderizado en caliente, por ejemplo:
  `route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''` o similar.

## Acceptance Criteria

### Compilación y Suite de Pruebas en Verde
- [ ] Ejecutar la suite completa de pruebas unitarias/regresión del frontend (`npx vitest run`) y comprobar que el 100% de los tests pasen exitosamente (verde).
- [ ] Ejecutar `npm run build` en el frontend y corroborar compilación exitosa sin advertencias ni errores.
- [ ] Garantizar que no se hayan modificado aserciones de pruebas históricas (Ley Global 4).

