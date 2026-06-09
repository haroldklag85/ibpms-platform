# Scope: Backend M1 (US-004)

## Architecture
Hexagonal Architecture

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Move Adapters | SharePointAdapterService, MsGraphWebClientAdapter | none | DONE |
| 2 | WebhookConsumer | Create WebhookIntakeConsumer with @RabbitListener and @Traceability | none | DONE |

## Code Layout
Adapters should be moved to correct infrastructure adapters package (external adapters).
Consumer should be in inbound infrastructure messaging adapters package.
