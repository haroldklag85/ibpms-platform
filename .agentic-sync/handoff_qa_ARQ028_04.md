# Handoff QA — ARQ-028-04 | Certificación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta |
| **Dependencia** | Ejecutar DESPUÉS de que Backend notifique éxito. |

---

## 2. Puntos de Certificación (Checkpoints)

Verifica que el refactor estructural se realizó correctamente sin romper los contratos.

| ID | Checkpoint | Método de Verificación | Resultado Esperado |
|----|-----------|----------------------|-------------------|
| **QA-02804-01** | Liquibase Script Creado | Buscar nuevo `.sql` de separación de BD | Archivo presente y válido |
| **QA-02804-02** | FormDefinition Limpio | `grep "isQaCertified" backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormDefinitionEntity.java` | **SIN RESULTADOS** |
| **QA-02804-03** | Nueva Entidad Creada | Buscar `FormCertificationEntity.java` | Archivo presente con mapeos JPA |
| **QA-02804-04** | API Contract Intacto | Revisar si los DTOs de salida del Form Controller perdieron atributos | Los DTOs mantienen la compatibilidad con el front |
| **QA-02804-05** | Compilación y Tests | `cd backend/ibpms-core && mvn clean test` | **BUILD SUCCESS** |

---

## 3. Reporte de Certificación (Template)

```markdown
## Reporte de Certificación QA — ARQ-028-04

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-02804-01 | Script BD | ✅/❌ | |
| QA-02804-02 | Entidad Limpia | ✅/❌ | |
| QA-02804-03 | Nueva Entidad | ✅/❌ | |
| QA-02804-04 | DTOs intactos | ✅/❌ | |
| QA-02804-05 | Tests | ✅/❌ | |

**Veredicto:** PASS / FAIL
```
