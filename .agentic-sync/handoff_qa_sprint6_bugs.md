# Handoff: Certificación E2E Zero-Mock (Sprint 6.2)

## 1. Contexto y Objetivos
**US / BUG:** BUG-S6-004 (Ejecución Forzada sin Skips de Suite J-04)
**Rama de Trabajo:** `sprint-6`
**Objetivo:** Certificar de forma definitiva los 55 escenarios del Journey J-04, validando que las optimizaciones de infraestructura, frontend y backend eliminaron los fallos sistémicos por Timeouts bajo el modo `Zero-Mock-E2E`.

## 2. Alineación Arquitectónica
- **ADR-010 (Testing Pyramid):** Las pruebas E2E son la última línea de defensa.
- Zero-Mock Policy: Las pruebas corren contra backend en vivo sin interceptores.

## 3. Requerimientos Funcionales y Técnicos
- **Desbloqueo Forzado:** Ejecutar las pruebas sin `test.skip()`.
- **Certificación Zod:** Validar que los botones de Pánico y recuperación de draft reaccionen visualmente en la suite Playwright y no provoquen timeouts ciegos.
- **SLA Workdesk:** Certificar que la carga de la bandeja de ejecución baje a <= 2s.

## 4. Tareas a Ejecutar
1. Validar que la compilación de Frontend y Backend haya finalizado y los contenedores estén levantados.
2. Ejecutar la suite masiva J-04 en Playwright (UI Mode / Headless).
3. Reportar los tiempos de ejecución y generar el Trace Log en caso de fallos remanentes.

## 5. Criterios de Aceptación
- [ ] 55 Escenarios en estado `PASS`.
- [ ] 0 Escenarios en estado `SKIP` o `FAIL`.
- [ ] Trazabilidad completada en `coverage_matrix.md` si aplica.

## 6. Instrucciones Operativas y de Compilación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

*Referencia obligatoria:* Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
