# Backend M1 (US-004) - Handoff Report

## 1. Observation

- **Adapters Current Locations**:
  - `SharePointAdapterService.java` is currently located at `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/sgdea/SharePointAdapterService.java` (Package: `com.ibpms.poc.application.service.sgdea`).
  - `MsGraphWebClientAdapter.java` is currently located at `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/client/MsGraphWebClientAdapter.java` (Package: `com.ibpms.poc.infrastructure.web.client`).
- **RabbitMQ Config**: `RabbitMqTopologyConfig.java` defines `QUEUE_INTEGRATIONS_WEBHOOK = "ibpms.integrations.webhook"`.
- **Existing Consumer**: There is currently a `WebhookIntakeListener.java` in `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/messaging/` consuming the string `"ibpms.integrations.webhook"`.
- **Traceability Annotation**: `@Traceability` is located at `backend/ibpms-core/src/main/java/com/ibpms/poc/crosscutting/annotations/Traceability.java`. It accepts `US` (String) and `CA` (String array), and its `@Target` allows it on both `TYPE` and `METHOD`.

## 2. Logic Chain

- **Adapters Target**: According to the hexagonal architecture scope in `SCOPE.md`, external adapters belong in `infrastructure/adapters/external`. Therefore:
  - Both adapters should be moved to `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/external/`.
  - Their package declarations must be updated to `package com.ibpms.poc.infrastructure.adapters.external;`.
  - Any imports in other files referencing their old packages must be updated to the new one. (e.g. `import com.ibpms.poc.application.service.sgdea.SharePointAdapterService;` to `import com.ibpms.poc.infrastructure.adapters.external.SharePointAdapterService;`).
- **Consumer Target**: The scope indicates the consumer belongs in the "inbound infrastructure messaging adapters package". The proper package path is `com.ibpms.poc.infrastructure.adapters.messaging.inbound`.
- **Traceability**: The requirements specify adding traceability for "LEY GLOBAL 3 (US-004, CA-6, CA-8)". This translates to the annotation `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})`, which can be applied to `WebhookIntakeConsumer` class or its consumer method.
- **Consumer Implementation**: The consumer needs to read from `RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK` inside its `@RabbitListener` annotation.

## 3. Caveats

- We observed an existing `WebhookIntakeListener.java` consuming the same queue. Creating `WebhookIntakeConsumer` without removing or replacing `WebhookIntakeListener` will cause RabbitMQ to round-robin messages between them, leading to unpredictable processing. It is recommended to replace `WebhookIntakeListener` with the new `WebhookIntakeConsumer`.
- A search for usages of the adapters could not be completed via script due to OS/command runner limitations; therefore, the implementer will need to do a project-wide search-and-replace for the old package paths to ensure there are no broken imports.

## 4. Conclusion

**Implementation Strategy:**

1. **Move and Refactor Adapters**:
   - Move `SharePointAdapterService.java` and `MsGraphWebClientAdapter.java` to `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/external/`.
   - Update their package declarations to `com.ibpms.poc.infrastructure.adapters.external`.
   - Perform a codebase-wide find-and-replace for `com.ibpms.poc.application.service.sgdea.SharePointAdapterService` and `com.ibpms.poc.infrastructure.web.client.MsGraphWebClientAdapter` to update imports.
2. **Create Consumer**:
   - Create `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/messaging/inbound/WebhookIntakeConsumer.java`.
   - Define package as `com.ibpms.poc.infrastructure.adapters.messaging.inbound`.
   - Annotate the class or its consumption method with `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})`.
   - Add `@RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)` to the listening method.
   - Inject necessary services (like `WebhookIntakeService`) and port the logic from `WebhookIntakeListener` into this new consumer.
3. **Cleanup**:
   - Delete the old `WebhookIntakeListener.java` in `com.ibpms.poc.infrastructure.messaging` to prevent duplicate consumption of the webhook queue.

## 5. Verification Method

- Check the file tree to ensure `SharePointAdapterService.java` and `MsGraphWebClientAdapter.java` are in `infrastructure/adapters/external/`.
- Ensure `WebhookIntakeConsumer.java` exists in `infrastructure/adapters/messaging/inbound/` and contains the `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})` annotation.
- Run `mvn clean compile` or the equivalent Maven/Gradle build command on the `backend` to guarantee that all package imports are correct and there are no compilation errors.
