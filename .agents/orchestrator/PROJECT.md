# Project: US-004

## Architecture
- Backend: Java Spring Boot with Hexagonal Architecture. Adaptors belong in `infrastructure`. Interfaces belong in `domain` or `application/ports`. Webhook consumed via RabbitMQ.
- Frontend: Vue 3 with Pinia for state management. Uses Tailwind CSS. Dumb components pattern for UI, delegating side effects/requests to Pinia stores.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Backend - M1 | Mover `SharePointAdapterService.java` y `MsGraphWebClientAdapter.java` a la capa de infraestructura (ej. `external`). Crear `WebhookIntakeConsumer.java` que escuche `QUEUE_INTEGRATIONS_WEBHOOK`. Aplicar `@Traceability: US-004, CA-6, CA-8`. | none | IN_PROGRESS |
| 2 | Frontend - M2 | Crear Pinia store `useIntakeTriageStore.ts` y dumb component `IntakeTriageView.vue`. Estilar con TailwindCSS y añadir a Router. Aplicar `@Traceability: US-004, CA-6, CA-8`. | none | DONE |

## Interface Contracts
### WebhookIntakeConsumer ↔ RabbitMQ
- Queue: `RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK`
- Listener: `@RabbitListener`

### IntakeTriageView ↔ useIntakeTriageStore
- View simply binds to data exposed by store, dispatches actions to store, and makes no direct HTTP requests.
