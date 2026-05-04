# 🟠 Handoff Backend — US-025 Fase 1B: Remediación Hexagonal

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Backend  
> **Prioridad:** 🟡 P1  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 0 completada  
> **Ejecución:** En PARALELO con Fase 1A (Frontend)  
> **Gate de Salida:** `mvn test` 100% pass + `mvn package` limpio

---

## 1. Contexto

La auditoría de US-025 reveló 2 violaciones hexagonales críticas en el backend:

1. **ARQ-025-10:** `MenuLayoutUseCase.java` tiene ~80 líneas de `if/else` hardcodeados mapeando roles→menús. Viola ADR-001 (Hexagonal) porque la lógica de resolución menú↔rol debería estar data-driven (tabla `ibpms_menu_topology`), no code-driven.

2. **ARQ-025-11:** `RoleHierarchyService.java` importa `RoleHierarchyEntity`, `RoleTemplateEntity`, y repositorios JPA directamente en `application.service.security`. Viola la regla de aislamiento: la capa Application solo debe importar puertos/interfaces, no implementaciones de Infrastructure.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-001 (Hexagonal) | Los UseCase/Service en `application/` NO deben importar `infrastructure/*`. Solo puertos en `application/ports/` |
| ADR-009 (PostgreSQL) | La resolución menú→rol debe usar tabla PostgreSQL con JSONB para `required_roles` |
| ADR-010 (Testing Pyramid) | Tests de integración obligatorios para controllers REST |
| ADR-011 (CQRS Local) | `MenuLayoutUseCase.getBuildLayoutForUser()` es una Query pura — debe consumir puerto de lectura |

---

## 3. Tareas

### Tarea 1B.2 — Crear Puerto `MenuTopologyPort`

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/ports/out/MenuTopologyPort.java`

```java
package com.ibpms.poc.application.ports.out;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import java.util.List;
import java.util.Set;

/**
 * Port-Out (Hexagonal) para consultar la topología de menús filtrada por roles.
 * La implementación concreta (JPA, JDBC, Cache) vive en infrastructure/adapters/.
 */
public interface MenuTopologyPort {
    
    /**
     * Retorna el árbol de menús filtrado por los roles efectivos del usuario.
     * @param effectiveRoles Roles del JWT (ya con herencia resuelta)
     * @return Lista de MenuItemDTO (árbol con children) ya podada
     */
    List<MenuItemDTO> findMenuTreeByRoles(Set<String> effectiveRoles);
}
```

---

### Tarea 1B.3 — Crear Adaptador JPA `MenuTopologyJpaAdapter`

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/ui/MenuTopologyJpaAdapter.java`

**Comportamiento esperado:**
1. Consultar tabla `ibpms_menu_topology` con query nativa PostgreSQL
2. Filtrar menús donde `required_roles` (JSONB array) intersecte con los roles del usuario, o donde `required_roles IS NULL` (acceso universal)
3. Construir el árbol jerárquico (parent_id → children) y retornar como `List<MenuItemDTO>`
4. Implementar `MenuTopologyPort`

**Query sugerida:**
```sql
SELECT id, label, icon, path, parent_id, sort_order, required_roles
FROM ibpms_menu_topology
WHERE required_roles IS NULL 
   OR required_roles ?| ARRAY[:roles]
ORDER BY sort_order
```

**Importante:** La "Regla de Poda Inteligente" (que hoy está hardcodeada en el UseCase L53-55) debe implementarse aquí: los folders sin hijos visibles no se incluyen en el resultado.

---

### Tarea 1B.4 — Refactorizar `MenuLayoutUseCase`

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/ui/MenuLayoutUseCase.java`

**Código actual (101 líneas de if/else):** Ver L21-99 completo.

**Código objetivo (~15 líneas):**
```java
@Service
public class MenuLayoutUseCase {

    private final MenuTopologyPort menuTopologyPort;

    public MenuLayoutUseCase(MenuTopologyPort menuTopologyPort) {
        this.menuTopologyPort = menuTopologyPort;
    }

    /**
     * CA-6: Construye el árbol de renderizado delegando al puerto.
     */
    public List<MenuItemDTO> getBuildLayoutForUser(Set<String> userRoles) {
        return menuTopologyPort.findMenuTreeByRoles(userRoles);
    }
}
```

**Reglas:**
- Eliminar TODOS los `if (userRoles.contains(...))` — la resolución es data-driven ahora
- El UseCase NO debe importar nada de `infrastructure.*`
- El UseCase solo inyecta `MenuTopologyPort` (inversión de dependencias)

---

### Tarea 1B.5 — Refactorizar `RoleHierarchyService`

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/security/RoleHierarchyService.java`

**Violación actual (L1-6):**
```java
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;  // ❌ infrastructure en application
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleTemplateEntity;   // ❌ infrastructure en application
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleHierarchyRepository; // ❌
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleTemplateRepository;  // ❌
```

**Crear puerto:** `application/ports/out/RoleHierarchyPort.java`
```java
public interface RoleHierarchyPort {
    Set<String> resolveAllEffectiveRoles(Set<String> directRoleNames);
    void registerHierarchy(UUID parentRoleId, UUID childRoleId);
    List<UUID> findAllAncestorRoleIds(UUID roleTemplateId);
}
```

**Crear adaptador:** `infrastructure/adapters/security/RoleHierarchyJpaAdapter.java`
- Mover la lógica actual de `RoleHierarchyService` que interactúa con JPA al adaptador
- El service original solo conserva la orquestación, delegando al puerto

---

### Tarea 1B.6 — Verificar/Crear endpoint SSE `/api/v1/security/stream`

**Verificar primero:** El frontend (`authStore.ts` L38) consume:
```typescript
const sseSource = new EventSource('/api/v1/security/stream', { withCredentials: true });
```

Si el endpoint NO existe en el backend, crear:

**Crear:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/SecuritySseController.java`

```java
@RestController
@RequestMapping("/api/v1/security")
public class SecuritySseController {
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSecurityEvents(Authentication auth) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        // Registrar el emitter en un registry (ConcurrentHashMap<userId, SseEmitter>)
        // Emitir eventos [ROLE_REVOKED], [ROLES_UPDATED] cuando admin modifica roles
        return emitter;
    }
}
```

Si el endpoint YA existe, verificar que emite los eventos `ROLE_REVOKED` y `ROLES_UPDATED` correctamente.

---

### Tarea 1B.7 — Tests de integración MenuLayoutController

**Crear:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/ui/MenuLayoutControllerIT.java`

**Escenarios obligatorios:**
1. `GET /api/v1/users/me/menu-layout` con JWT `ROLE_SUPER_ADMIN` → HTTP 200 + layout contiene TODOS los módulos (Workdesk, Gobernanza, Analytics, etc.)
2. `GET /api/v1/users/me/menu-layout` con JWT `ROLE_OPERADOR` → HTTP 200 + layout contiene SOLO Home + Workdesk
3. `GET /api/v1/users/me/menu-layout` sin JWT → HTTP 401

**Usar:** `@SpringBootTest` + `@AutoConfigureMockMvc` + Testcontainers PostgreSQL (ADR-010 prohíbe H2)

---

### Tarea 1B.8 — Tests de integración AuthBffController

**Crear:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/AuthBffControllerIT.java`

**Escenarios:**
1. `GET /api/v1/auth/effective-roles` con JWT de user con herencia piramidal → Retorna roles directos + heredados
2. `GET /api/v1/auth/effective-roles` sin JWT → HTTP 401

---

## 4. Criterios de Aceptación del Gate

- [ ] `MenuLayoutUseCase` tiene 0 imports de `infrastructure.*`
- [ ] `RoleHierarchyService` tiene 0 imports de `infrastructure.*`
- [ ] `MenuTopologyPort` interface existe en `application/ports/out/`
- [ ] `MenuTopologyJpaAdapter` implementa el puerto y consulta PostgreSQL
- [ ] SSE endpoint `/security/stream` funcional
- [ ] `MenuLayoutControllerIT` — 3 escenarios pasando
- [ ] `AuthBffControllerIT` — 2 escenarios pasando
- [ ] `mvn test` — 100% verde
- [ ] `mvn package` — Sin errores

## 5. Exclusiones

- NO implementar Impersonación (CA-9/CA-31) — Diferido a V2 por decisión PO
- NO modificar el contrato de `MenuItemDTO` — el frontend ya consume este DTO
- NO crear nuevos endpoints — solo refactorizar los existentes

## 6. Archivos Impactados

| Archivo | Acción | Detalle |
|---------|--------|---------|
| `application/ports/out/MenuTopologyPort.java` | Crear | Puerto hexagonal |
| `infrastructure/adapters/ui/MenuTopologyJpaAdapter.java` | Crear | Adaptador JPA |
| `application/usecase/ui/MenuLayoutUseCase.java` | Refactorizar | Eliminar if/else, inyectar puerto |
| `application/ports/out/RoleHierarchyPort.java` | Crear | Puerto hexagonal |
| `infrastructure/adapters/security/RoleHierarchyJpaAdapter.java` | Crear | Adaptador JPA |
| `application/service/security/RoleHierarchyService.java` | Refactorizar | Desacoplar de infrastructure |
| `infrastructure/web/security/SecuritySseController.java` | Crear/Verificar | SSE endpoint |
| `test/.../MenuLayoutControllerIT.java` | Crear | 3 escenarios |
| `test/.../AuthBffControllerIT.java` | Crear | 2 escenarios |
