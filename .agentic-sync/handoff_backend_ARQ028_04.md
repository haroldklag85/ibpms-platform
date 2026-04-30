# Handoff Backend — ARQ-028-04 | Segregación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta (`FormDefinitionEntity.java`) |
| **Dependencia** | Ejecutar **DESPUÉS** de que Infra/BD haya creado el changeset Liquibase y hecho push. |

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto |
|-----|---------|
| `adr-001-hexagonal-architecture.md` | La nueva `FormCertificationEntity` vive en `infrastructure/jpa/entity/` (capa de infraestructura). El servicio de aplicación (`FormCertificationService`) interactúa a través de Puertos (`FormDefinitionPort`), no directamente con repositorios. |
| `adr_009_postgresql_pgvector_migration.md` | La nueva tabla `ibpms_form_certifications` fue creada por Infra/BD vía Liquibase. |
| `adr_010_testing_pyramid_governance.md` | Se requieren tests unitarios para el nuevo Repository y de integración para el flujo de certificación. |

**Trazabilidad:** `FormDefinitionEntity.java` (líneas 101-145) contiene actualmente 4 campos de certificación QA (`isQaCertified`, `certifiedSchemaHash`, `certifiedBy`, `certifiedAt`) que deben ser extraídos a una nueva entidad. El servicio `FormCertificationService.java` ya usa `FormDefinitionPort` (ARQ-028-01 cerrado). El adaptador JPA debe adaptarse para leer/escribir en la nueva tabla sin romper el contrato REST.

---

## 3. Rutas Exactas y Contexto Preexistente

| Archivo | Estado Actual | Acción |
|---------|--------------|--------|
| `backend/.../infrastructure/jpa/entity/FormDefinitionEntity.java` | Contiene campos QA en líneas 101-145: `isQaCertified`, `certifiedSchemaHash`, `certifiedBy`, `certifiedAt`. | **ELIMINAR** esos 4 campos y sus getters/setters. |
| `backend/.../infrastructure/jpa/entity/FormCertificationEntity.java` | **NO EXISTE** | **CREAR** nueva entidad mapeada a `ibpms_form_certifications`. |
| `backend/.../infrastructure/jpa/repository/FormCertificationRepository.java` | **NO EXISTE** | **CREAR** JpaRepository para la nueva entidad. |
| `backend/.../application/port/out/FormDefinitionPort.java` | Puerto existente para definiciones de formularios. | **ADAPTAR** si expone métodos de certificación. |
| `backend/.../application/service/FormCertificationService.java` | Servicio que orquesta la certificación QA. | **ADAPTAR** para que persista en `FormCertificationEntity` en lugar de modificar flags en `FormDefinitionEntity`. |

---

## 4. Snippets Prescriptivos

### 4.1 Nueva Entidad: `FormCertificationEntity.java`
```java
package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_form_certifications")
public class FormCertificationEntity {

    @Id
    private UUID id;

    @Column(name = "form_definition_id", nullable = false)
    private UUID formDefinitionId;

    @Column(name = "is_qa_certified", nullable = false)
    private Boolean isQaCertified = false;

    @Column(name = "certified_schema_hash", length = 64)
    private String certifiedSchemaHash;

    @Column(name = "certified_by", length = 100)
    private String certifiedBy;

    @Column(name = "certified_at")
    private LocalDateTime certifiedAt;

    public FormCertificationEntity() {
        this.id = UUID.randomUUID();
    }

    // Getters y Setters completos...
}
```

### 4.2 Nuevo Repository
```java
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormCertificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FormCertificationRepository extends JpaRepository<FormCertificationEntity, UUID> {
    Optional<FormCertificationEntity> findByFormDefinitionId(UUID formDefinitionId);
}
```

### 4.3 Mantener Contrato API Intacto
Los DTOs que viajan al Frontend **DEBEN** seguir conteniendo los campos de certificación. El Mapper (o el servicio) debe unificar los datos de `FormDefinitionEntity` + `FormCertificationEntity` antes de enviarlos al Controller. **NO SE PERMITE** romper la estructura JSON existente del endpoint.

---

## 5. Criterios de Aceptación

- [ ] `FormDefinitionEntity.java` **NO** contiene campos `isQaCertified`, `certifiedSchemaHash`, `certifiedBy`, `certifiedAt`.
- [ ] `FormCertificationEntity.java` existe y está mapeada a `ibpms_form_certifications`.
- [ ] `FormCertificationRepository.java` existe con método `findByFormDefinitionId`.
- [ ] `FormCertificationService.java` persiste certificaciones en la nueva entidad.
- [ ] El endpoint REST devuelve **la misma estructura JSON** que antes (retrocompatibilidad).
- [ ] `mvn clean test` → **BUILD SUCCESS**.

---

## 6. Instrucciones Operativas y de Comunicación

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> ⚠️ Notifica finalización a QA para certificación.
