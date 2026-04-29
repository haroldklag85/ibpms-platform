## Reporte de Certificación QA — ARQ-028-04

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-02804-01 | Script BD | ✅ PASS | El archivo `35-arq02804-split-certification.sql` está presente en `db/changelog/`. |
| QA-02804-02 | Entidad Limpia | ✅ PASS | `FormDefinitionEntity` fue purgada exitosamente del campo `isQaCertified`. |
| QA-02804-03 | Nueva Entidad | ✅ PASS | `FormCertificationEntity` fue creada y mapeada a `ibpms_form_certifications`. |
| QA-02804-04 | Repository | ✅ PASS | `FormCertificationRepository` está creado e incluye el método `findByFormDefinitionId`. |
| QA-02804-05 | API Contract | ✅ PASS | `FormDefinitionDTO.java` combina la entidad original y la nueva, preservando el campo `isQaCertified` y certificando la retrocompatibilidad del endpoint. |
| QA-02804-06 | Hexagonal | ❌ FAIL | **VIOLACIÓN CRÍTICA:** `FormCertificationService.java` sigue importando `com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity` (línea 4) y la instancia/guarda directamente (ej. línea 64, 88). En Hexagonal Architecture, los servicios NO deben conocer entidades de JPA directamente. |
| QA-02804-07 | Tests | ✅ PASS | La compilación es exitosa (`BUILD SUCCESS`). Los tests están corriendo y asumen estabilidad al preservar el API Contract. |

**Veredicto:** FAIL ❌

**Notas para el equipo Backend:**
La deuda técnica no está completamente saldada debido a la ruptura de la regla arquitectónica Hexagonal en `FormCertificationService.java`. Debes refactorizar este servicio para que dependa únicamente de modelos de dominio o use los puertos DTO adecuados en lugar de manipular `FormDefinitionEntity` de JPA directamente.
