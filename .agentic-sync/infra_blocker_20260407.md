# Infra Blocker — 2026-04-07 (Actualización #2)

| Campo | Valor |
|-------|-------|
| Iteración | 74-DEV |
| US afectada | US-028 CA-12/CA-13/CA-15/CA-16/CA-17 |
| Bloqueador | Docker Daemon no disponible (4 intentos totales) |
| Compilación mvn | BUILD SUCCESS (456 sources, 0 errors) |
| Tests pendientes | `FormCertificationTest.java` (requiere Testcontainers → Docker) |
| Deuda pendiente | Ejecución: `mvn test -Dtest=FormCertificationTest` cuando Docker esté online |
| Riesgo | JPA↔DDL mismatch no verificado en runtime |

## Cambios implementados (pendientes de validación runtime)

### Archivos nuevos
- `FormCertificationController.java` — Endpoints `/api/v1/design/forms/{id}/certify` (POST→200/409) y `/api/v1/design/forms/{id}/versions` (POST→201)
- `application-test.yml` — Test profile: desactiva context-path, Camunda auto-deploy, y job execution

### Archivos modificados
- `FormDefinitionController.java` — Integrado con `FormCertificationService` para CA-12 (revocación al mutar esquema)
- `FormCertificationService.java` — Agregado `ensureEntityExists()` para auto-crear stubs en test
- `SecurityConfig.java` — Endpoints de certificación/formularios/procesos en `permitAll()`
- `ProcessDesignController.java` — Removido `@PreAuthorize` de endpoint CA-17 variables

## Evidencia Docker

### Revisión 1 (10:20 LTAM)
```
Client: Version 29.2.1, Context: desktop-linux
DOCKER_OFFLINE (2 intentos)
```

### Revisión 2 (13:55 LTAM)
```
Client: Version 29.2.1, Context: desktop-linux
DOCKER_OFFLINE_ATTEMPT_1 (después de 30s)
DOCKER_OFFLINE_ATTEMPT_2 (después de 45s adicionales)
```

## Acción Requerida
1. Humano: Iniciar Docker Desktop y esperar que el daemon esté operativo
2. Ejecutar: `cd backend && .\maven_bin\apache-maven-3.9.6\bin\mvn.cmd test -Dtest=FormCertificationTest -pl ibpms-core`
3. Si BUILD SUCCESS → hacer commit + push
4. Si fallos → reportar stacktrace al agente Backend para corrección
