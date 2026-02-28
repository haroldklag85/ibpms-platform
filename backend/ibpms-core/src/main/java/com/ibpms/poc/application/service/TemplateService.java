package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.AppBuilderUseCase;
import com.ibpms.poc.application.port.out.UiTemplatePersistencePort;
import com.ibpms.poc.domain.model.TemplateType;
import com.ibpms.poc.domain.model.UiTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService implements AppBuilderUseCase {

    private final UiTemplatePersistencePort persistencePort;

    public TemplateService(UiTemplatePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public UiTemplate saveTemplate(String name, TemplateType type, String rawCode) {
        Optional<UiTemplate> latestTemplate = persistencePort.findLatestByName(name);
        String nextVersion = "v1.0.0";

        if (latestTemplate.isPresent()) {
            nextVersion = bumpMinorVersion(latestTemplate.get().getVersion());
        }

        UiTemplate newTemplate = UiTemplate.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .type(type)
                .rawCode(rawCode)
                .version(nextVersion)
                .createdAt(LocalDateTime.now())
                .build();

        return persistencePort.save(newTemplate);
    }

    @Override
    public UiTemplate getLatestTemplateByName(String name) {
        return persistencePort.findLatestByName(name)
                .orElseThrow(() -> new RuntimeException("Template no encontrado: " + name));
    }

    @Override
    public List<UiTemplate> getVersionHistory(String name) {
        return persistencePort.findAllVersionsByName(name);
    }

    /**
     * Incrementa la version minor de SemVer vX.Y.Z -> vX.(Y+1).0
     */
    private String bumpMinorVersion(String currentVersion) {
        Pattern pattern = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(currentVersion);
        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2)) + 1;
            return "v" + major + "." + minor + ".0";
        }
        return "v1.1.0"; // Fallback
    }
}
