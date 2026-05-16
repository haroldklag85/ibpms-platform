# 🧠→⚙️ Handoff: Arquitecto Líder → Backend
# T-04: Refactorizar WorkdeskAttendNextController → Application Service (ADR-001)
# T-05: Completar FeatureToggleController con Audit Log (CA-08/CA-16)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [⚙️ BACKEND - JAVA]
**Fecha:** 2026-05-11T22:07:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 ALTA

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Tu skill de compilación y SRE
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Estándares de código limpio
cat .agents/skills/clean_code_standards/SKILL.md

# 4. Zero-Mock enforcement
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 5. ADR-001 (Hexagonal Architecture)
cat docs/architecture/adr-001-hexagonal-architecture.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `@Traceability(US = "US-001", CA = {"CA-XX"})` en cada clase y método público. Esto es INNEGOCIABLE.

---

## 🎯 TAREA T-04: Extraer lógica de WorkdeskAttendNextController → Application Service

### Diagnóstico del Arquitecto

El archivo `WorkdeskAttendNextController.java` viola el ADR-001 en múltiples dimensiones:

| Violación | Línea | Detalle |
|-----------|:-----:|---------|
| Import directo de `@Entity` JPA | 9-16 | Importa `FeatureToggleEntity`, `TaskSkipEntity`, `WorkdeskProjectionEntity`, `UserEntity` |
| Inyección de Repositories en Controller | 39-44 | `WorkdeskProjectionRepository`, `FeatureToggleRepository`, `TaskSkipRepository`, `UserRepository` |
| `@Transactional` en Controller | 63, 127 | La gestión transaccional debe vivir en el Application Service |
| Lógica de negocio en Controller | 72-103 | Toggle validation, skill parsing, task assignment, skip counting — TODO debe estar en un UseCase |

### Instrucciones Quirúrgicas

**Paso 1: Crear el Puerto de Entrada (Input Port)**

Archivo: `src/main/java/com/ibpms/poc/application/port/in/AttendNextTaskUseCase.java`
```java
package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.SkipReasonDTO;
import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;

/**
 * Puerto de entrada para el caso de uso "Atender Siguiente Tarea".
 * @Traceability(US = "US-001", CA = {"CA-28", "CA-21", "CA-16"})
 */
public interface AttendNextTaskUseCase {
    WorkdeskGlobalItemDTO attendNext(String userId);
    WorkdeskGlobalItemDTO skipAndAttendNext(String userId, SkipReasonDTO skipReason);
}
```

**Paso 2: Crear el Application Service (Caso de Uso)**

Archivo: `src/main/java/com/ibpms/poc/application/service/AttendNextTaskService.java`

- Debe inyectar **Puertos de Salida** (interfaces), NO repositorios JPA directos.
- Los puertos necesarios a crear/reutilizar:
  - `WorkdeskProjectionPort` → `findNextAvailableTask(tenantId, skills)`, `save()`, `findById()`
  - `FeatureTogglePort` → `findByTenantIdAndToggleKey()` (o reutilizar `UpdateFeatureToggleUseCase.isFeatureEnabled()`)
  - `TaskSkipPort` → `save(TaskSkipEntity)`, `countRecentSkips()`
  - `UserPort` → `findByUsername()`
- Mover TODA la lógica de líneas 60-173 del controlador actual al servicio.
- Mantener `@Transactional` SOLO en el servicio.
- Mantener las llamadas a `SimpMessagingTemplate` — estas son "ports de salida" de notificación y pueden quedarse aquí o extraerse a un `WorkdeskNotificationPort`.

**Paso 3: Refactorizar el Controller**

El `WorkdeskAttendNextController` SOLO debe:
1. Extraer el `userId` del `Authentication`.
2. Invocar `attendNextTaskUseCase.attendNext(userId)`.
3. Retornar el DTO.
4. CERO imports de `@Entity`, `@Repository` o `@Transactional`.

**Paso 4: Crear/actualizar Adapters JPA**

Si no existen, crear en `infrastructure/adapters/`:
- `WorkdeskProjectionJpaAdapter implements WorkdeskProjectionPort`
- `TaskSkipJpaAdapter implements TaskSkipPort`

> **NOTA:** El `UserRepository` ya puede tener un adapter existente. Verificar antes de crear duplicados.

### Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Controller sin imports de `@Entity` ni `@Repository` | Grep: `0 results` para `import.*infrastructure.jpa` en el controller |
| 2 | `@Transactional` solo en Application Service | Grep: `0 results` para `@Transactional` en el controller |
| 3 | `@Traceability` en cada clase nueva | Anotación presente en UseCase, Service y Adapters |
| 4 | `mvn compile -DskipTests` → BUILD SUCCESS | Log de terminal |
| 5 | `mvn spring-boot:run -Dspring-boot.run.profiles=e2e` → "Tomcat started on port 8080" | Log de terminal |

---

## 🎯 TAREA T-05: Completar FeatureToggleController (Audit Log + Swagger)

### Diagnóstico del Arquitecto

El `FeatureToggleController.java` ya sigue ADR-001 correctamente (usa `UpdateFeatureToggleUseCase`). **Sin embargo**, tiene 2 brechas funcionales pendientes:

| Brecha | Línea | Detalle |
|--------|:-----:|---------|
| Sin Audit Log en PUT | 57-63 | El PUT `/feature-toggles/{key}` actualiza el toggle pero NO deja huella inmutable en el Audit Log (CA-16 exige: "dejar huella inmutable en el Audit Log Central en los encendidos de madrugada") |
| Sin validación de payload | 60 | No valida que `body` contenga la key `"enabled"` — puede lanzar NPE con body vacío |

### Instrucciones Quirúrgicas

1. En `FeatureToggleService.updateFeatureToggle()`, después de persistir el cambio, invocar `AuditLogPort.saveAuditLog()` con:
   - `action`: `"FEATURE_TOGGLE_CHANGED"`
   - `details`: JSON con `{key, previousValue, newValue, changedBy, timestamp}`
2. En el Controller, validar el body con `@Valid` o un guard manual que lance `400 Bad Request` si `"enabled"` no está presente.
3. Agregar `@Traceability(US = "US-001", CA = {"CA-08", "CA-16"})` al método `updateFeatureToggle`.

### Criterios de Aceptación

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | PUT deja registro en `ibpms_audit_log` | Query SQL post-invocación |
| 2 | PUT con body vacío retorna HTTP 400 | Test unitario o curl |
| 3 | `@Traceability` en el método | Inspección visual |
| 4 | BUILD SUCCESS + arranque exitoso | Log de terminal |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Ejecutar T-04 primero (es la deuda mayor)
2. Ejecutar T-05 después (es incremental)
3. Compilar: `..\..\maven\apache-maven-3.9.6\bin\mvn.cmd compile -DskipTests`
4. Arrancar: `..\..\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=e2e -Dmaven.test.skip=true`
5. Commit: `git add . && git commit -m "refactor(US-001): extract AttendNextTaskService — ADR-001 compliance" && git push`

---

**RECUERDA:** Si en cualquier momento el RAG no te devuelve información suficiente, DETENTE y pregunta al Humano (LEY GLOBAL 0).
