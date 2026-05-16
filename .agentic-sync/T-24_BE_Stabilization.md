# 🧠→⚙️ Handoff: ARQUITECTO LÍDER → BACKEND
# T-24-BE: Estabilización Pasiva (Fixing) para Certificación J-02

**Emitido por:** 🧠 ARQUITECTO LÍDER (Antigravity)
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-14T03:34:00-05:00
**Sprint:** 6 — Iteración 7.2
**Prioridad:** 🟡 Media (reactivo a bugs de QA)
**Dependencia:** T-24-DB (seeds deben existir) | T-24-QA (bugs reportados por QA)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre
cat .cursorrules

# 2. Skill principal del agente Backend
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Auditoría de brechas (contexto)
cat .agentic-sync/T-24_UAT_Gap_Analysis.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> `// @Traceability: Estabilización J-02 (T-24)`. INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El Backend se encuentra **estable** bajo la arquitectura Hexagonal/Zero-Mock. Sin embargo, la certificación E2E de 57 CUs generará bugs que el QA reportará en `.agentic-sync/bug_report_qa_j02.md`. El rol del Backend en este ciclo es **exclusivamente reactivo**.

| Área de Riesgo | Endpoint | Detalle |
|----------------|:--------:|---------|
| Rate Limiting (CU-NEG-10) | `GET /api/v1/workdesk/tasks` | `WorkdeskRateLimitFilter.java` — verificar que retorna 429 tras 60 req/min |
| DTO Sanitización (CU-NEG-11) | `GET /api/v1/workdesk/tasks` | Validar que response NO contiene PII ni variables Camunda internas |
| Hard Limit (CU-NEG-08) | `GET /api/v1/workdesk/tasks?size=500` | Validar que retorna 400 si size > 100 |
| Force-Unclaim (CU-C06) | `POST /api/v1/tasks/{id}/force-unclaim` | Validar cruce `team_id` supervisor vs `team_id` tarea |
| Bulk Claim (CU-C03) | `POST /api/v1/workbox/tasks/bulk-claim` | Validar batch atómico con conflictos individuales |

---

## 🎯 Instrucciones Quirúrgicas

### Modo de Operación: Estabilización Pasiva

El Backend NO tiene tareas proactivas. Su flujo es:

1. **Esperar** a que QA reporte bugs en `.agentic-sync/bug_report_qa_j02.md`.
2. **Leer** el reporte del bug (archivo, línea, endpoint, error HTTP).
3. **Diagnosticar** la causa raíz en la capa de Application/Domain/Infrastructure.
4. **Corregir** el bug sin alterar contratos de API existentes.
5. **Compilar**: `mvn clean compile -f backend/ibpms-core/pom.xml`.
6. **Commit** con trazabilidad.

### Excepción: Endpoints Faltantes

Si QA reporta que un endpoint requerido por un CU **no existe**, Backend DEBE crearlo siguiendo la arquitectura Hexagonal:

```
Port (interface) → UseCase (application) → Adapter (infrastructure/web)
```

Ejemplo para `force-unclaim`:
```java
// @Traceability: Estabilización J-02 (T-24) — CU-J02-C06
@PostMapping("/api/v1/tasks/{taskId}/force-unclaim")
@PreAuthorize("hasRole('SUPERVISOR')")
public ResponseEntity<?> forceUnclaim(
    @PathVariable UUID taskId,
    @RequestBody ForceUnclaimDTO dto,
    @CurrentTenant String tenantId) {
    // Validar team_id supervisor == team_id tarea
}
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Todos los bugs reportados por QA en `bug_report_qa_j02.md` resueltos | Archivo sin items OPEN |
| 2 | `mvn clean compile` exitoso | Exit code 0, "BUILD SUCCESS" |
| 3 | Código nuevo con `// @Traceability: Estabilización J-02 (T-24)` | `grep -rL "@Traceability" [archivos_modificados]` → 0 |
| 4 | Commit en `sprint-6` | `git log -1 --oneline` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer `.cursorrules` + Skills de Sección 2.
2. Verificar infraestructura: `docker ps` (postgres, redis, rabbitmq healthy).
3. Esperar bugs de QA o ejecutar fixes conocidos.
4. Compilar: `mvn clean compile -f backend/ibpms-core/pom.xml`.
5. Commit: `git add . && git commit -m "fix(j02): [descripción del fix] // @Traceability: T-24" && git push origin sprint-6`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de [⚙️ BACKEND - JAVA].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat .agentic-sync/T-24_BE_Stabilization.md
6. cat .agentic-sync/bug_report_qa_j02.md (si existe)

TU MISIÓN:

1. Rol REACTIVO: corregir bugs reportados por QA en bug_report_qa_j02.md.
2. Si un endpoint faltante es reportado, crearlo siguiendo Hexagonal Architecture.
3. Compilar: mvn clean compile -f backend/ibpms-core/pom.xml
4. Commit: git add . && git commit -m "fix(j02): [fix]" && git push origin sprint-6

REGLAS INQUEBRANTABLES:
- NO crear endpoints que no sean requeridos por los 57 CUs del UAT.
- NO modificar contratos de API existentes (breaking changes).
- OBLIGATORIO documentar con // @Traceability: Estabilización J-02 (T-24).
- Compilación nativa OBLIGATORIA (LEY GLOBAL 2). PROHIBIDO docker exec.
```

---

> // @Traceability: Estabilización J-02 (T-24)
