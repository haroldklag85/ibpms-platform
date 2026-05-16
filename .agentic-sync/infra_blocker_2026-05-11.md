# [🕵️ QA - E2E] INFRA BLOCKER — Backend No Arranca (Sprint 7, J-04 Iter.2)

**Fecha:** 2026-05-11T20:21:00-05:00
**Agente:** QA E2E
**Severity:** P0 — BLOQUEANTE (No se pueden ejecutar tests E2E)
**Skill Activado:** `qa_e2e_validation_audit/SKILL.md` §5

---

## Error Reportado

```
APPLICATION FAILED TO START

Parameter 0 of constructor in com.ibpms.poc.application.service.FormCertificationService 
required a bean of type 'com.ibpms.poc.application.port.out.FormDefinitionPort' 
that could not be found.
```

## Causa Raíz (Diagnóstico QA)

Existe una **duplicación de paquetes** para la interfaz `FormDefinitionPort`:

| Paquete | Quién la usa | Adapter JPA |
|---------|-------------|-------------|
| `com.ibpms.poc.application.port.out.FormDefinitionPort` | `FormCertificationService`, `FormDefinitionController` | ❌ Ninguno |
| `com.ibpms.poc.application.ports.out.FormDefinitionPort` | `FormCompletionService`, `FormCompletionServiceTest` | ✅ `FormDefinitionJpaAdapter` |

**El adapter `FormDefinitionJpaAdapter` implementa la versión con `ports` (plural), pero `FormCertificationService` importa la versión con `port` (singular).** Spring no encuentra un bean que satisfaga la interfaz singular.

## Impacto en la Certificación J-04

- ❌ **18 tests E2E BLOQUEADOS** — No se pueden ejecutar sin backend vivo
- ✅ Compilación estática (`mvn compile`) pasa — el error es solo en runtime DI
- ✅ Corrección del `@PreAuthorize` fue verificada correctamente (no causa este error)
- ✅ Infraestructura Docker está healthy (PostgreSQL, RabbitMQ, Redis, Camunda)

## Acciones Completadas

| # | Acción | Estado |
|---|--------|--------|
| 1 | Lectura de 4 archivos de governance | ✅ Completado |
| 2 | Verificación de corrección `@PreAuthorize` | ✅ Confirmado (línea 17) |
| 3 | `mvn compile -DskipTests` | ✅ BUILD SUCCESS |
| 4 | Deprecar `us008-kanban-hub.spec.ts` | ✅ Renombrado a `.deprecated` |
| 5 | Levantar backend nativo | ❌ FALLA — FormDefinitionPort DI |
| 6 | Ejecutar 18 tests Playwright | ⏸️ BLOQUEADO |

## Remediación Sugerida para el Arquitecto

**Opción A (Recomendada):** Unificar los dos paquetes. Mover `FormDefinitionPort` del paquete `port` (singular) al paquete `ports` (plural) donde vive el adapter:
```java
// En FormCertificationService.java y FormDefinitionController.java, cambiar:
// import com.ibpms.poc.application.port.out.FormDefinitionPort;
// por:
import com.ibpms.poc.application.ports.out.FormDefinitionPort;
```

**Opción B:** Crear un segundo adapter que implemente la interfaz singular.

---

**Conforme al Skill §5:** _"Si tras 2 intentos el backend no arranca, NO ejecutes tests. Reporta el bloqueo."_

Quedo en espera de resolución del Arquitecto para re-ejecutar la suite completa.
