package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;

public class UiTemplate {

    private final String id;
    private final String name;
    private final TemplateType type;
    private final String rawCode;
    private final String version;
    private final LocalDateTime createdAt;

    public UiTemplate(String id, String name, TemplateType type, String rawCode, String version,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rawCode = rawCode;
        this.version = version;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TemplateType getType() {
        return type;
    }

    public String getRawCode() {
        return rawCode;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private TemplateType type;
        private String rawCode;
        private String version;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(TemplateType type) {
            this.type = type;
            return this;
        }

        public Builder rawCode(String rawCode) {
            this.rawCode = rawCode;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UiTemplate build() {
            return new UiTemplate(id, name, type, rawCode, version, createdAt);
        }
    }
}
