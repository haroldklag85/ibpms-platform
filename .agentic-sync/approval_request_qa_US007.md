# 🛡️ Solicitud de Aprobación de Plan QA - US-007 Bloque 3

**De:** Agente QA (Antigravity)
**Para:** Arquitecto Líder
**Rama:** `sprint-6`

## 📋 Resumen del Plan de Trabajo (PLANNING MODE)

He recibido el handoff `.agentic-sync/handoff_qa_US007_B3.md` y he elaborado mi plan de implementación, adhiriéndome a las políticas `Zero-Mock`, `TDD-First` y las normativas de `Clean Code`. 

Mi plan de ejecución consta de **7 nuevos archivos `.spec.ts` de Playwright** para abordar los GAPs detectados y **9 escenarios de validación cruzada** adicionales para probar las remediaciones B1 (Backend) y B2 (Frontend):

1. **`e2e/us007-rate-limiting.spec.ts`** -> Valida GAP-01 y CA-20 (Caché Hit insensible a mayúsculas).
2. **`e2e/us007-gc-drafts.spec.ts`** -> Valida GAP-02 y el borrado de drafts (Backend Testcontainers).
3. **`e2e/us007-hit-policy-catchall.spec.ts`** -> Valida GAP-05 y CA-22 (Rechazo a hitPolicy UNIQUE).
4. **`e2e/us007-simulator-xai.spec.ts`** -> Valida GAP-09 y CA-23 (Rate Limit de simulador).
5. **`e2e/us007-draft-persistence.spec.ts`** -> Valida GAP-11 (Persistencia LocalStorage/PostgreSQL).
6. **`e2e/us007-evaluate-test.spec.ts`** -> Valida GAP-13 (Restricción de estado en evaluación).
7. **`e2e/us007-dmn-catalog.spec.ts`** -> Valida GAP-15 y CA-24 (Buscador Server-Side y metadatos).
8. **Cobertura Transversal:** Inyección de validación PII (CA-05), rechazo de dot-notation (CA-08), hard-stop de LLM a 50 filas (CA-09), error pre-flight de BPMN (CA-14) y validación de edición manual (CA-32).

Todos los flujos serán testeados con el entorno `docker-compose.e2e.yml` en vivo para garantizar el compliance `Zero-Mock`. Si un componente no funciona, se documentará con evidencias visuales (`.png`, `.webm`) y fallará el pipeline empíricamente.

---

> **A la espera del `GO` formal por parte del Arquitecto Líder para pasar a modo `EXECUTION`.**
