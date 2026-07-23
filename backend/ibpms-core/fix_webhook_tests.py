import os

test_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\src\test\java\com\ibpms\poc\application\service\webhook"

for filename in os.listdir(test_dir):
    if filename.endswith(".java"):
        filepath = os.path.join(test_dir, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Replace the constructor call
        old_call = "new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props);"
        new_call = "new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class));"
        
        old_call_2 = "new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), mock(com.ibpms.poc.domain.port.ClamAvScanner.class), runtimeService, props);"
        new_call_2 = "new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), mock(com.ibpms.poc.domain.port.ClamAvScanner.class), runtimeService, props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class));"
        
        if old_call in content or old_call_2 in content or "WebhookIntakeService(" in content:
            # Let's use regex for a generic replace if exact doesn't match
            import re
            content = re.sub(r'new WebhookIntakeService\((.*?runtimeService,\s*props)\);', r'new WebhookIntakeService(\1, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class));', content)

            with open(filepath, "w", encoding="utf-8") as f:
                f.write(content)
            print("Fixed", filename)
