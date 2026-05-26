## Aggregated Findings
Explorer 3 has identified the necessary changes:

1. **Adapter Relocation**: 
   - `SharePointAdapterService.java` is in `com.ibpms.poc.application.service.sgdea.SharePointAdapterService`.
   - `MsGraphWebClientAdapter.java` is in `com.ibpms.poc.infrastructure.web.client.MsGraphWebClientAdapter`.
   - Both should be moved to `infrastructure/adapters/external`. Change `SharePointAdapterService` from `@Service` to `@Component`. Update package declarations and all imports referencing them.

2. **Consumer Implementation**:
   - Create `WebhookIntakeConsumer.java` in `com.ibpms.poc.infrastructure.adapters.inbound.messaging`.
   - Replicate the logic of the old listener (`WebhookIntakeListener.java`, which should be deleted).
   - Consume the queue: `@RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)`.

3. **Traceability**:
   - Decorate `WebhookIntakeConsumer` with LEY GLOBAL 3 annotation: `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})`.

## Action Plan for Worker
- Execute the relocation of the two adapters. Fix all compilation errors caused by broken imports.
- Delete `WebhookIntakeListener.java` and create the new `WebhookIntakeConsumer.java` following hexagonal architecture.
- Apply the `@Traceability` annotation as required.
- Build and run tests to ensure correctness (`mvn clean package -DskipTests`).
