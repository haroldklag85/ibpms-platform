# 🧠→🕵️ Handoff: Arquitecto Líder → QA E2E
# Certificación T-04/T-05/T-06: Arquitectura Hexagonal ADR-001 (US-001)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🕵️ QA - E2E]
**Fecha:** 2026-05-11T22:07:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🟡 MEDIA
**Dependencia:** T-04/T-05 Backend + T-06 Frontend deben estar completados

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Tu skill de QA E2E
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Zero-Mock enforcement
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADR-010 (Testing Pyramid Governance)
cat docs/architecture/adr_010_testing_pyramid_governance.md

# 5. ADR-001 (Hexagonal Architecture — para entender qué validar)
cat docs/architecture/adr-001-hexagonal-architecture.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO spec de test DEBE incluir `// @Traceability: US-001, CA-XX` al inicio del bloque `describe()`. Esto es INNEGOCIABLE.

---

## 🎯 Misión de Certificación

Validar que las correcciones de Arquitectura Hexagonal (T-04, T-05, T-06) funcionan end-to-end sin romper funcionalidad existente del Workdesk (US-001).

### Tests a crear/ejecutar

**Archivo:** `frontend/e2e/certification/us001-hexagonal-compliance.e2e.spec.ts`

#### Escenarios mínimos obligatorios:

| # | Escenario | Qué valida | US/CA |
|---|-----------|------------|-------|
| 1 | POST `/api/v1/workdesk/attend-next` → 200 OK con tarea asignada | T-04: refactor no rompió el endpoint | CA-28 |
| 2 | POST `/api/v1/workdesk/attend-next/skip` → 200 OK con siguiente tarea | T-04: skip sigue funcionando post-refactor | CA-21 |
| 3 | GET `/api/v1/workdesk/feature-toggles/FORCE_ROUTING` → 200 con `{enabled: true/false}` | T-05: consulta de toggle funcional | CA-08 |
| 4 | PUT `/api/v1/workdesk/feature-toggles/FORCE_ROUTING` con ROLE_SUPER_ADMIN → 200 | T-05: actualización de toggle con audit | CA-08/CA-16 |
| 5 | PUT `/api/v1/workdesk/feature-toggles/FORCE_ROUTING` sin ROLE_SUPER_ADMIN → 403 | T-05: RBAC enforcement | CA-08 |
| 6 | UI Workdesk: dropdown de delegantes muestra opciones reales (no vacío) | T-06: campo fantasma reemplazado | CA-04 |
| 7 | Seleccionar delegante en UI → cambia contexto de bandeja | T-06: funcionalidad completa | CA-04 |

### Reglas de ejecución

1. **PROHIBIDO** usar `route.fulfill()` o cualquier mock de red.
2. **OBLIGATORIO** ejecutar contra backend nativo + infraestructura Docker real.
3. **OBLIGATORIO** documentar `@Traceability` en cada `test()` block.

### Secuencia de ejecución

```bash
# 1. Verificar backend arrancado
curl http://localhost:8080/actuator/health

# 2. Ejecutar specs
cd frontend
npx playwright test certification/us001-hexagonal-compliance.e2e.spec.ts --reporter=list

# 3. Reportar resultados
```

### Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | 7/7 tests PASS | Log Playwright |
| 2 | 0 usos de `route.fulfill()` en el spec | Grep del archivo |
| 3 | `@Traceability` en cada test | Inspección de código |
| 4 | Commit en rama de sprint | `git log -1` |

---

**RECUERDA:** Si el backend NO arranca, aplica el protocolo §5 de tu SKILL: 2 intentos máximo, luego reportar bloqueante al Arquitecto.
