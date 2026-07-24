package com.ibpms.poc.application.service.form;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormDirectoryService {

    private final com.ibpms.poc.application.service.FormDesignService formDesignService;

    public FormDirectoryService(com.ibpms.poc.application.service.FormDesignService formDesignService) {
        this.formDesignService = formDesignService;
    }

    public List<Map<String, Object>> searchForms(String query) {
        List<com.ibpms.poc.application.dto.FormDesignDTO> allActiveForms = formDesignService.listarCatalogo();

        List<Map<String, Object>> directory = allActiveForms.stream()
            .map(f -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", f.getTechnicalName());
                map.put("name", f.getName());
                map.put("type", f.getPattern());
                map.put("version", f.getVersion() != null ? f.getVersion().toString() : "1.0");
                map.put("author", f.getAuthorId());
                map.put("updatedAt", f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : java.time.LocalDateTime.now().toString());
                return map;
            })
            .collect(Collectors.toList());

        if (query == null || query.isBlank()) {
            return directory;
        }
        
        String lowerQuery = query.toLowerCase();
        return directory.stream()
            .filter(f -> {
                String name = (String) f.get("name");
                String id = (String) f.get("id");
                String type = (String) f.get("type");
                return (name != null && name.toLowerCase().contains(lowerQuery))
                    || (id != null && id.toLowerCase().contains(lowerQuery))
                    || (type != null && type.toLowerCase().contains(lowerQuery));
            })
            .collect(Collectors.toList());
    }
}
