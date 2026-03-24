package com.ibpms.poc.application.service.form;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormDirectoryService {

    // Estructura en memoria según requerimiento de Misión (Evasión de BD compleja para acelerar Boot)
    private final List<Map<String, Object>> mockDirectory = List.of(
        Map.of("id", "FRM-001", "name", "Solicitud de Crédito Express", "type", "FINANCIAL", "version", "1.0", "author", "System", "updatedAt", LocalDateTime.now().minusDays(1).toString()),
        Map.of("id", "FRM-002", "name", "Alta de Empleado (Onboarding)", "type", "HR", "version", "2.1", "author", "Admin", "updatedAt", LocalDateTime.now().minusHours(5).toString()),
        Map.of("id", "FRM-003", "name", "Reclamación Seguro (PQR)", "type", "LEGAL", "version", "1.5", "author", "AuditAgent", "updatedAt", LocalDateTime.now().toString())
    );

    public List<Map<String, Object>> searchForms(String query) {
        if (query == null || query.isBlank()) {
            return mockDirectory;
        }
        String lowerQuery = query.toLowerCase();
        return mockDirectory.stream()
            .filter(f -> ((String) f.get("name")).toLowerCase().contains(lowerQuery) 
                      || ((String) f.get("id")).toLowerCase().contains(lowerQuery)
                      || ((String) f.get("type")).toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
    }
}
