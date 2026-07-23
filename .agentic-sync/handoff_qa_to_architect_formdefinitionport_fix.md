# 🕵️→🧠 Handoff: QA E2E → Arquitecto Líder
# Bloqueante P0: Backend NO Arranca — FormDefinitionPort Package Split

**Emitido por:** [🕵️ QA - E2E]
**Destinatario:** [🧠 ARQUITECTO LÍDER]
**Fecha:** 2026-05-11T20:27:00-05:00
**Prioridad:** P0 — BLOQUEANTE (18 tests E2E suspendidos)
**Skill Activado:** `qa_e2e_validation_audit/SKILL.md` §5

---

## 📋 Contexto de la Certificación

Recibí el handoff de Iteración 2 y ejecuté la secuencia ordenada:

| Paso | Acción | Estado |
|:----:|--------|:------:|
| 0 | Lectura de 4 archivos de governance | ✅ |
| 1 | `mvn compile -DskipTests` | ✅ BUILD SUCCESS (4s) |
| 2 | Deprecar `us008-kanban-hub.spec.ts` → `.deprecated` | ✅ |
| 3 | Verificación corrección `@PreAuthorize` línea 17 | ✅ `hasAnyRole('ADMIN_IT', 'SUPER_ADMIN')` |
| 4 | Levantar backend nativo (`spring-boot:run -Dspring-boot.run.profiles=e2e`) | ❌ APPLICATION FAILED TO START |
| 5 | Ejecutar 18 tests Playwright | ⏸️ BLOQUEADO |

La infraestructura Docker está 100% operativa:
- PostgreSQL: `healthy` en `:5433`
- RabbitMQ: `healthy` en `:5673`
- Redis: `healthy` en `:6380`
- Camunda: `healthy` en `:8085`

---

## 🚨 Error Exacto

```
***************************
APPLICATION FAILED TO START
***************************

Description:
Parameter 0 of constructor in com.ibpms.poc.application.service.FormCertificationService
required a bean of type 'com.ibpms.poc.application.port.out.FormDefinitionPort'
that could not be found.

Action:
Consider defining a bean of type 'com.ibpms.poc.application.port.out.FormDefinitionPort'
in your configuration.
```

---

## 🔬 Diagnóstico Forense

Existen **DOS interfaces** con el mismo nombre `FormDefinitionPort` en **DOS paquetes** distintos, con **contratos incompatibles**:

### Interfaz A: `application.port.out.FormDefinitionPort` (singular, 32 puertos en el paquete)
**Archivo:** `src/main/java/com/ibpms/poc/application/port/out/FormDefinitionPort.java`
```java
package com.ibpms.poc.application.port.out;

public interface FormDefinitionPort {
    boolean existsById(UUID id);
    Optional<FormDefinition> findById(UUID id);
    List<FormDefinition> findByFormIdOrderByVersionIdDesc(UUID formId);
    FormDefinition save(FormDefinition formDefinition);
}
```
**Consumidores:**
- `FormCertificationService.java` (línea 5)
- `FormDefinitionController.java` (línea 4)

**Adapter que la implementa:** ❌ NINGUNO → `UnsatisfiedDependencyException`

### Interfaz B: `application.ports.out.FormDefinitionPort` (plural, 10 puertos en el paquete)
**Archivo:** `src/main/java/com/ibpms/poc/application/ports/out/FormDefinitionPort.java`
```java
package com.ibpms.poc.application.ports.out;

public interface FormDefinitionPort {
    Optional<String> findSchemaContentByVersion(String schemaVersion);
}
```
**Consumidores:**
- `FormCompletionService.java` (línea 13)

**Adapter que la implementa:** ✅ `FormDefinitionJpaAdapter.java` (`@Component`)

---

## 🔧 Fix Quirúrgico (Instrucciones Exactas)

El problema tiene 2 dimensiones: los contratos son **distintos** (no es un simple typo de import).

### Opción A: Crear adapter faltante para la interfaz singular (RECOMENDADA)

Crear `FormDefinitionFullAdapter.java` que implemente la interfaz del paquete `port` (singular) usando el `FormDefinitionRepository` existente:

**Archivo nuevo:** `src/main/java/com/ibpms/poc/infrastructure/adapters/FormDefinitionFullAdapter.java`
```java
package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.domain.model.FormDefinition;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FormDefinitionFullAdapter implements FormDefinitionPort {

    private final FormDefinitionRepository repository;

    public FormDefinitionFullAdapter(FormDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<FormDefinition> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<FormDefinition> findByFormIdOrderByVersionIdDesc(UUID formId) {
        return repository.findByFormIdOrderByVersionIdDesc(formId)
            .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public FormDefinition save(FormDefinition domain) {
        FormDefinitionEntity entity = toEntity(domain);
        FormDefinitionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    // Mappers Domain ↔ Entity (adaptar según los campos reales de FormDefinitionEntity)
    private FormDefinition toDomain(FormDefinitionEntity e) {
        FormDefinition d = new FormDefinition();
        d.setId(e.getId());
        d.setFormId(e.getFormId());
        d.setVersionId(e.getVersionId());
        d.setSchemaContent(e.getSchemaContent());
        d.setCreatedBy(e.getCreatedBy());
        d.setHashSha256(e.getHashSha256());
        return d;
    }

    private FormDefinitionEntity toEntity(FormDefinition d) {
        FormDefinitionEntity e = new FormDefinitionEntity();
        e.setId(d.getId());
        e.setFormId(d.getFormId());
        e.setVersionId(d.getVersionId());
        e.setSchemaContent(d.getSchemaContent());
        e.setCreatedBy(d.getCreatedBy());
        e.setHashSha256(d.getHashSha256());
        return e;
    }
}
```

> **NOTA:** Verificar los campos exactos de `FormDefinitionEntity` y `FormDefinition` (domain model) antes de crear los mappers. Además, verificar si `FormDefinitionRepository` ya expone `findByFormIdOrderByVersionIdDesc()` — si no, agregarlo como query method JPA.

### Opción B: Unificar las dos interfaces (Refactor mayor)

1. Fusionar ambas interfaces en una sola en `application.port.out.FormDefinitionPort`
2. Actualizar `FormDefinitionJpaAdapter` para implementar la interfaz unificada
3. Corregir imports en `FormCompletionService` de `ports` → `port`
4. Eliminar el paquete `ports` si queda vacío tras la migración

**Riesgo:** Puede afectar `FormCompletionService` y sus tests unitarios.

### Opción C: Quick-fix temporal (solo para desbloquear QA)

Si necesitas desbloquear los tests YA sin resolver la deuda arquitectónica:

**Archivo:** `FormCertificationService.java`, líneas 5-7
```diff
- import com.ibpms.poc.application.port.out.FormDefinitionPort;
- import com.ibpms.poc.application.port.out.FormCertificationPort;
- import com.ibpms.poc.application.port.out.AuditLogPort;
+ import com.ibpms.poc.application.ports.out.FormDefinitionPort; // FIX: usar paquete con adapter
```

⚠️ **ALERTA:** Esto cambiará el contrato de `FormDefinitionPort` que `FormCertificationService` ve — el de `ports` solo tiene `findSchemaContentByVersion()`, NO tiene `existsById()`, `findById()`, `save()`. **Esto causará errores de compilación.** Solo funciona con Opción A o B.

---

## 📊 Mapa de Impacto (Árbol de Dependencias)

```
FormDefinitionPort (port/singular)
├── FormCertificationService.java   → existsById, findById, save
├── FormDefinitionController.java   → findById, save, findByFormIdOrderByVersionIdDesc
└── ADAPTER: ❌ NINGUNO

FormDefinitionPort (ports/plural)
├── FormCompletionService.java      → findSchemaContentByVersion
└── ADAPTER: ✅ FormDefinitionJpaAdapter.java (@Component)
```

---

## ✅ Validación Post-Fix (Para el Arquitecto)

Después de aplicar el fix, el Arquitecto DEBE ejecutar:

```bash
# 1. Compilar
cd backend/ibpms-core
..\..\maven\apache-maven-3.9.6\bin\mvn.cmd compile -DskipTests

# 2. Arrancar con perfil E2E
cmd /c "cd /d backend\ibpms-core && ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=e2e -Dmaven.test.skip=true"

# 3. Esperar: "Started Application" + "Tomcat started on port 8080"

# 4. Health check
curl http://localhost:8080/actuator/health
```

Si arranca exitosamente, **notificar al Humano** para que re-invoque al agente QA con:

> _"Agente QA: el backend ya arranca. Re-ejecuta los 18 tests de certificación J-04."_

---

**Nota:** La corrección del `@PreAuthorize` en `SessionRevocationController.java` fue verificada correctamente (línea 17: `hasAnyRole('ADMIN_IT', 'SUPER_ADMIN')`). Este bug de DI es **preexistente** y no fue causado por esa corrección.
