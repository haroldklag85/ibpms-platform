# Auditoría de Arquitectura V2: Reporte Consolidado de Deuda Técnica y Ambigüedades

**Fecha de Ejecución:** 2026-05-12
**Autor:** Antigravity (Lead Architect)
**Alcance (Historias de Usuario):** US-000, US-001, US-002, US-003, US-004, US-005, US-007, US-017, US-025, US-028, US-029, US-030, US-034, US-036, US-038, US-039, US-043, US-048.
**Criterios de Evaluación:** ADR-001 (Arquitectura Hexagonal), ADR-006 (Dumb Components / Pinia), ADR-010 (Zero-Mock), Ley Global 3 (Trazabilidad).

## Resumen Ejecutivo
Se ha ejecutado un escaneo dinámico directo sobre el código fuente (Backend Java/Spring Boot, Frontend Vue/TypeScript) ignorando los artefactos declarativos (`taskAud.md`, etc.). A pesar de que muchas historias se encuentran marcadas como "Certificadas V2", el escáner ha revelado **fisuras microscópicas y deudas técnicas no reportadas** que comprometen el aislamiento de capas y la reactividad pura.

---

## Hallazgos Críticos y Medios (Por Capa Arquitectónica)

### 1. Violación de Arquitectura Hexagonal (Backend - ADR-001)

Se detectó un patrón generalizado donde los Adaptadores de Entrada (`@RestController`) están inyectando directamente Adaptadores de Salida (Repositorios JPA), saltándose los Puertos de Entrada, Casos de Uso y Servicios de Dominio. Esto acopla la capa HTTP al ORM (Hibernate) e imposibilita las pruebas unitarias aisladas.

*   **[Crítico] `DynamicWebhookRouterController` (Asoc. US-017/US-000):** Inyecta directamente `InboundWebhookRepository` para resolver y buscar webhooks por ID (`findByIdAndIsActiveTrue`).
*   **[Crítico] `RbacAdminController` (Asoc. US-036):** Inyecta y orquesta crudamente `IbpmsProfileRepository`, `IdpGroupMappingRepository`, y `ProfileBpmnAssignmentRepository`. No existe lógica de negocio interpuesta, actúa como un CRUD directo.
*   **[Crítico] `AuthSyncController` (Asoc. US-038/US-048):** Inyecta `UserRepository` y `SystemAuditLogRepository`. Genera mutaciones directas de estado (`userRepository.save()`) y dispara auditorías de emergencia (`systemAuditLogRepository.save(new SystemAuditLogEntity(...))`) desde la capa HTTP.
*   **[Crítico] Controladores de Operación (`TaskController`, `TaskSkipController`):** Inyectan `FormFieldValueAuditRepository` y `SkipAuditRepository` para guardar logs directamente durante las transacciones.
*   **[Medio] `AuditReportController` (US-036):** Inyecta `IdentityRepository` para listar entidades directamente.

**Acción de Remediación:** Refactorización inmediata. Todo `@RestController` debe depender exclusivamente de interfaces (`UseCases` o `InputPorts`). 

### 2. Violación del Patrón "Dumb Components" (Frontend - ADR-006)

El frontend presenta una degradación en la separación de responsabilidades. Múltiples componentes visuales (`.vue`) están asumiendo el rol de orquestadores de red en lugar de despachar acciones a los Stores de Pinia o a Composables de lógica.

*   **[Crítico] Inyecciones Directas de `apiClient`:** Los siguientes componentes invocan directamente `apiClient.get/post/put/delete`, conteniendo URLs hardcodeadas y lógica de respuesta HTTP:
    *   `IdentityGovernance.vue` (Múltiples endpoints: kill-session, revoke, reset-password).
    *   `RbacManagerView.vue`, `ServiceAccountsTable.vue`, `GlobalRolesTable.vue`, `RbacDelegationLog.vue` (US-036/US-038).
    *   `PmoSettings.vue` (Endpoints de SLA).
    *   `InstancesManager.vue`, `DlqDashboard.vue`.
*   **[Medio] Fuga de Temporizadores (`setInterval`):** A pesar del esfuerzo en CA-11, existen componentes y módulos que continúan instanciando `setInterval` ignorando el ciclo centralizado (`requestAnimationFrame` / `timeStore`):
    *   `BpmnDesigner.vue` (US-005): Posee temporizadores huérfanos para `heartbeatInterval` y `autoSaveInterval`, los cuales generarán DOM-thrashing en sesiones prolongadas.
    *   `IntakeTriageView.vue`: Utiliza polling (`setInterval`) en lugar de depender de WebSockets o SSE para la actualización de tareas.
    *   `ImpersonationBanner.vue`, `useFormStore.ts`, `authStore.ts`: Utilizan `setInterval` para cálculos de UI, propenso a crear zombies si no se limpian en `onUnmounted`.

**Acción de Remediación:** Extraer toda invocación de `apiClient` hacia acciones en `Pinia` o Composables (`useQuery`, `useMutation`). Migrar todos los `setInterval` restantes al ciclo global del motor `startEngine()`.

### 3. Trazabilidad y Zero-Mock (Ley Global 3 y ADR-010)

*   **[Bajo] Trazabilidad Inyectada pero Anémica:** Se confirma mediante análisis heurístico que el marcador `// @Traceability` existe masivamente en los controladores del Backend (ej. `TaskController`, `FormStorageController`). Sin embargo, el almacenamiento del `correlation_id` (US-038 CA-09) en entidades satélite sigue sin concretarse a nivel de esquema de base de datos.
*   **[Ambigüedad] Zero-Mock vs Mock Interno de UI:** Existe ambigüedad funcional en los Fuzzers (ej. Simulador Zod US-028) donde el "Mock" se erradicó a nivel de red, pero la UI sigue emulando un "Happy Path" mediante Teleport para bypassear validaciones complejas de formularios no desarrolladas completamente.

---

## Conclusión del Arquitecto
Las historias listadas presentan un "Falso Positivo" en su grado de madurez arquitectónica. Aunque funcionan en entornos E2E bajo el esquema "Happy Path", el Backend exhibe un fuerte acoplamiento a base de datos (Anti-Hexagonal) y el Frontend adolece del anti-patrón "Smart Components" y fugas de temporizadores. 

**Recomendación Inmediata:** Despachar un Handoff de Refactorización Retroactiva transversal (Cross-Cutting) enfocado en limpiar los Controladores (Backend) y centralizar el estado (Frontend) antes de intentar integrar nuevas épicas como la US-004 (Async RabbitMQ).
