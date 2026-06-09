package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.ExternalTaskTopic;
import java.util.List;

public interface ExternalTaskTopicPort {
    List<ExternalTaskTopic> findByIsActiveTrue();
    List<ExternalTaskTopic> findAll();
}
