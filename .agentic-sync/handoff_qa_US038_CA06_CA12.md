---
title: "Handoff QA - US-038 (CA-06 al CA-12)"
role: "QA"
epic: "US-038 - Asignación Multi-Rol y Sincronización EntraID"
iteration: "02-DEV-038-DAVID"
branch: "DevDavid"
---

# Handoff Arquitectónico: QA SDET

## 1. Contexto y Objetivos
El objetivo de esta iteración es validar empíricamente (E2E) las implementaciones de Segregación de Funciones (SoD), Delegación Jerárquica con mensajería de contingencia y las vistas de Anomalías de la US-038.

**Exclusiones:** El CA-09 ha sido EXCLUIDO y no requiere validación.

## 2. Alineación Arquitectónica y QA
* **ADR-010 (Testing Pyramid):** Debes validar que el flujo E2E cubre todas las capas. Se exige un entorno local levantado (Docker, Backend, Frontend).
* **Zero-Mock Policy:** Prohibido el uso de mocks en los tests de Playwright. Debes consumir los endpoints reales.

## 3. Requerimientos Técnicos (Entregables)

### 3.1 Suite E2E (Playwright)
Crear un archivo de test `us-038-iteration2-sod-delegation.spec.ts` que valide:
* **CA-06 (SoD):** Intentar aprobar una tarea en Camunda donde el aprobador sea igual al creador y validar que se bloquea en UI y que la anomalía aparece en el nuevo endpoint GET `/api/v1/security/anomalies`.
* **CA-07 y CA-08:** Validar el formulario de delegación de perfil (Fecha Inicio/Fin) y asegurarse que la solicitud hace POST exitosamente.
* **CA-10 y CA-11:** Validar visualmente la presencia de chips o badges en la UI correspondientes a los múltiples roles.
* **CA-12:** Validar el comportamiento del "Tablero de Anomalías", interactuando con el botón "Subsanar".

Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---
**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
