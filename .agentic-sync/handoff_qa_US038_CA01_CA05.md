# Handoff Arquitectónico: QA
**Iteración:** 01-DEV-038-DAVID
**Épica:** 13 — Seguridad/RBAC (US-038)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-038 exige pruebas sobre la sincronización y validación de identidades. QA deberá certificar que los flujos de "Edge Cases" o Casos Borde de ciberseguridad operen sin fallas: Tolerancia a fallos de Redis (Fail-Open), rechazo de roles basura (Anti-Token Bloat), bloqueo por perfil incompleto JIT (HTTP 428) y contingencia Break-Glass.

## 2. Alineación Arquitectónica y Criterios a Validar

**Criterios de Aceptación y Scenarios Gherkin de referencia:**
*   **CA-01: Fail-Open Policy**
    *   *Gherkin:* Simular caída de Redis y confirmar que el Frontend puede hacer `GET` (Lee datos) pero se le rechaza en `POST/PUT/DELETE`.
*   **CA-02: Anti-Token Bloat**
    *   *Gherkin:* Verificar que el JWT generado (o Claims en sesión) contiene solo roles con prefijo `ibpms_rol_`.
*   **CA-03: JIT Provisioning (Guardrail 428)**
    *   *Gherkin:* Emular un perfil sin `Sucursal_ID` y confirmar que el Frontend muestra el Modal de "Completar Perfil Local" y que no se accede al Workdesk hasta llenarlo.
*   **CA-04: Break-Glass Protocol**
    *   *Gherkin:* Validar el endpoint/UI de emergencia.
*   **CA-05: Aditividad RBAC**
    *   *Gherkin:* Verificar que un usuario con Rol A (Lectura) y Rol B (Escritura) hereda ambos sin conflictos lógicos negativos.

## 3. Estrategia NFR/QA
> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta `docs\architecture\arquitecturar.md`.

- Actualizar o reemplazar el archivo `us038-multi-rol-entraid.spec.ts` en la suite de E2E (Playwright).
- Se prohíbe el uso de Mocks; las pruebas deben realizarse contra el backend levantado interactuando con la DB/Redis real.

## 4. Instrucciones Operativas
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
