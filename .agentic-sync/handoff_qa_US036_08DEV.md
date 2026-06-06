---
title: "QA Execution Plan - US-036 (Identity Governance)"
agent: "QA/DevOps"
branch: "DevDavid"
us: "US-036"
status: "Partial Compliance - Iteration 08-DEV-DAVID"
---

# 1. Metadatos de la Delegación
*   **Rol Asignado:** QA / DevOps
*   **Rama de Trabajo:** `DevDavid`
*   **US Objetivo:** US-036 (Identity Governance)
*   **Criterios a Validar (Scope Actual):** CA-29, CA-30, CA-31, CA-32 (Implementados en Iteración 08-DEV-DAVID).
*   **Alineación Arquitectónica:** Cumplimiento de ADR-010 (Pirámide de Pruebas) y ADR-011 (CQRS). Cero tolerancia a mocks según el skill `qa_e2e_validation_audit`.

# 2. Contexto de Negocio
El equipo de desarrollo ha entregado la funcionalidad de topología dinámica de menús y gobernanza de caché híbrida (CA-29 a CA-32). Necesitamos garantizar empíricamente que el Frontend reacciona a los cambios en el menú, que el Backend realiza la evicción del caché correctamente y que la base de datos se mantiene consistente.

# 3. NFR / QA Strategy (Estrategia de Pruebas)
*   **Ejecución Empírica E2E:** Ejecutar la suite de Playwright apuntando al backend vivo (`ibpms-core` en Docker) en la rama `DevDavid`.
*   **Validación de Supervivencia:** Prohibido reportar éxito si los contenedores no están activos.
*   **Tests de Regresión (Backend):** Verificar que los endpoints `GET /api/v1/users/me/menu-layout` devuelvan arreglos sin duplicados.
*   **Caché Eviction:** Modificar un rol y verificar que el token/caché sea revocado causando un HTTP 403 y el consecuente `$reset()` en el Frontend (Toast de notificación).

# 4. Scenarios Gherkin a Validar
*   **CA-29:** Diseño Limpio del Modal de Roles (Tablas/Tabs). (Validar existencia de pestañas en `IdentityGovernance.vue`).
*   **CA-30:** Superposición Inclusiva Multirrol. (Asignar 2 roles a un usuario y verificar unión matemática).
*   **CA-31:** Arquitectura Endpoint Dinámico. (Asegurar que el Sidebar dependa de la API, no de hardcode).
*   **CA-32:** Caché Híbrida y Auto-Curación. (Alterar permiso de un usuario logueado en otra ventana; verificar auto-curación).

# 5. Entregables Esperados del Agente QA
1.  **Reporte de Ejecución:** `playwright-report/index.html` con screenshots de fallos si los hubiera.
2.  **Artefacto de Resultados:** `.agentic-sync/qa_report_US-036_08DEV.md` con el estado final (Pass/Fail) por cada CA.
3.  **Matriz de Cobertura QA:** Actualizar (si aplica) `docs/qa/QA - US-036/matriz_QA_US-036.md`.

# 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_QA.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_QA.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> ⚠️ Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin. Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
