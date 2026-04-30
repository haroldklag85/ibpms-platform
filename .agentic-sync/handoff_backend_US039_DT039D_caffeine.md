# Handoff Backend CORREGIDO — US-039 | DT-039-02: Caffeine Cache con CompositeCacheManager

---

> [!CAUTION]
> **VERSIÓN CORREGIDA.** Este handoff REEMPLAZA al anterior (`handoff_backend_US039_DT039D_caffeine.md`).
> La versión original prescribía `spring.cache.type: caffeine` que **rompería** el `RedisCacheManager` existente para `workdesk_tasks` y `menuTopology`.

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Cierre Deuda Técnica — Iteración 3 |
| **Sprint** | 6 |
| **Rama Git** | `sprint-6` |
| **User Story** | US-039 — Formulario Genérico Base |
| **Deuda Técnica** | DT-039-02 — Caffeine cache provider para `vipRoles` (coexistencia con Redis) |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md` |
| **Flujo de Trabajo** | Backend → QA → Arquitecto (verificación) |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables
- **ADR-001 (Hexagonal):** La caché es concern de infraestructura. La configuración va en `infrastructure/config/`.
- **ADR-011 (Local CQRS V1):** Caffeine opera como caché L1 in-memory para lecturas calientes de roles VIP.

### Contexto Crítico: Infraestructura de Caché Existente

**Estado actual verificado por Arquitecto Líder:**

| Cache Name | Provider | Uso | Archivo |
|-----------|----------|-----|---------|
| `workdesk_tasks` | **Redis** (TTL 10s) | Workdesk Query Service | `WorkdeskQueryService.java` L20, L26 |
| `menuTopology` | **Redis** (default) | Menu Layout Service | `MenuLayoutService.java` L26 |
| `vipRoles` | **Redis** (sin TTL específico) | VIP Role Lookup | `BpmTaskService.java` L192 |

**Problema:** `vipRoles` actualmente va a Redis sin TTL configurado. Queremos moverlo a Caffeine (in-process, TTL 5min) para:
- ✅ Evitar latencia de red Redis para una consulta de 3 filas
- ✅ Tener TTL explícito de 5 minutos
- ✅ No depender de Redis para esta consulta específica

**Solución: NO usar `spring.cache.type: caffeine`.** En su lugar, registrar un `CaffeineCacheManager` como bean adicional y usar `cacheManager` explícito en `@Cacheable`.

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivo a MODIFICAR
**`backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/config/CacheConfig.java`**

**Estado actual (22 líneas):**
```java
package com.ibpms.poc.infrastructure.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("workdesk_tasks",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(10)));
    }
}
```

### Archivo a MODIFICAR
**`backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/bpm/BpmTaskService.java`**

**Estado actual L192:**
```java
@Cacheable(value = "vipRoles", key = "'ALL'", unless = "#result.isEmpty()")
public List<String> getVipRoleNames() {
```

### Archivo de dependencias
**`backend/ibpms-core/pom.xml`**
- Agregar `com.github.ben-manes.caffeine:caffeine` si no existe.

### Archivo SIN modificar
**`backend/ibpms-core/src/main/resources/application.yml`**
- **NO agregar `spring.cache.type: caffeine`** — esto rompería Redis.

---

## 4. Snippets Prescriptivos

### 4.1 Dependencia Maven (agregar a `pom.xml`)

```xml
<!-- Caffeine L1 In-Process Cache para caches de dominio sin latencia de red -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

> **Nota:** NO agregar `spring-boot-starter-cache` porque `@EnableCaching` ya está activo vía Redis. Solo necesitamos la librería Caffeine.

### 4.2 Modificar `CacheConfig.java` — Agregar Bean Caffeine

Reemplazar el archivo completo con:

```java
package com.ibpms.poc.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis: CacheManager PRIMARIO (default).
     * Usado por: workdesk_tasks (TTL 10s), menuTopology (default TTL).
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("workdesk_tasks",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(10)));
    }

    /**
     * Caffeine: CacheManager SECUNDARIO (in-process, sin latencia de red).
     * DT-039-02: Usado exclusivamente para caches de dominio con baja cardinalidad.
     * - vipRoles: 3 filas, TTL 5 min, evita query repetitiva a BD para pre-flight VIP.
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("vipRoles");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(5, TimeUnit.MINUTES));
        return cacheManager;
    }
}
```

### 4.3 Modificar `BpmTaskService.java` L192 — Apuntar a Caffeine

Cambiar la anotación `@Cacheable` para que use el `caffeineCacheManager` explícitamente:

```java
// ANTES (usa Redis por defecto):
@Cacheable(value = "vipRoles", key = "'ALL'", unless = "#result.isEmpty()")

// DESPUÉS (usa Caffeine explícitamente):
@Cacheable(value = "vipRoles", key = "'ALL'", unless = "#result.isEmpty()", cacheManager = "caffeineCacheManager")
```

**Solo cambiar la línea 192.** NO tocar nada más en `BpmTaskService.java`.

---

## 5. Lo Que NO Hacer

| ❌ Prohibido | Razón |
|-------------|-------|
| Agregar `spring.cache.type: caffeine` a `application.yml` | Sobrescribiría el `RedisCacheManager` global |
| Modificar `WorkdeskQueryService.java` | Sigue usando Redis correctamente |
| Modificar `MenuLayoutService.java` | Sigue usando Redis correctamente |
| Cambiar el `@EnableCaching` existente | Ya funciona correctamente |
| Tocar `application.yml` | No se requiere ningún cambio |

---

## 6. Verificación sobre Hallazgo #4 (Migración Idempotente)

> [!NOTE]
> **El hallazgo #4 del Agente INFRA/BD sobre `21-us039-generic-form-schema.sql` L9 está MITIGADO.**
>
> La migración `29-consolidate-roles.sql` (changeset posterior) ejecuta `DROP TABLE IF EXISTS ibpms_roles` (L12), lo que elimina la tabla legacy completa. La columna `is_vip_restricted` fue migrada a `ibpms_security_role` con `IF NOT EXISTS` en L5 de ese mismo changeset.
>
> **Acción requerida: NINGUNA.** Changeset 21 es legacy y no se re-ejecutará (Liquibase lo marca como ya aplicado).

---

## 7. Matriz de QA y Testing Atómico

| Test Name | Deuda Evaluada | Aserción Esperada |
|-----------|---------------|-------------------|
| `testVipCacheUsesCaffeine` | DT-039-02 | `BpmTaskService.getVipRoleNames()` debe estar anotado con `cacheManager = "caffeineCacheManager"` |
| `testRedisCacheStillWorks` | Regresión | `WorkdeskQueryService` sigue usando Redis (`workdesk_tasks` cache resuelve a `RedisCacheManager`) |
| `testApplicationContextLoads` | Integridad | El contexto Spring arranca sin `BeanCreationException` con ambos CacheManagers |

---

## 8. Mensaje de Despacho

> **Instrucciones para el Agente Backend:**
>
> ⚠️ **IMPORTANTE: Este handoff REEMPLAZA la versión anterior.** Lee este documento COMPLETO.
>
> Tu tarea es:
> 1. Agregar `caffeine` al `pom.xml` (solo la librería, NO `spring-boot-starter-cache`).
> 2. **Modificar** `CacheConfig.java` agregando el bean `caffeineCacheManager()` — NO eliminar el `RedisCacheManagerBuilderCustomizer` existente.
> 3. **Modificar** `BpmTaskService.java` L192: agregar `cacheManager = "caffeineCacheManager"` a la anotación `@Cacheable`.
> 4. **NO tocar** `application.yml`, `WorkdeskQueryService.java`, ni `MenuLayoutService.java`.
>
> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
>
> **Rama:** `sprint-6`. PROHIBIDO trabajar en `main`.
