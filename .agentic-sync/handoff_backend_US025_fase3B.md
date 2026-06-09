# 🟠 Handoff Backend — US-025 Fase 3B: Impersonación "Ver Sistema Como"

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Backend  
> **Prioridad:** 🟡 P1 — **APROBADO POR PO PARA V1**  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 1B completada (MenuTopologyPort + RoleHierarchyPort existentes)  
> **Gate de Salida:** `mvn test` 100% pass + `mvn package` limpio

---

## 1. Contexto

El PO ha aprobado CA-9 (Impersonación "Ver Sistema Como") y CA-31 (Trazabilidad Impersonator) para V1. Esto requiere desarrollo backend completo:

1. **ImpersonationUseCase** — Caso de uso hexagonal que genera un JWT híbrido con el claim `ImpersonatedBy`
2. **Endpoint REST** — `POST /api/v1/admin/impersonate/{targetUserId}` + `POST /api/v1/admin/impersonate/exit`
3. **Audit Trail** — Toda impersonación queda registrada en `ibpms_impersonation_audit_log`

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-001 (Hexagonal) | UseCase en `application/`, Puerto en `ports/out/`, Adaptador en `infrastructure/` |
| ADR-009 (PostgreSQL) | Tabla audit log vía Liquibase |
| ADR-010 (Testing) | Tests de integración obligatorios |
| C4-Model | Agregar `ImpersonationUseCase` al Level 3 Component |

**Restricciones de Seguridad Zero-Trust:**
- SOLO usuarios con `ROLE_SUPER_ADMIN` pueden impersonar
- Un admin NO puede impersonar a otro `ROLE_SUPER_ADMIN`
- La impersonación tiene TTL máximo de 30 minutos
- Toda impersonación genera entry en audit log (no omitible)

---

## 3. Tareas

### Tarea 3B.1 — Crear Puerto `ImpersonationPort`

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/ports/out/ImpersonationPort.java`

```java
package com.ibpms.poc.application.ports.out;

import java.util.UUID;

/**
 * Puerto de salida para persistir el registro de auditoría de impersonación.
 */
public interface ImpersonationPort {
    
    /**
     * Registra una acción de impersonación en el audit log.
     */
    void logImpersonation(UUID adminId, UUID targetUserId, String action, String metadata);
    
    /**
     * Verifica si el usuario objetivo es impersonable (no es SUPER_ADMIN).
     */
    boolean isImpersonable(UUID targetUserId);
}
```

---

### Tarea 3B.2 — Crear `ImpersonationUseCase`

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/security/ImpersonationUseCase.java`

**Lógica de negocio:**
```java
@Service
public class ImpersonationUseCase {

    private final ImpersonationPort impersonationPort;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * CA-9: Inicia impersonación. Genera JWT híbrido.
     * @param adminId UUID del admin que impersona
     * @param targetUserId UUID del usuario objetivo
     * @return JWT con claims: sub=targetUserId, impersonatedBy=adminId, exp=30min
     * @throws SecurityException si targetUserId es SUPER_ADMIN
     */
    public String startImpersonation(UUID adminId, UUID targetUserId) {
        // Guardia 1: No impersonar a otro admin
        if (!impersonationPort.isImpersonable(targetUserId)) {
            throw new SecurityException("No se puede impersonar a un SUPER_ADMIN");
        }
        
        // Guardia 2: Registrar en audit log
        impersonationPort.logImpersonation(adminId, targetUserId, "START", null);
        
        // Generar JWT híbrido con claim extra
        Map<String, Object> extraClaims = Map.of(
            "impersonatedBy", adminId.toString(),
            "impersonationMode", true
        );
        return jwtTokenProvider.generateToken(targetUserId, extraClaims, Duration.ofMinutes(30));
    }

    /**
     * CA-31: Finaliza impersonación. Registra en audit log.
     */
    public void exitImpersonation(UUID adminId, UUID targetUserId) {
        impersonationPort.logImpersonation(adminId, targetUserId, "EXIT", null);
    }
}
```

---

### Tarea 3B.3 — Crear Adaptador `ImpersonationJpaAdapter`

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/security/ImpersonationJpaAdapter.java`

```java
@Component
public class ImpersonationJpaAdapter implements ImpersonationPort {
    
    private final JdbcTemplate jdbcTemplate;
    private final RoleTemplateRepository roleTemplateRepository;

    @Override
    public void logImpersonation(UUID adminId, UUID targetUserId, String action, String metadata) {
        jdbcTemplate.update(
            "INSERT INTO ibpms_impersonation_audit_log (admin_id, target_user_id, action, metadata, created_at) VALUES (?, ?, ?, ?::jsonb, NOW())",
            adminId, targetUserId, action, metadata
        );
    }

    @Override
    public boolean isImpersonable(UUID targetUserId) {
        // Verificar que el usuario no tiene ROLE_SUPER_ADMIN
        return !roleTemplateRepository.existsByUserIdAndRoleName(targetUserId, "ROLE_SUPER_ADMIN");
    }
}
```

---

### Tarea 3B.4 — Crear Controller `ImpersonationController`

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/ImpersonationController.java`

```java
@RestController
@RequestMapping("/api/v1/admin/impersonate")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ImpersonationController {

    private final ImpersonationUseCase impersonationUseCase;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<Map<String, String>> startImpersonation(
            @PathVariable UUID targetUserId, Authentication auth) {
        UUID adminId = extractUserId(auth);
        String hybridToken = impersonationUseCase.startImpersonation(adminId, targetUserId);
        return ResponseEntity.ok(Map.of(
            "token", hybridToken,
            "impersonating", targetUserId.toString(),
            "expiresIn", "1800" // 30 min
        ));
    }

    @PostMapping("/exit")
    public ResponseEntity<Void> exitImpersonation(Authentication auth) {
        // Extraer adminId del claim 'impersonatedBy' del JWT actual
        UUID adminId = extractImpersonatorId(auth);
        UUID targetUserId = extractUserId(auth);
        impersonationUseCase.exitImpersonation(adminId, targetUserId);
        return ResponseEntity.noContent().build();
    }
}
```

---

### Tarea 3B.5 — Modificar `JwtAuthFilter` para claims de impersonación

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/security/JwtAuthFilter.java`

Al parsear el JWT, si existe el claim `impersonatedBy`:
1. Incluir el claim en los `GrantedAuthority` como `IMPERSONATING` flag
2. Propagar `impersonatedBy` en el `SecurityContext` para audit trail
3. Verificar que el TTL del JWT híbrido no exceda 30 minutos

---

### Tarea 3B.6 — Tests de integración Impersonación

**Crear:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/ImpersonationControllerIT.java`

**Escenarios:**
1. `POST /admin/impersonate/{userId}` con `ROLE_SUPER_ADMIN` → HTTP 200 + JWT híbrido con claim `impersonatedBy`
2. `POST /admin/impersonate/{adminId}` impersonando a otro admin → HTTP 403 (prohibido)
3. `POST /admin/impersonate/{userId}` sin `ROLE_SUPER_ADMIN` → HTTP 403
4. `POST /admin/impersonate/exit` con JWT híbrido → HTTP 204 + audit log entry
5. Verificar audit log: `SELECT * FROM ibpms_impersonation_audit_log WHERE admin_id = ?` retorna registros

---

## 4. Criterios de Aceptación del Gate

- [ ] `ImpersonationUseCase` sin imports de `infrastructure.*`
- [ ] `ImpersonationPort` interface en `application/ports/out/`
- [ ] JWT híbrido contiene claims `impersonatedBy` + `impersonationMode`
- [ ] TTL del JWT impersonado = 30 minutos máximo
- [ ] Guardia: no impersonar a otro SUPER_ADMIN
- [ ] Audit log registra START y EXIT
- [ ] 5 tests de integración pasando
- [ ] `mvn test` — 100% verde
- [ ] `mvn package` — Sin errores

## 5. Archivos Impactados

| Archivo | Acción |
|---------|--------|
| `application/ports/out/ImpersonationPort.java` | Crear |
| `application/usecase/security/ImpersonationUseCase.java` | Crear |
| `infrastructure/adapters/security/ImpersonationJpaAdapter.java` | Crear |
| `infrastructure/web/security/ImpersonationController.java` | Crear |
| `infrastructure/security/JwtAuthFilter.java` | Modificar |
| `test/.../ImpersonationControllerIT.java` | Crear |
