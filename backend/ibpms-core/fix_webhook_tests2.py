import os
import re

test_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\src\test\java\com\ibpms\poc\application\service\webhook"

for filename in os.listdir(test_dir):
    if filename.endswith(".java"):
        filepath = os.path.join(test_dir, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
        
        # We need to find new WebhookIntakeService(...) with exactly 7 arguments
        # It's safer to just replace new WebhookIntakeService(..., props) with new WebhookIntakeService(..., props, null) if it doesn't already have it
        # Or mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class)
        
        lines = content.split('\n')
        changed = False
        for i, line in enumerate(lines):
            if "new WebhookIntakeService(" in line and "props)" in line:
                # Add mock if not present
                if "mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class)" not in line and "null)" not in line:
                    lines[i] = line.replace("props)", "props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class))")
                    changed = True
            
            # Special case for PurgeRejectedPayloadsTest if it has a multi-line or strange syntax
            # The compiler error says: <nulltype>,com.ibpms.poc.domain.port.OrphanPayloadRepository,<nulltype>,com.ibpms.poc.domain.port.TriageTaskRepository,<nulltype>,<nulltype>,com.ibpms.poc.infrastructure.config.WebhookProperties
            if "new WebhookIntakeService(null," in line and "props)" in line:
                if "mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class)" not in line and "null)" not in line:
                    lines[i] = line.replace("props)", "props, null)")
                    changed = True
                    
        if changed:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write('\n'.join(lines))
            print("Fixed", filename)
