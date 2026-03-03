package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotNull;

public class DeployRequestDTO {

    public enum MigrationStrategy {
        COEXIST, FORCE_MIGRATE
    }

    @NotNull(message = "La estrategia de migración es obligatoria (COEXIST o FORCE_MIGRATE).")
    private MigrationStrategy migrationStrategy;

    public MigrationStrategy getMigrationStrategy() {
        return migrationStrategy;
    }

    public void setMigrationStrategy(MigrationStrategy migrationStrategy) {
        this.migrationStrategy = migrationStrategy;
    }
}
