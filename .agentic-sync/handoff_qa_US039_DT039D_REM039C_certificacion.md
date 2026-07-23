# Handoff QA CORREGIDO — US-039 | Certificación de Cierre DT-039-02 + REM-039-C

---

> [!CAUTION]
> **VERSIÓN CORREGIDA (V2).** Este handoff REEMPLAZA la versión anterior.
> Los checkpoints de DT-039-02 fueron actualizados para validar el patrón **CompositeCacheManager** (Caffeine + Redis coexistentes), NO `spring.cache.type: caffeine` global.

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Cierre Deuda Técnica — Iteración 3 |
| **Sprint** | 6 |
| **Rama Git** | `sprint-6` |
| **User Story** | US-039 — Formulario Genérico Base |
| **Deudas a Certificar** | DT-039-02 (Caffeine Cache con CompositeCacheManager) + REM-039-C (Draft Banner Test) |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md` |
| **Flujo de Trabajo** | Backend (DT-039-02) → Frontend (REM-039-C) → **QA (certificación)** → Arquitecto (veredicto) |

---

## 2. Contexto Arquitectónico Crítico

> [!IMPORTANT]
> **El proyecto usa Redis como CacheManager principal.** No se debe buscar `spring.cache.type: caffeine` en `application.yml`. La solución correcta es un **bean secundario** `@Bean("caffeineCacheManager")` que coexiste con Redis.

### Mapa de CacheManagers Esperado

| Cache Name | Provider | Archivo que lo usa |
|-----------|----------|-------------------|
| `workdesk_tasks` | **Redis** (TTL 10s) | `WorkdeskQueryService.java` |
| `menuTopology` | **Redis** (default) | `MenuLayoutService.java` |
| `vipRoles` | **Caffeine** (TTL 5min) | `BpmTaskService.java` ← este es el que cambió |

---

## 3. Certificar DT-039-02 (Caffeine Cache — Backend) — CHECKPOINTS CORREGIDOS

| ID | Checkpoint | Método de Verificación | Resultado Esperado |
|----|-----------|----------------------|-------------------|
| **QA-DT039D-01** | Dependencia Caffeine en classpath | Buscar en `pom.xml`: `com.github.ben-manes.caffeine` | ✅ Presente |
| **QA-DT039D-02** | ~~Config application.yml~~ **ELIMINADO** | ~~Buscar spring.cache.type: caffeine~~ | **N/A — NO debe existir. Si existe, es un ERROR.** |
| **QA-DT039D-02-V2** | Bean `caffeineCacheManager` en CacheConfig | Buscar en `CacheConfig.java`: `@Bean("caffeineCacheManager")` + `CaffeineCacheManager` | Bean con `maximumSize(50)` y `expireAfterWrite(5, TimeUnit.MINUTES)` |
| **QA-DT039D-03** | @EnableCaching activo | Buscar `@EnableCaching` en `CacheConfig.java` | ✅ Presente |
| **QA-DT039D-04** | Redis NO fue afectado | Buscar en `CacheConfig.java`: `redisCacheManagerBuilderCustomizer` | ✅ Bean Redis sigue presente con `workdesk_tasks` TTL 10s |
| **QA-DT039D-05** | BpmTaskService apunta a Caffeine | Buscar en `BpmTaskService.java` L192: `cacheManager = "caffeineCacheManager"` | ✅ Atributo `cacheManager` presente en `@Cacheable` |
| **QA-DT039D-06** | application.yml SIN spring.cache.type | Verificar que `application.yml` NO contiene `spring.cache.type` | ✅ Ausente (correcto — Redis es default por auto-config) |
| **QA-DT039D-07** | Compilación exitosa | Ejecutar protocolo SRE Backend | `BUILD SUCCESS` |

**Comandos de verificación:**
```bash
cd backend/ibpms-core

# Verificar dependencia Caffeine
grep -n "caffeine" pom.xml

# Verificar bean Caffeine en CacheConfig
grep -n "caffeineCacheManager" src/main/java/com/ibpms/poc/infrastructure/config/CacheConfig.java

# Verificar que Redis sigue intacto
grep -n "redisCacheManagerBuilderCustomizer" src/main/java/com/ibpms/poc/infrastructure/config/CacheConfig.java

# Verificar que BpmTaskService usa el cacheManager correcto
grep -n "caffeineCacheManager" src/main/java/com/ibpms/poc/application/service/bpm/BpmTaskService.java

# Verificar que application.yml NO tiene spring.cache.type
grep -n "cache.type" src/main/resources/application.yml
# Resultado esperado: SIN RESULTADOS (correcto)
```

---

## 4. Certificar REM-039-C (Draft Banner Test — Frontend)

| ID | Checkpoint | Método de Verificación | Resultado Esperado |
|----|-----------|----------------------|-------------------|
| **QA-REM039C-01** | Test file existe | `ls frontend/src/tests/views/admin/GenericForm/GenericFormView.spec.ts` | Archivo presente |
| **QA-REM039C-02** | Tests nuevos pasan | `npx vitest run src/tests/views/admin/GenericForm/GenericFormView.spec.ts` | 3/3 tests PASS |
| **QA-REM039C-03** | Nombres siguen convención | Inspeccionar output de Vitest | Tests: `QA-039-C-01`, `QA-039-C-02`, `QA-039-C-03` |
| **QA-REM039C-04** | Regresión suite previa | `npx vitest run src/tests/components/forms/generic/DraftRestorationBanner.spec.ts` | 3/3 tests previos PASS |
| **QA-REM039C-05** | Componente DraftSyncIndicator intacto | Verificar que `DraftSyncIndicator.vue` NO fue modificado por el agente Frontend | Archivo sin cambios |

> [!WARNING]
> **Sobre QA-REM039C-04:** Si los tests de `DraftRestorationBanner.spec.ts` fallan, verifica PRIMERO que `DraftSyncIndicator.vue` y `genericFormStore.ts` no hayan sido modificados por el agente Frontend. Estos archivos NO debían ser tocados según el handoff. Si están intactos y los tests fallan, ejecuta `npx vitest run` con `--reporter=verbose` para ver el detalle del error y reportar al Arquitecto.

**Comandos de verificación:**
```bash
cd frontend

# Test nuevo (debe pasar)
npx vitest run src/tests/views/admin/GenericForm/GenericFormView.spec.ts

# Test anterior — REGRESIÓN (debe seguir pasando)
npx vitest run src/tests/components/forms/generic/DraftRestorationBanner.spec.ts

# Si hay fallo en regresión, verificar que el componente no fue modificado:
git diff src/components/forms/generic/DraftSyncIndicator.vue
git diff src/stores/genericFormStore.ts
# Resultado esperado: SIN CAMBIOS en ambos
```

---

## 5. Reporte de Certificación (Template V2)

```markdown
## Reporte de Certificación QA V2 — US-039 (DT-039-02 + REM-039-C)

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-DT039D-01 | Caffeine en classpath | ✅/❌ | [output grep pom.xml] |
| QA-DT039D-02-V2 | Bean caffeineCacheManager | ✅/❌ | [output grep CacheConfig.java] |
| QA-DT039D-03 | @EnableCaching | ✅/❌ | [output grep] |
| QA-DT039D-04 | Redis NO afectado | ✅/❌ | [output grep redisCacheManagerBuilderCustomizer] |
| QA-DT039D-05 | BpmTaskService → Caffeine | ✅/❌ | [output grep cacheManager] |
| QA-DT039D-06 | application.yml sin cache.type | ✅/❌ | [output grep — sin resultados = PASS] |
| QA-DT039D-07 | Compilación | ✅/❌ | BUILD SUCCESS |
| QA-REM039C-01 | Test file existe | ✅/❌ | [path] |
| QA-REM039C-02 | 3/3 tests PASS | ✅/❌ | [output vitest] |
| QA-REM039C-03 | Convención nombres | ✅/❌ | [inspection] |
| QA-REM039C-04 | Regresión suite previa | ✅/❌ | [output vitest + git diff] |
| QA-REM039C-05 | DraftSyncIndicator intacto | ✅/❌ | [git diff — sin cambios] |

**Veredicto:** PASS / FAIL
**Fecha:** YYYY-MM-DD
```

---

## 6. Mensaje de Despacho

> **Instrucciones para el Agente QA (Re-certificación V2):**
>
> ⚠️ **Tu certificación anterior usó checkpoints desactualizados.** Lee este documento COMPLETO — reemplaza tu handoff anterior.
>
> **Cambio principal:** NO busques `spring.cache.type: caffeine` en `application.yml`. Eso era la instrucción ORIGINAL que fue descartada por el Arquitecto Líder porque rompería Redis. La solución correcta es un **bean secundario** `@Bean("caffeineCacheManager")` en `CacheConfig.java`.
>
> Tu tarea:
> 1. Ejecutar los 7 checkpoints de DT-039-02 (sección 3) — nota que QA-DT039D-02 fue reemplazado por QA-DT039D-02-V2.
> 2. Ejecutar los 5 checkpoints de REM-039-C (sección 4).
> 3. Si QA-REM039C-04 falla de nuevo, ejecutar `git diff` sobre `DraftSyncIndicator.vue` y `genericFormStore.ts` para descartar modificaciones no autorizadas.
> 4. Rellenar el template V2 de la sección 5.
>
> **Rama:** `sprint-6`. PROHIBIDO trabajar en `main`.
