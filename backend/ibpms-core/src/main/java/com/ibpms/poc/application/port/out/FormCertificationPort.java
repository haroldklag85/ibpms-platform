package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.FormCertification;
import java.util.Optional;
import java.util.UUID;

public interface FormCertificationPort {
    Optional<FormCertification> findByFormDefinitionId(UUID formDefinitionId);
    FormCertification save(FormCertification formCertification);
}
