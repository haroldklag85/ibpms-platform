# 🧠→⚙️ Handoff: Arquitecto Líder → Backend Java
# T-20: Retro-Remediación ADR-001 (Controladores vs Repositorios)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-12T19:30:00-05:00
**Sprint:** 7 — Iteración de Consolidación
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/ADR-001_Hexagonal_Architecture.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: Retro-Remediación ADR-001`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

La Auditoría V2 transversal ha revelado una violación Crítica del ADR-001 (Arquitectura Hexagonal). Los adaptadores de entrada (Controladores) están inyectando directamente adaptadores de salida (Repositorios JPA), rompiendo la inversión de dependencias y la capa de Casos de Uso. Esto imposibilita el aislamiento para pruebas unitarias.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Inyección de Repositorio en Inbound | `DynamicWebhookRouterController.java:27` | Inyecta `InboundWebhookRepository` |
| Inyección de Repositorio en Inbound | `RbacAdminController.java:26-28` | Inyecta `IbpmsProfileRepository`, `IdpGroupMappingRepository`, `ProfileBpmnAssignmentRepository` |
| Inyección de Repositorio en Inbound | `AuthSyncController.java:21-24` | Inyecta `UserRepository` y `SystemAuditLogRepository` |
| Inyección de Repositorio en Inbound | `TaskController.java:44` | Inyecta `FormFieldValueAuditRepository` |
| Inyección de Repositorio en Inbound | `TaskSkipController.java:22` | Inyecta `SkipAuditRepository` |
| Inyección de Repositorio en Inbound | `AuditReportController.java:27` | Inyecta `IdentityRepository` |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Refactorización de Controladores hacia Casos de Uso/Servicios

**Archivo:** Múltiples (todos los listados en el Diagnóstico).
**Acción:** Extirpar la inyección directa de `*Repository`. Crear/Actualizar las interfaces de Servicio (Outbound Ports / UseCases) que envuelvan estas llamadas.

```java
// Snippet prescriptivo — AuthSyncController.java (Ejemplo de Remediación)
// @Traceability: Retro-Remediación ADR-001 - Extirpación de Repositorios en Controllers

// INCORRECTO:
// private final UserRepository userRepository;

// CORRECTO:
private final UserManagementUseCase userManagementUseCase;
private final SystemAuditLogUseCase systemAuditLogUseCase;

@PostMapping("/sync")
public ResponseEntity<?> syncUser(...) {
    // La mutación ocurre en el servicio, no en el controlador
    userManagementUseCase.syncUserFromIdp(email, idpData);
    return ResponseEntity.ok().build();
}
```

### Paso 2: Eliminación de `@Transactional` en Capa Web

Asegúrate de que ninguna anotación `@Transactional` resida en los Controladores. Si alguna lógica cruda de base de datos estaba en el Controlador, muévela al `@Service` correspondiente e inyecta la trazabilidad.

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Ningún Repositorio es inyectado en la capa Web | `grep -r "import.*repository.*" backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/*.java` debe devolver 0 resultados directos (salvo filtros/security si está estrictamente justificado, pero NUNCA en `@RestController`). |
| 2 | Trazabilidad Inversa Inyectada | Cada Controlador y Servicio modificado debe contener el comentario `// @Traceability: Retro-Remediación ADR-001`. |
| 3 | Compilación Exitosa | Ejecución de `mvn clean verify -DskipTests` finaliza en SUCCESS. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Buscar y aislar todos los `@RestController` que inyecten `JpaRepository`.
2. Crear o actualizar las clases `@Service` (o interfaces `UseCase`) para albergar la lógica.
3. Inyectar los Servicios en los Controladores.
4. Agregar marcadores `@Traceability`.
5. Ejecutar compilación: `mvn clean verify -DskipTests` en `backend/ibpms-core`.
6. Commit: `git add . && git commit -m "refactor(backend): extraer repositorios de capa web (ADR-001)" && git push`

```