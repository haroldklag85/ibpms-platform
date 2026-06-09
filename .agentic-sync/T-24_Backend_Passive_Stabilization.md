# 🧠→⚙️ Handoff: Arquitecto → Backend Java
# T-24-BACKEND: Estabilización Pasiva J-02

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-13T17:55:00-05:00
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🟡 Media (Reactiva)
**Dependencia:** Hallazgos del Agente QA

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skill principal del agente receptor
cat ibpms-platform/.agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md

# 4. ADRs relevantes
cat ibpms-platform/docs/architecture/adr-001-hexagonal-architecture.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo/corregido DEBE llevar la anotación `@Traceability` o comentario `// @Traceability: Bugfix QA J-02 (T-24)`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El Backend para J-02 (Low-Code) ya cuenta con sus adaptadores JPA y Controladores implementados (Zero-Mock en operación). En este ciclo, el rol del Backend es estrictamente de **estabilización pasiva (Fixing)**. Sólo se intervendrá el código si los scripts de QA Playwright revelan defectos reales de persistencia, concurrencia o de RBAC.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Pendiente Bug Reports | (Por confirmar) | Intervención requerida solo bajo solicitud del Agente QA o reportes de fallos E2E. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Monitoreo y Fixing Reactivo

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/` (Controladores y Adaptadores J-02)

Si QA reporta una falla (ej. HTTP 500 al guardar un DMN o problemas de descarte de variables en BPMN), debes:
1. Replicar la prueba a nivel local o analizar el stack trace.
2. Identificar la raíz (ej. validaciones nulas, roles faltantes en JWT).
3. Aplicar el parche preservando la pureza Hexagonal (ADR-001).

```java
// Snippet prescriptivo — Ejemplo de fix esperado si falla la persistencia DMN:
// @Traceability: Bugfix QA J-02 (T-24)
public DmnModelEntity saveModel(DmnModelDTO dto, String tenantId) {
    if(dto.getXmlContent() == null || dto.getXmlContent().isBlank()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XML nulo");
    }
    // ...
}
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Compilación Exitosa | `mvn clean compile` se ejecuta sin errores. |
| 2 | Corrección Funcional | La regresión reportada por QA ahora pasa exitosamente en la DB local. |
| 3 | Trazabilidad Inyectada | Comentarios `@Traceability: Bugfix QA J-02 (T-24)` presentes. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. (Esperar reporte de QA).
2. Analizar y corregir el archivo reportado.
3. Compilar: `cd backend/ibpms-core && mvn clean compile`
4. Commit: `git add . && git commit -m "fix(backend): corrección de persistencia J-02 reportada por QA" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de ⚙️ BACKEND - JAVA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
5. cat ibpms-platform/docs/architecture/adr-001-hexagonal-architecture.md
6. cat ibpms-platform/.agentic-sync/T-24_Backend_Passive_Stabilization.md

TU MISIÓN:

1. Proveer estabilización pasiva (Fixing) para resolver exclusivamente los bugs encontrados por el Agente QA durante la certificación E2E de J-02 (BPMN/DMN).
2. Compile: `cd backend/ibpms-core && mvn clean compile`
3. Commit: `git add . && git commit -m "fix(backend): estabilización E2E J-02" && git push`

REGLAS INQUEBRANTABLES:
- OBLIGATORIO inyectar `// @Traceability: Bugfix QA J-02 (T-24)` en cualquier ajuste.
- PROHIBIDO desarrollar features nuevos, tu rol es REACTIVO (estabilización de J-02).
```
