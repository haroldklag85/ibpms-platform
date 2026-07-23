# Handoff QA: Optimización de Concurrencia E2E (J-04)

**Objetivo:** Reducir la asfixia del entorno local modificando la concurrencia de la suite Playwright para dar espacio al backend, estabilizando los Timeouts restantes (BUG-S6-004).

**Instrucciones Arquitectónicas:**
1. **Reducción de Paralelismo:** Modificar `playwright.e2e.config.ts`. Establecer el límite de `workers` a 2 (antes 4) por mandato directivo, aliviando la carga de CPU y pool de BD.
2. **Action Timeout Tuning:** Aumentar `actionTimeout` y `navigationTimeout` a 15000ms.
3. **Ejecución de Certificación:** Tras validar la configuración, lanzar de nuevo la suite completa: `npx playwright test --project="Zero-Mock-E2E" --workers=2`. Todos los escenarios deben ejecutarse (sin `test.skip`).

**Alineación Arquitectónica:**
- Se alinea con los límites del entorno de infraestructura local sin sacrificar la validación lógica Zero-Mock de las pruebas (ADR-010 Testing Pyramid).
- Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` a la rama `sprint-6_uat_certification`. Queda estrictamente prohibido usar git stash.
