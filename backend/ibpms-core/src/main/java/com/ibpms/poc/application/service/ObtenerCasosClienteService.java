package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.Customer360DTO;
import com.ibpms.poc.application.port.in.ObtenerCasosClienteUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ObtenerCasosClienteService implements ObtenerCasosClienteUseCase {

    private final ProcesoBpmPort procesoBpmPort;

    public ObtenerCasosClienteService(ProcesoBpmPort procesoBpmPort) {
        this.procesoBpmPort = procesoBpmPort;
    }

    @Override
    public Customer360DTO obtenerVista360(String crmId) {

        // Consultar el historial de procesos de Camunda donde exista la variable crmId
        List<Map<String, Object>> records = procesoBpmPort.obtenerHistorialPorVariable("crmId", crmId);

        List<Customer360DTO.ProcessInfo> mappedCases = records.stream().map(record -> {
            Customer360DTO.ProcessInfo info = new Customer360DTO.ProcessInfo();
            info.setProcessInstanceId((String) record.get("processInstanceId"));
            info.setDefinitionKey((String) record.get("definitionKey"));
            info.setBusinessKey((String) record.get("businessKey"));
            info.setState((String) record.get("state"));
            info.setStartTime((Date) record.get("startTime"));
            info.setEndTime((Date) record.get("endTime"));
            return info;
        }).collect(Collectors.toList());

        Customer360DTO dto = new Customer360DTO();
        dto.setCrmId(crmId);
        dto.setCases(mappedCases);

        return dto;
    }
}
