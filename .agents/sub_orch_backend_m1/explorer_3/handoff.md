# Handoff Report

## 1. Observation
- `SharePointAdapterService.java` is currently located at `com.ibpms.poc.application.service.sgdea.SharePointAdapterService`. It is annotated with `@Service`.
- `MsGraphWebClientAdapter.java` is currently located at `com.ibpms.poc.infrastructure.web.client.MsGraphWebClientAdapter`. It is annotated with `@Component`.
- According to `SCOPE.md`, these adapters belong in the external infrastructure adapters package.
- There is an existing listener, `WebhookIntakeListener.java`, at `com.ibpms.poc.infrastructure.messaging` which consumes the webhook queue.
- `SCOPE.md` dictates creating a new `WebhookIntakeConsumer` in the "inbound infrastructure messaging adapters package" (`com.ibpms.poc.infrastructure.adapters.inbound.messaging`).
- The `RabbitMqTopologyConfig` defines the queue constant: `public static final String QUEUE_INTEGRATIONS_WEBHOOK = "ibpms.integrations.webhook";`
- The `@Traceability` annotation is located at `com.ibpms.poc.crosscutting.annotations.Traceability` and takes two parameters: `US` (String) and `CA` (String array).

## 2. Logic Chain
- **Adapter Relocation:** Moving `SharePointAdapterService` and `MsGraphWebClientAdapter` to `com.ibpms.poc.infrastructure.adapters.external` adheres to the hexagonal architecture constraints defined for the project (outbound external adapters). The package declaration must be changed, and all importing classes will require updated imports. For `SharePointAdapterService`, replacing `@Service` with `@Component` is conceptually more accurate for an infrastructure adapter.
- **Consumer Implementation:** Creating `WebhookIntakeConsumer` in `com.ibpms.poc.infrastructure.adapters.inbound.messaging` fulfills the target layout for inbound messaging adapters. It should replicate the webhook payload processing (delegating to `WebhookIntakeService`) and be annotated with `@RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)`.
- **Traceability Application:** To comply with LEY GLOBAL 3 (US-004, CA-6, CA-8), the consumer class or its listener method must be decorated with `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})`.

## 3. Caveats
- Since a workspace-wide reference search timed out, the implementer will need to rely on their IDE refactoring tools or a workspace-wide text replace to update all imports for the moved adapter classes.
- The existing `WebhookIntakeListener.java` must likely be deleted to avoid duplicate queue consumers and unexpected race conditions, as `WebhookIntakeConsumer.java` effectively replaces it.

## 4. Conclusion
1. **Move Adapters**: Relocate `SharePointAdapterService.java` and `MsGraphWebClientAdapter.java` to `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/external`. Update their `package` statements and fix any imports across the project. Change `@Service` to `@Component` in `SharePointAdapterService`.
2. **Create Consumer**: Create `WebhookIntakeConsumer.java` in `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/inbound/messaging`. 
3. **Configure Consumer**: Inject `WebhookIntakeService` and create a method to consume payloads. Annotate the class or method with `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})` and `@RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)`.
4. **Cleanup**: Delete the old `WebhookIntakeListener.java`.

## 5. Verification Method
- Ensure the project compiles successfully using the build tool (`mvn clean compile` or `./gradlew build`) to confirm no dangling imports remain.
- Inspect the file system to ensure `SharePointAdapterService.java` and `MsGraphWebClientAdapter.java` are strictly in the `infrastructure/adapters/external` directory.
- Inspect `WebhookIntakeConsumer.java` to confirm the presence of both `@RabbitListener` and `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})`.
