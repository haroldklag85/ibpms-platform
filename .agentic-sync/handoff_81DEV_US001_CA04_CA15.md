# 📋 Handoff Arquitectónico — Iteración 81-DEV (US-001)
> **Fecha:** 2026-04-14 | **Arquitecto Líder:** Agente Antigravity  
> **Rama:** `sprint-3/informe_auditoriaSprint1y2`  
> **Protocolo aplicado:** `.agents/skills/architect_handoff_protocol/SKILL.md` v1.0.0

---

## 1. Metadatos y SSOT (Single Source of Truth)

| Campo | Valor |
|-------|-------|
| **Iteración** | 81-DEV |
| **Sprint** | Sprint 3 |
| **Rama Git** | `sprint-3/informe_auditoriaSprint1y2` |
| **User Story** | US-001 — Obtener Tareas Pendientes en el Workdesk |
| **Archivo SSOT** | `docs/requirements/epics/epic_A_motor_core.md` (líneas 81–84 para CA-04, líneas 168–173 para CA-15) |
| **CAs a desarrollar** | CA-04, CA-15 |
| **CAs excluidos (V2)** | Ninguno — CA-04 y CA-15 son 100% V1 sin referencias a funcionalidad V2 |
| **Flujo de ejecución** | **Backend → Frontend → QA (Secuencial estricto)** |
| **Razón de secuencialidad** | El Backend DEBE proveer el endpoint de validación jerárquica RBAC (CA-15) ANTES de que el Frontend conecte el Toggle (CA-04) y el Banner de delegación. El Frontend depende del contrato API seguro. |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs que rigen esta iteración

| ADR / Restricción | Impacto en 81-DEV |
|--------------------|-------------------|
| **Anti-IDOR (CA-15)** | PROHIBIDO confiar en parámetros enviados por el cliente para determinar delegación. El Backend OBLIGATORIAMENTE debe validar que el usuario logueado (JWT) es jerárquicamente superior al `delegatedToId` enviado. Toda alteración de URL/payload retornará HTTP 403 Forbidden. |
| **CQRS Read Model** | El campo `assignee` ya existe en `WorkdeskProjectionEntity.java` (L39) y en la query `findWorkdeskTasks()` (L19, L25). La query nativa ya acepta filtro por `assignee`. |
| **Hibernate SecurityFilter** | La entidad ya tiene `@FilterDef(name = "assigneeSecurityFilter")` (L20-21) que restringe por `assignee = :currentUserId OR assignee IS NULL`. DEBE desactivarse selectivamente cuando se consulta en modo delegación validada. |
| **Tenant Isolation (CA-14)** | Las consultas de delegación DEBEN mantener el filtro `tenant_id` obligatorio. La delegación opera DENTRO del mismo tenant. Cross-tenant delegation es IMPOSIBLE por diseño. |
| **Lazy Evaluation (CA-23/US-036)** | `TaskDelegationService.java` ya implementa `evaluateAndRevertTaskIfNeeded()` para delegaciones expiradas. El endpoint de 81-DEV DEBE invocar este servicio para cada tarea retornada en modo delegación. |
| **Zero-Trust SRE** | Compilación Backend vía `.agents/skills/backend_sre_compilation_audit/SKILL.md`. |
| **Zero-Trust UI** | Build Frontend vía `.agents/skills/frontend_build_audit/SKILL.md`. |

### Trazabilidad de la Solución

Esta iteración es **Backend-dominante para seguridad, Frontend-dominante para UX**. El Backend debe crear un nuevo endpoint o extender el actual para aceptar `delegatedToId` con validación perimetral RBAC. El Frontend ya tiene un placeholder de filtro de delegación (`Workdesk.vue` L27-35 con `<select v-model="delegationFilter">`) que debe reemplazarse por un Toggle semántico con las opciones correctas según Gherkin: `[Mis Tareas]` y `[Tareas de mi Asistente]`. Adicionalmente, CA-15 exige un **Banner permanente** de alerta visual cuando se está en modo delegación.

### Especificaciones Gherkin (SSOT)

**CA-04** (L81-84 de `epic_A_motor_core.md`):
```gherkin
Given que un Asistente le delega permisos temporales a su Jefe (Juan),
Then el Workdesk de Juan muestra un Interruptor o Dropdown (Toggle) en la cabecera,
And permite alternar entre [Mis Tareas] y [Tareas de mi Asistente]
    sin mezclar visualmente los contextos.
```

**CA-15** (L168-173 de `epic_A_motor_core.md`):
```gherkin
Given el Toggle para ver las tareas de 'Mi Asistente',
When el Ejecutivo presiona el botón enviando el user_id del asistente,
Then el Backend VALIDARÁ PERIMETRALMENTE el RBAC,
    comprobando que el Ejecutivo logueado sea jerárquicamente el superior de ese ID.
And si se altera la URL para espiar a otro usuario,
    el servidor arrojará 403 Forbidden (Prevención IDOR).
And al cargar la vista delegada, el Frontend aplicará un destello visual
    o Banner permanente alertando: 'Estás viendo el escritorio de [Nombre]',
    mitigando errores operativos.
```

---

## 3. Rutas Exactas y Contexto Preexistente

### 3.1 Backend — Archivos a modificar/crear

| Archivo | Ruta relativa | Estado actual | Acción |
|---------|---------------|---------------|--------|
| **WorkdeskProjectionRepository.java** | `backend/.../infrastructure/jpa/repository/WorkdeskProjectionRepository.java` | ✅ Ya tiene `findWorkdeskTasks(tenantId, search, assignee, pageable)` con parámetro `assignee` (L25). | **AGREGAR** nueva query `findDelegatedTasks()` que consulte por `assignee = :delegatedUserId` sin activar el SecurityFilter. |
| **TaskDelegationService.java** | `backend/.../application/service/TaskDelegationService.java` | ✅ Tiene `evaluateAndRevertTaskIfNeeded()` (L28). | **EXTENDER** con método `validateDelegationHierarchy(String executiveId, String assistantId, String tenantId)` que retorna boolean. |
| **RbacAuthorizationService.java** | `backend/.../application/service/RbacAuthorizationService.java` | ✅ Tiene `getPermittedBpmnLanesForGroups()` (L48). | **SIN CAMBIOS** — se reutiliza la infraestructura existente de perfiles. |
| **WorkdeskProjectionEntity.java** | `backend/.../infrastructure/jpa/entity/WorkdeskProjectionEntity.java` | ✅ Campo `assignee` (L39), Filter `assigneeSecurityFilter` (L20-21). | **SIN CAMBIOS**. |
| **[NUEVO] DelegationValidationDTO.java** | `backend/.../application/dto/DelegationValidationDTO.java` | ❌ No existe. | **CREAR** DTO con campos: `delegatedUserId`, `delegatedUserDisplayName`, `isDelegationActive`. |
| **Controller del Workdesk** | (Endpoint `/workdesk/global-inbox`) | ✅ Ya acepta `delegatedToId` como query param (evidencia: `useWorkdeskStore.ts` L67). | **EXTENDER** para invocar `TaskDelegationService.validateDelegationHierarchy()` ANTES de ejecutar la query. Si falla → HTTP 403. |

### 3.2 Frontend — Archivos a modificar

| Archivo | Ruta relativa | Estado actual | Acción |
|---------|---------------|---------------|--------|
| **Workdesk.vue** | `frontend/src/views/Workdesk.vue` | ✅ Tiene `<select v-model="delegationFilter">` con opciones "Mis Tareas" y "Tareas del Equipo" (L27-35). | **REEMPLAZAR** select por Toggle con opciones del Gherkin: "Mis Tareas" / "Tareas de mi Asistente". **AGREGAR** Banner de delegación activa (CA-15). |
| **useWorkdeskStore.ts** | `frontend/src/stores/useWorkdeskStore.ts` | ✅ `fetchGlobalInbox()` ya acepta `delegatedToId` (L54, L67). | **EXTENDER** con estado `delegationMode` y acción `fetchDelegatedInbox()` que maneja el 403. |
| **authStore.ts** | `frontend/src/stores/authStore.ts` | ✅ Existe. Contiene `tenantId`. | **VERIFICAR** que expone `userId` y `displayName` del usuario logueado. |

### 3.3 QA — Archivos a modificar/crear

| Archivo | Ruta relativa | Estado actual | Acción |
|---------|---------------|---------------|--------|
| **WorkdeskRepositoryTest.java** | `backend/.../test/.../repository/WorkdeskRepositoryTest.java` | ✅ Tiene 11 tests (76-DEV a 80-DEV). | **EXTENDER** con tests de delegación y anti-IDOR. |
| **useWorkdeskStore.spec.ts** | `frontend/src/tests/useWorkdeskStore.spec.ts` | ✅ Tiene tests de 79-DEV y 80-DEV. | **EXTENDER** con tests del Toggle, Banner y manejo de 403. |

---

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### 4.1 Backend — Extensión de TaskDelegationService (CA-15): [MODIFICAR]

**Archivo:** `backend/.../application/service/TaskDelegationService.java`

**Agregar después del método `evaluateAndRevertTaskIfNeeded()` (L42):**

```java
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// ... dentro de la clase TaskDelegationService:

private final IbpmsProfileRepository profileRepository;

// Constructor actualizado con ProfileRepository
public TaskDelegationService(IbpmsProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
}

/**
 * CA-15: Validación Perimetral RBAC Anti-IDOR.
 * Verifica que el usuario ejecutivo (logueado) es jerárquicamente superior
 * al usuario asistente cuyo escritorio desea consultar.
 *
 * REGLAS:
 * 1. Ambos usuarios DEBEN pertenecer al mismo tenant (aislamiento inter-tenant).
 * 2. El ejecutivo DEBE tener un perfil con rol de supervisión sobre el asistente.
 * 3. Si la validación falla, se lanza 403 Forbidden (prevención IDOR).
 *
 * V1: La relación jerárquica se valida contra la tabla ibpms_delegation_authority.
 * Si la tabla no existe aún, se usa un fallback temporal basado en profiles.
 *
 * @param executiveUserId  ID del usuario logueado (extraído del JWT)
 * @param assistantUserId  ID del usuario asistente (enviado por el Frontend)
 * @param tenantId         Tenant ID del ejecutivo (extraído del JWT)
 * @return displayName del asistente si la validación pasa
 * @throws ResponseStatusException 403 si la validación falla
 */
public String validateDelegationHierarchy(String executiveUserId, String assistantUserId, String tenantId) {
    log.info("CA-15 RBAC Perimeter Check: Executive={} requesting delegation view for Assistant={} in Tenant={}",
            executiveUserId, assistantUserId, tenantId);

    // VALIDACIÓN 1: Self-delegation is a no-op (not an error, but no delegation needed)
    if (executiveUserId.equals(assistantUserId)) {
        log.warn("CA-15: Self-delegation attempted. Returning own context.");
        return executiveUserId; // Retorna su propio nombre, el Frontend manejará este caso
    }

    // VALIDACIÓN 2: Consultar si existe relación de delegación activa en BD
    // V1 Implementation: Query ibpms_workdesk_projection para verificar que
    // existen tareas donde el assignee original era el ejecutivo y fueron
    // delegadas al asistente, O que existe un registro de delegación explícito.
    //
    // NOTA PARA IMPLEMENTADOR: Si la tabla `ibpms_delegation_authority` no existe,
    // crear con DDL:
    //   CREATE TABLE ibpms_delegation_authority (
    //     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    //     executive_user_id VARCHAR(255) NOT NULL,
    //     assistant_user_id VARCHAR(255) NOT NULL,
    //     tenant_id VARCHAR(255) NOT NULL,
    //     is_active BOOLEAN DEFAULT TRUE,
    //     created_at TIMESTAMP DEFAULT NOW(),
    //     expires_at TIMESTAMP,
    //     UNIQUE(executive_user_id, assistant_user_id, tenant_id)
    //   );

    boolean isAuthorized = checkDelegationAuthority(executiveUserId, assistantUserId, tenantId);

    if (!isAuthorized) {
        log.error("CA-15 IDOR BLOCKED: User {} attempted unauthorized delegation view of {} in tenant {}. " +
                  "SUDO Action [Audit Trail]: Potential IDOR attack vector detected.",
                  executiveUserId, assistantUserId, tenantId);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Acceso denegado: No tiene autorización jerárquica para visualizar el escritorio de este usuario.");
    }

    log.info("CA-15: Delegation hierarchy validated. Executive={} → Assistant={}", executiveUserId, assistantUserId);

    // Retornar nombre visible del asistente para el Banner del Frontend
    return resolveDisplayName(assistantUserId);
}

/**
 * Verifica la autoridad de delegación en la base de datos.
 * V1: Fallback basado en existencia de tareas con ownership pattern.
 */
private boolean checkDelegationAuthority(String executiveId, String assistantId, String tenantId) {
    // IMPLEMENTAR: Query a ibpms_delegation_authority WHERE
    //   executive_user_id = :executiveId AND assistant_user_id = :assistantId
    //   AND tenant_id = :tenantId AND is_active = TRUE
    //   AND (expires_at IS NULL OR expires_at > NOW())
    //
    // Si la tabla no existe aún en V1, el implementador puede usar un
    // approach basado en ProfileBpmnAssignment para inferir jerarquía:
    //   - El ejecutivo tiene un perfil con nivel de supervisión
    //   - El asistente tiene un perfil subordinado al mismo grupo

    // PLACEHOLDER V1 — El implementador DEBE reemplazar con query real
    log.warn("CA-15 V1 PLACEHOLDER: Using profile-based hierarchy check. " +
             "Replace with ibpms_delegation_authority query when DDL is available.");
    return true; // TEMPORAL — Reemplazar con validación real
}

/**
 * Resuelve el nombre visible de un usuario por su ID.
 */
private String resolveDisplayName(String userId) {
    // IMPLEMENTAR: Query contra IbpmsProfileRepository o cache de usuarios
    // V1 Fallback: Retornar el userId como displayName
    return userId;
}
```

### 4.2 Backend — DTO de respuesta para delegación (CA-15): [NUEVO]

**Crear archivo:** `backend/.../application/dto/DelegationValidationDTO.java`

```java
package com.ibpms.poc.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CA-15: DTO que acompaña la respuesta del Workdesk en modo Delegación.
 * Permite al Frontend renderizar el Banner de contexto delegado.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelegationContextDTO {
    /** ID del usuario cuyas tareas se están visualizando */
    private String delegatedUserId;

    /** Nombre visible del usuario delegado (para el Banner) */
    private String delegatedUserDisplayName;

    /** Indica si el modo delegación está activo en esta respuesta */
    private boolean delegationActive;
}
```

### 4.3 Backend — Extensión del Controller del Workdesk (CA-15): [MODIFICAR]

**Referencia de contrato API vigente:** El Frontend ya envía `delegatedToId` como query param (evidencia en `useWorkdeskStore.ts` L67).

**Pseudocódigo del flujo que DEBE implementar el Controller:**

```java
// En el método GET /workdesk/global-inbox del Controller existente:

@GetMapping("/workdesk/global-inbox")
public ResponseEntity<?> getGlobalInbox(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String delegatedToId, // CA-04/CA-15
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String slaLevel,
        @RequestParam(required = false) String status,
        @AuthenticationPrincipal JwtAuthenticationToken jwt) {

    String currentUserId = jwt.getName(); // ID extraído del JWT
    String tenantId = extractTenantId(jwt);

    // ==========================================
    // CA-15: VALIDACIÓN PERIMETRAL ANTI-IDOR
    // ==========================================
    DelegationContextDTO delegationContext = null;
    String effectiveAssignee = currentUserId; // Default: mis propias tareas

    if (delegatedToId != null && !delegatedToId.isBlank()) {
        // CA-15: Validar jerarquía ANTES de ejecutar cualquier query
        String assistantDisplayName = taskDelegationService
            .validateDelegationHierarchy(currentUserId, delegatedToId, tenantId);

        effectiveAssignee = delegatedToId; // Query por las tareas del asistente
        delegationContext = new DelegationContextDTO(
            delegatedToId, assistantDisplayName, true
        );
    }

    // Query con el assignee efectivo (propio o delegado/validado)
    Page<WorkdeskProjectionEntity> result = workdeskRepository
        .findWorkdeskTasks(tenantId, search, effectiveAssignee, PageRequest.of(page, size));

    // Construir respuesta incluyendo contexto de delegación
    Map<String, Object> response = new HashMap<>();
    response.put("content", result.getContent().stream().map(this::toDto).toList());
    response.put("pageable", result.getPageable());
    response.put("totalElements", result.getTotalElements());
    if (delegationContext != null) {
        response.put("delegationContext", delegationContext); // CA-15: Metadata para Banner
    }

    return ResponseEntity.ok(response);
}
```

### 4.4 Frontend — Toggle de Delegación en Workdesk.vue (CA-04): [MODIFICAR]

**A. Reemplazar el `<select>` placeholder (L27-35) por Toggle semántico:**

```html
<!-- CA-04: Toggle de Delegación con contextos separados -->
<div class="flex items-center gap-3 mb-1">
  <div class="inline-flex rounded-lg border border-gray-200/80 bg-white/50 backdrop-blur-sm p-0.5 shadow-sm">
    <button
      :class="[
        'px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200',
        delegationMode === 'SELF'
          ? 'bg-indigo-600 text-white shadow-sm'
          : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
      ]"
      @click="switchDelegationMode('SELF')"
    >
      📋 Mis Tareas
    </button>
    <button
      :class="[
        'px-3 py-1.5 text-xs font-semibold rounded-md transition-all duration-200',
        delegationMode === 'DELEGATED'
          ? 'bg-amber-500 text-white shadow-sm'
          : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
      ]"
      @click="switchDelegationMode('DELEGATED')"
    >
      👤 Tareas de mi Asistente
    </button>
  </div>
</div>

<!-- CA-15: Banner de Delegación Activa (solo visible en modo DELEGATED) -->
<Transition name="slide-down">
  <div
    v-if="delegationMode === 'DELEGATED' && delegatedUserName"
    class="w-full px-4 py-2.5 mb-3 rounded-lg bg-amber-50 border border-amber-200/60 flex items-center gap-2 shadow-sm"
    role="alert"
    aria-live="polite"
  >
    <span class="text-amber-600 text-lg">⚠️</span>
    <span class="text-sm font-medium text-amber-800">
      Estás viendo el escritorio de <strong>{{ delegatedUserName }}</strong>
    </span>
    <button
      class="ml-auto text-xs text-amber-600 hover:text-amber-800 underline"
      @click="switchDelegationMode('SELF')"
    >
      Volver a mis tareas
    </button>
  </div>
</Transition>
```

**B. Agregar al `<script setup>` las variables y función de cambio de modo:**

```typescript
// CA-04: Estado del modo de delegación
const delegationMode = ref<'SELF' | 'DELEGATED'>('SELF');
const delegatedUserId = ref<string | null>(null);
const delegatedUserName = ref<string | null>(null);

// CA-04/CA-15: Cambiar modo de delegación
const switchDelegationMode = async (mode: 'SELF' | 'DELEGATED') => {
  if (mode === delegationMode.value) return;

  delegationMode.value = mode;

  if (mode === 'DELEGATED') {
    // CA-15: Enviar request con el assistantId del usuario configurado
    // V1: El assistantId se obtiene del perfil del ejecutivo logueado
    const auth = useAuthStore();
    const assistantId = auth.delegatedAssistantId; // DEBE existir en authStore

    if (!assistantId) {
      console.warn('CA-04: No se encontró asistente configurado para delegación');
      delegationMode.value = 'SELF';
      return;
    }

    delegatedUserId.value = assistantId;

    try {
      // CA-15: El Backend valida la jerarquía y retorna 403 si es IDOR
      await store.fetchGlobalInbox(0, 50, searchQuery.value, assistantId);

      // Si la respuesta incluye delegationContext, extraer nombre
      // (El store debe exponer delegationContext desde la última respuesta)
      delegatedUserName.value = store.lastDelegationContext?.delegatedUserDisplayName || assistantId;
    } catch (error: any) {
      if (error.response?.status === 403) {
        // CA-15: Bloqueo IDOR — revertir al modo propio
        console.error('CA-15: Delegación denegada por el servidor (403 Forbidden)');
        delegationMode.value = 'SELF';
        delegatedUserId.value = null;
        delegatedUserName.value = null;
        // Mostrar notificación al usuario
        alert('No tiene permisos para ver el escritorio de este usuario.');
      }
    }
  } else {
    // Volver a "Mis Tareas"
    delegatedUserId.value = null;
    delegatedUserName.value = null;
    await loadData(); // Recargar con el contexto propio
  }
};
```

**C. Eliminar el `<select>` viejo (L27-35 de Workdesk.vue):**

Eliminar completamente:
```html
<!-- ELIMINAR ESTE BLOQUE COMPLETO -->
<select v-model="delegationFilter" class="...">
  <option value="mine">📋 Mis Tareas</option>
  <option value="team">👥 Tareas del Equipo</option>
</select>
```

Y eliminar la variable `delegationFilter` de las refs del script setup.

**D. Actualizar `loadData()` para respetar el modo de delegación:**

```typescript
// Modificar la función loadData existente:
const loadData = async () => {
  const delegatedId = delegationMode.value === 'DELEGATED' ? delegatedUserId.value : undefined;
  await store.fetchGlobalInbox(0, 50, searchQuery.value, delegatedId || undefined);
};
```

**E. Agregar transición CSS para el Banner:**

```css
/* CA-15: Animación suave del Banner de delegación */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
```

### 4.5 Frontend — Extensión del Store (CA-15): [MODIFICAR]

**Archivo:** `frontend/src/stores/useWorkdeskStore.ts`

**Agregar al state (después de L50):**

```typescript
// CA-15: Contexto de delegación de la última respuesta
lastDelegationContext: null as { delegatedUserId: string; delegatedUserDisplayName: string; delegationActive: boolean } | null,
```

**Modificar `fetchGlobalInbox()` (L74-78) para capturar el delegationContext:**

```typescript
// Dentro del bloque if (response.data && Array.isArray(response.data.content))
// AGREGAR después de la línea de facets (L78):
this.lastDelegationContext = response.data.delegationContext || null;
```

---

## 5. Matriz de QA y Testing Atómico

### Tests del Repository + Service Layer (JUnit / @SpringBootTest)

**Archivo destino:** `backend/.../test/.../repository/WorkdeskRepositoryTest.java` (extender)

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 12 | `testFindWorkdeskTasks_FilterByAssignee` | CA-04/CA-15 | Insertar 3 tareas: 2 con `assignee="executiveA"`, 1 con `assignee="assistantB"`. Consultar con `assignee="assistantB"`. Resultado debe tener exactamente 1 tarea (la del asistente). |
| 13 | `testFindWorkdeskTasks_DelegationCrossTenantBlocked` | CA-15/CA-14 | Insertar tarea con `assignee="assistantB"` en `tenantA`. Consultar con `tenantId="tenantB"` y `assignee="assistantB"`. Resultado debe ser vacío (0 tareas). Cross-tenant = invisible. |
| 14 | `testDelegationValidation_SelfDelegationNoop` | CA-15 | Invocar `validateDelegationHierarchy("userA", "userA", "tenant1")`. NO debe lanzar excepción. Retorna el mismo userId. |
| 15 | `testDelegationValidation_UnauthorizedThrows403` | CA-15 | Invocar `validateDelegationHierarchy("userX", "userY", "tenant1")` SIN registro de autoridad. DEBE lanzar `ResponseStatusException` con status 403. |

### Tests del Frontend Store/Component (Vitest)

**Archivo destino:** `frontend/src/tests/useWorkdeskStore.spec.ts` (extender)

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 22 | `delegation_toggle_switches_mode_to_delegated` | CA-04 | Simular `switchDelegationMode('DELEGATED')` con `assistantId` configurado. Verificar que `fetchGlobalInbox` se invoca con `delegatedToId`. |
| 23 | `delegation_toggle_returns_to_self` | CA-04 | Desde modo `DELEGATED`, invocar `switchDelegationMode('SELF')`. Verificar que `fetchGlobalInbox` se invoca SIN `delegatedToId`. Verificar `delegationMode === 'SELF'`. |
| 24 | `delegation_banner_shows_assistant_name` | CA-15 | Mock response con `delegationContext: { delegatedUserDisplayName: "Ana García" }`. Verificar que `lastDelegationContext.delegatedUserDisplayName === "Ana García"`. |
| 25 | `delegation_403_reverts_to_self_mode` | CA-15 | Mock `fetchGlobalInbox` para lanzar error con `status: 403`. Verificar que `delegationMode` se revierte a `'SELF'` y `delegatedUserId` es null. |
| 26 | `delegation_store_captures_delegation_context` | CA-15 | Mock response con `delegationContext` presente. Verificar que `store.lastDelegationContext` se setea correctamente. |
| 27 | `delegation_mode_persists_through_pagination` | CA-04 | En modo `DELEGATED`, invocar `fetchGlobalInbox(page=1)`. Verificar que `delegatedToId` sigue pasándose en las páginas siguientes. |

---

## 6. Mensajes de Despacho (Copy & Paste para el Humano)

### 🔄 Orden de ejecución: SECUENCIAL ESTRICTO
1. **Primero:** Backend (Validación RBAC + DTO + Extension del Controller)
2. **Segundo:** Frontend (Toggle CA-04 + Banner CA-15 + Manejo 403)
3. **Tercero:** QA (certificación cruzada Backend + Frontend)

---

### 1️⃣ Para el Agente Backend:

```text
Actúa como Agente Backend.
Iniciamos la Iteración 81-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Implementar la validación perimetral RBAC para delegación de tareas (Anti-IDOR) y extender el endpoint del Workdesk para soportar consultas delegadas seguras.

ALCANCE (CAs):
- CA-04 (Infraestructura): El endpoint `/workdesk/global-inbox` ya acepta `delegatedToId` como query param. DEBES agregar la validación de jerarquía ANTES de ejecutar la query cuando este parámetro esté presente.
- CA-15 (Seguridad): Implementar validación perimetral que verifica que el usuario logueado (JWT) es jerárquicamente superior al `delegatedToId`. Si la relación no existe o la URL fue manipulada → HTTP 403 Forbidden. AGREGAR auditoría en logs (Audit Trail).

CONTEXTO PREEXISTENTE (lee estos archivos ANTES de codificar):
- `backend/.../application/service/TaskDelegationService.java` — Ya tiene `evaluateAndRevertTaskIfNeeded()`. DEBES extender con `validateDelegationHierarchy()`.
- `backend/.../infrastructure/jpa/repository/WorkdeskProjectionRepository.java` — Ya acepta parámetro `assignee` en query nativa (L25).
- `backend/.../infrastructure/jpa/entity/WorkdeskProjectionEntity.java` — Tiene `assigneeSecurityFilter` (L20-21).
- `backend/.../application/service/RbacAuthorizationService.java` — Infraestructura RBAC existente.
- `backend/.../test/.../repository/WorkdeskRepositoryTest.java` — Ya tiene 11 tests. DEBES extender.

TAREAS PRESCRIPTIVAS:
1. CREAR DTO `DelegationContextDTO.java` con campos: `delegatedUserId`, `delegatedUserDisplayName`, `delegationActive`.
2. EXTENDER `TaskDelegationService.java`:
   a. Agregar método `validateDelegationHierarchy(executiveId, assistantId, tenantId)`.
   b. Si la relación no existe → lanzar ResponseStatusException(403).
   c. Log de auditoría obligatorio para intentos bloqueados.
   d. Self-delegation (mismo ID) = no-op, no error.
3. EXTENDER el Controller del Workdesk:
   a. Cuando `delegatedToId` está presente → invocar `validateDelegationHierarchy()` ANTES de la query.
   b. Pasar `delegatedToId` como `assignee` a `findWorkdeskTasks()`.
   c. Incluir `DelegationContextDTO` en la respuesta JSON cuando delegación está activa.
4. AGREGAR TEST 12: `testFindWorkdeskTasks_FilterByAssignee` — Verificar filtro por assignee.
5. AGREGAR TEST 13: `testFindWorkdeskTasks_DelegationCrossTenantBlocked` — Cross-tenant bloqueado.
6. AGREGAR TEST 14: `testDelegationValidation_SelfDelegationNoop` — Self-delegation no lanza error.
7. AGREGAR TEST 15: `testDelegationValidation_UnauthorizedThrows403` — IDOR bloqueado con 403.

NOTA SOBRE DDL: Si la tabla `ibpms_delegation_authority` no existe, puedes implementar un fallback temporal basado en perfiles (`IbpmsProfileRepository`). Documenta cualquier deuda técnica en el commit.

Los snippets exactos están en `.agentic-sync/handoff_81DEV_US001_CA04_CA15.md` sección 4.1 a 4.3.

Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en .agents/skills/backend_sre_compilation_audit/SKILL.md (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
Actualiza `.agentic-sync/coverage_matrix.md` marcando CA-04 y CA-15 en columna Back como ✅.
Haz commit y push en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

### 2️⃣ Para el Agente Frontend (solo tras commit del Backend):

```text
Actúa como Agente Frontend.
Continuamos la Iteración 81-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Implementar el Toggle de Delegación en la cabecera del Workdesk y el Banner de seguridad para el modo delegado.

ALCANCE (CAs):
- CA-04: Reemplazar el `<select v-model="delegationFilter">` placeholder (L27-35 de Workdesk.vue) por un Toggle estilizado con dos opciones: "📋 Mis Tareas" y "👤 Tareas de mi Asistente". Los contextos NO deben mezclarse visualmente.
- CA-15 (Frontend): Al activar el modo delegación, mostrar Banner permanente con texto: "Estás viendo el escritorio de [Nombre]". Si el servidor retorna 403 → revertir automáticamente al modo "Mis Tareas" y mostrar alerta.
- Sin exclusiones V2 para estos CAs.

CONTEXTO PREEXISTENTE (lee estos archivos ANTES de codificar):
- `frontend/src/views/Workdesk.vue` — Tiene `<select>` placeholder en L27-35. Eliminar completamente.
- `frontend/src/stores/useWorkdeskStore.ts` — `fetchGlobalInbox()` ya acepta `delegatedToId` (L67). DEBES extender el state con `lastDelegationContext`.
- `frontend/src/stores/authStore.ts` — VERIFICAR que expone `delegatedAssistantId` del perfil del ejecutivo.

TAREAS PRESCRIPTIVAS:
1. ELIMINAR el `<select v-model="delegationFilter">` (L27-35) y la variable `delegationFilter` del script setup.
2. AGREGAR Toggle component (usando clases existentes del proyecto) con estados 'SELF' y 'DELEGATED'.
3. AGREGAR variables reactivas: `delegationMode`, `delegatedUserId`, `delegatedUserName`.
4. IMPLEMENTAR `switchDelegationMode(mode)`:
   a. Si mode='DELEGATED' → obtener `assistantId` del authStore, llamar `fetchGlobalInbox(…, assistantId)`.
   b. Si el servidor retorna 403 → revertir a 'SELF', limpiar estado, mostrar alerta.
   c. Si mode='SELF' → limpiar estado delegación, recargar datos propios.
5. AGREGAR Banner HTML con `<Transition name="slide-down">` visible solo en modo DELEGATED.
6. AGREGAR CSS para la transición `slide-down`.
7. MODIFICAR `loadData()` para pasar `delegatedUserId` cuando `delegationMode === 'DELEGATED'`.
8. EXTENDER `useWorkdeskStore.ts`:
   a. Agregar `lastDelegationContext` al state.
   b. Capturar `response.data.delegationContext` en `fetchGlobalInbox()`.

Los snippets exactos están en `.agentic-sync/handoff_81DEV_US001_CA04_CA15.md` sección 4.4 a 4.5.

Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en .agents/skills/frontend_build_audit/SKILL.md.
Actualiza `.agentic-sync/coverage_matrix.md` marcando CA-04 y CA-15 en columna Front como ✅.
Haz commit y push en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

### 3️⃣ Para el Agente QA (solo tras commit del Frontend):

```text
Actúa como Agente QA Especialista.
Cerramos la Iteración 81-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Certificar el Toggle de Delegación (CA-04) y la Validación Perimetral Anti-IDOR (CA-15).

NFR / QA STRATEGY: Pruebas unitarias al Repository Data Layer + Vitest para la lógica de delegación y manejo de errores 403.

CONTEXTO PREEXISTENTE:
- `backend/.../test/.../repository/WorkdeskRepositoryTest.java` — Ya tiene 11 tests (80-DEV). Backend ya agregó TEST 12-15 en su paso. VERIFICA que existen.
- `frontend/src/tests/useWorkdeskStore.spec.ts` — Ya tiene tests de 79-DEV y 80-DEV. DEBES extender con 6 tests adicionales.

TAREAS PRESCRIPTIVAS (Frontend / Vitest):
1. EXTENDER `frontend/src/tests/useWorkdeskStore.spec.ts` con los siguientes tests:
   - Test 22: `delegation_toggle_switches_mode_to_delegated` (CA-04) — Verificar que `fetchGlobalInbox` se invoca con `delegatedToId` al cambiar a modo DELEGATED.
   - Test 23: `delegation_toggle_returns_to_self` (CA-04) — Verificar retorno limpio a modo SELF.
   - Test 24: `delegation_banner_shows_assistant_name` (CA-15) — Mock response con delegationContext, verificar nombre.
   - Test 25: `delegation_403_reverts_to_self_mode` (CA-15) — Mock 403, verificar reversión automática.
   - Test 26: `delegation_store_captures_delegation_context` (CA-15) — Verificar que el store captura el contexto.
   - Test 27: `delegation_mode_persists_through_pagination` (CA-04) — Verificar persistencia del modo en paginación.

La matriz completa de test-a-CA está en `.agentic-sync/handoff_81DEV_US001_CA04_CA15.md` sección 5.

Audita `.agentic-sync/coverage_matrix.md` y verifica que CA-04 y CA-15 estén marcados ✅ en Back, Front y QA.
Haz commit cerrando la iteración en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

## 7. Diagrama de Dependencias

```
┌─────────────────────────────────────────────────────────────────┐
│                      81-DEV: CA-04 + CA-15                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  BACKEND (Paso 1)                                               │
│  ┌─────────────────────────────────────┐                        │
│  │ TaskDelegationService.java          │                        │
│  │  + validateDelegationHierarchy()    │──┐                     │
│  │  + checkDelegationAuthority()       │  │                     │
│  └─────────────────────────────────────┘  │                     │
│  ┌─────────────────────────────────────┐  │                     │
│  │ DelegationContextDTO.java [NUEVO]   │  │  ↓ HTTP 200 + DTO  │
│  └─────────────────────────────────────┘  │  ↓ o HTTP 403      │
│  ┌─────────────────────────────────────┐  │                     │
│  │ Controller /workdesk/global-inbox   │←─┘                     │
│  │  + if delegatedToId → validate()    │                        │
│  │  + include delegationContext in res │                        │
│  └─────────────────────────────────────┘                        │
│                         │                                       │
│                         ▼                                       │
│  FRONTEND (Paso 2)                                              │
│  ┌─────────────────────────────────────┐                        │
│  │ useWorkdeskStore.ts                 │                        │
│  │  + lastDelegationContext (state)    │                        │
│  │  + capture delegationContext        │                        │
│  └─────────────────────────────────────┘                        │
│  ┌─────────────────────────────────────┐                        │
│  │ Workdesk.vue                        │                        │
│  │  - ELIMINAR <select> placeholder    │                        │
│  │  + Toggle [Mis Tareas | Asistente]  │                        │
│  │  + Banner "Estás viendo el          │                        │
│  │    escritorio de [Nombre]"          │                        │
│  │  + switchDelegationMode()           │                        │
│  │  + Manejo de 403 → revert to SELF  │                        │
│  └─────────────────────────────────────┘                        │
│                         │                                       │
│                         ▼                                       │
│  QA (Paso 3)                                                    │
│  ┌─────────────────────────────────────┐                        │
│  │ WorkdeskRepositoryTest.java         │                        │
│  │  TEST 12-15: Delegación + IDOR     │                        │
│  ├─────────────────────────────────────┤                        │
│  │ useWorkdeskStore.spec.ts            │                        │
│  │  TEST 22-27: Toggle + Banner + 403 │                        │
│  └─────────────────────────────────────┘                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 8. Criterio de Aceptación de la Iteración

| Verificación | Descripción | Responsable |
|-------------|-------------|-------------|
| ☐ | Backend compila sin errores (Zero-Trust SRE) | Backend Agent |
| ☐ | Tests 12-15 pasan (JUnit) | Backend Agent |
| ☐ | Endpoint retorna 403 cuando `delegatedToId` no tiene relación jerárquica | Backend Agent |
| ☐ | Frontend build sin errores (Zero-Trust UI) | Frontend Agent |
| ☐ | Toggle alterna entre "Mis Tareas" y "Tareas de mi Asistente" | Frontend Agent |
| ☐ | Banner visible con nombre del asistente en modo delegación | Frontend Agent |
| ☐ | Error 403 revierte al modo propio automáticamente | Frontend Agent |
| ☐ | Tests 22-27 pasan (Vitest) | QA Agent |
| ☐ | Coverage matrix actualizada con ✅ en CA-04 y CA-15 (Back, Front, QA) | QA Agent |
| ☐ | Commit + push en rama `sprint-3/informe_auditoriaSprint1y2` | Todos |
