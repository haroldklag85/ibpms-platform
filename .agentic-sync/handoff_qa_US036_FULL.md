---
title: "Handoff QA - Certificación Total US-036 (Identity Governance)"
agent: "QA/DevOps"
branch: "DevDavid"
us: "US-036"
cas: "CA-1 al CA-32"
---

# 1. Metadatos de la Delegación
*   **Rol Asignado:** QA / DevOps
*   **Rama de Trabajo:** `DevDavid`
*   **US Objetivo:** US-036 (Identity Governance)
*   **Alineación Arquitectónica:** Cumplimiento de ADR-010 (Pirámide de Pruebas). Cero tolerancia a mocks según el skill `qa_e2e_validation_audit`.

# 2. Contexto de Negocio
El ecosistema de desarrollo ha finalizado la construcción del 100% de la lógica de negocio para la US-036 (32 Criterios de Aceptación). Esto incluye la creación de roles, asignaciones JIT (EntraID), caché híbrida, delegación, segregación multirrol y generación del reporte ISO 27001.

# 3. NFR / QA Strategy (Estrategia de Pruebas Obligatoria)
*   **Ejecución Empírica:** Tienes ESTRICTAMENTE PROHIBIDO reportar éxito sin pruebas empíricas. Debes lanzar el contenedor de Docker del backend (`ibpms-core`) y el Frontend local (Vite) para ejecutar pruebas reales.
*   **Pirámide Completa:** Asegura la correcta ejecución de unit tests en Frontend (Vitest) y Backend (JUnit), finalizando con la suite de Playwright E2E.
*   **Zero-Mock Enforcement:** El login y la extracción de datos debe apuntar a PostgreSQL y Redis locales reales.

# 4. Scenarios Gherkin Críticos a Validar (E2E)
Debes verificar prioritariamente los siguientes flujos funcionales:
1.  **Kill-Session & Cache (CA-14, CA-32):** El Super Admin revoca permisos de un usuario, lo que debe desencadenar un HTTP 403 y purgar el `menuStore` en el Frontend del usuario afectado (Zero-Trust).
2.  **Topología Multirrol (CA-30, CA-31):** Un usuario con 2 roles distintos visualiza la unión matemática de los 7 Módulos Macro en su Sidebar sin errores 404 ni FOUC.
3.  **Auditoría ISO 27001 (CA-16, CA-24):** Generación del reporte CSV desde la UI, validando la descarga correcta (Blob) y el guardado en la tabla `ibpms_audit_reports`.
4.  **Inmutabilidad Nativos (CA-27):** Comprobar que en el modal de edición, el rol `SUPER_ADMIN` tiene deshabilitada (Read-Only) la matriz de macros.

# 5. Entregables Esperados del Agente QA
1.  **Reporte Playwright:** Generar `playwright-report/index.html` con las evidencias de validación (Screenshots de posibles fallos).
2.  **Acta de Certificación QA:** Crear un archivo `.agentic-sync/qa_report_US-036_FINAL.md` documentando la tasa de éxito y detallando cualquier defecto (Bug).
3.  **Matriz de QA:** Actualizar la matriz oficial en `docs/qa/QA - US-036/matriz_QA_US-036.md` colocando el estado final de los tests.

# 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_QA.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_QA.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> ⚠️ Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
