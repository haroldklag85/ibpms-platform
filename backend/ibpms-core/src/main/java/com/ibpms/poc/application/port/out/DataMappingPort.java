package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.DataMapping;
import java.util.List;

public interface DataMappingPort {
    List<DataMapping> findByProcessDefinitionKey(String processDefinitionKey);
    DataMapping save(DataMapping dataMapping);
}
