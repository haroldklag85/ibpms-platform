# Handoff Report: Backend M1 (US-004) Investigation

## Observation
1. **Adapters:**
   - `SharePointAdapterService.java` is currently located at `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/sgdea/SharePointAdapterService.java` (package: `com.ibpms.poc.application.service.sgdea`).
   - `MsGraphWebClientAdapter.java` is currently located at `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/client/MsGraphWebClientAdapter.java` (package: `com.ibpms.poc.infrastructure.web.client`).
   - `SCOPE.md` directs moving these to the "correct infrastructure adapters package (external adapters)".

2. **Consumer:**
   - There is an existing `WebhookIntakeListener.java` at `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/messaging/WebhookIntakeListener.java`.
   - `SCOPE.md` directs creating a `WebhookIntakeConsumer.java` in the "inbound infrastructure messaging adapters package" using `@RabbitListener` and `@Traceability`.
   - The queue constant is `RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK`.

3. **Traceability:**
   - The `@Traceability` annotation is located at `backend/ibpms-core/src/main/java/com/ibpms/poc/crosscutting/annotations/Traceability.java`.
   - It takes attributes `US` (String) and `CA` (String array).

## Logic Chain
1. **Target Adapter Locations:** Based on hexagonal architecture and the `SCOPE.md`, the adapters must be moved to the infrastructure layer, specifically `com.ibpms.poc.infrastructure.adapters.external`. The directories will need to be created.
2. **Consumer Target Location:** The new consumer should be created under an inbound messaging adapters package, which logically maps to `com.ibpms.poc.infrastructure.adapters.inbound.messaging` (or `com.ibpms.poc.infrastructure.messaging.adapters`). It should replace or subsume the logic in the current `WebhookIntakeListener`.
3. **Traceability Application:** The annotation must be applied to `WebhookIntakeConsumer` as `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})` on the class or consumer method.
4. **Import/Build Issues & Edge Cases:** 
   - Moving `SharePointAdapterService` out of `application.service.sgdea` means any services in that package that rely on it will need updated `import` statements.
   - `MsGraphWebClientAdapter` implements an outbound port (`MsGraphClientPort`), so direct imports might be rare, but should be checked.
   - The `@RabbitListener` queue name requires importing `RabbitMqTopologyConfig` and using its static constant `queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK`.

## Caveats
- Direct global search (grep) across the codebase for imports was unavailable due to environment constraints. Therefore, the implementing agent will need to actively update imports using IDE features or manual find-and-replace for `com.ibpms.poc.application.service.sgdea.SharePointAdapterService` and `com.ibpms.poc.infrastructure.web.client.MsGraphWebClientAdapter`.
- The exact package for "inbound infrastructure messaging adapters" could vary based on existing conventions, but `com.ibpms.poc.infrastructure.adapters.inbound.messaging` is recommended. The implementing agent should ensure it aligns with any undocumented structure.

## Conclusion
- Move `SharePointAdapterService.java` to `.../infrastructure/adapters/external/SharePointAdapterService.java`.
- Move `MsGraphWebClientAdapter.java` to `.../infrastructure/adapters/external/MsGraphWebClientAdapter.java`.
- Create `WebhookIntakeConsumer.java` in `.../infrastructure/adapters/inbound/messaging/` (likely migrating logic from `WebhookIntakeListener`).
- Annotate the new consumer with `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})` and `@RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)`.

## Verification Method
- Execute the build command (e.g., `mvn clean compile` or `./mvnw clean compile` in the backend directory) to ensure all imports are correctly updated.
- Use `view_file` to confirm the package declarations and location of the moved files.
- Inspect `WebhookIntakeConsumer.java` to verify the `@Traceability` and `@RabbitListener` annotations are correct.
