# Handoff QA — ARQ-028-02 | Certificación de Refactorización Hexagonal

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Certificar** | **ARQ-028-02:** Abstracción de AuditLog (Hexagonal) |
| **Dependencia** | Ejecutar DESPUÉS de que Backend notifique su finalización |

---

## 2. Puntos de Certificación (Checkpoints)

Verifica que el Agente Backend haya cumplido con el estándar de Arquitectura Hexagonal.

| ID | Checkpoint | Método de Verificación | Resultado Esperado |
|----|-----------|----------------------|-------------------|
| **QA-ARQ028-01** | Puerto creado | `ls backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/AuditLogPort.java` | Archivo presente |
| **QA-ARQ028-02** | Adaptador creado | `ls backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/` | Archivo `AuditLogJpaAdapter.java` (o similar) presente |
| **QA-ARQ028-03** | Servicio Limpio de SQL | `grep "JdbcTemplate" backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FormCertificationService.java` | **SIN RESULTADOS** (0 matches) |
| **QA-ARQ028-04** | Inyección Correcta | `grep "AuditLogPort" backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FormCertificationService.java` | Presente en import y constructor |
| **QA-ARQ028-05** | Compilación y Tests | `cd backend/ibpms-core && mvn clean test` | **BUILD SUCCESS** |

---

## 3. Reporte de Certificación (Template)

Rellena y entrega este reporte al Arquitecto Líder.

```markdown
## Reporte de Certificación QA — ARQ-028-02

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-ARQ028-01 | Puerto creado | ✅/❌ | [path] |
| QA-ARQ028-02 | Adaptador creado | ✅/❌ | [path] |
| QA-ARQ028-03 | Servicio Limpio | ✅/❌ | grep sin resultados |
| QA-ARQ028-04 | Inyección Correcta| ✅/❌ | presente |
| QA-ARQ028-05 | Tests Backend | ✅/❌ | BUILD SUCCESS |

**Veredicto:** PASS / FAIL
```
