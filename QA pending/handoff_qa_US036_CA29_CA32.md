# 📄 Handoff de Arquitectura: QA / Validación E2E
> **US:** US-036 | **CAs:** CA-29 al CA-32 | **Iteración:** 08-DEV-DAVID
> *Nota: Este handoff se almacena en `QA pending` porque la iteración actual está configurada como `necesita_qa: No`.*

## 1. Metadatos de la Delegación
- **Rol Destino:** Agente QA
- **Objetivo:** Verificar la integridad de la interfaz, el cálculo del menú y la purga de caché.
- **Alineación Arquitectónica:**
  - Garantizar cobertura según ADR-010 (Pirámide de Pruebas).

## 2. Contexto de Negocio
Se ha implementado el diseño limpio del Modal de roles, la lógica matemática de unión de menús por el backend, la carga del menú dinámicamente y la auto-curación de caché en el frontend para evitar mostrar menús a usuarios revocados.

## 3. Criterios de Aceptación a Validar
- **CA-29:** Verificar que el Modal de Rol tenga "Tab 1: Información Básica" y "Tab 2: Topología de Menús".
- **CA-30:** Verificar que un usuario con Rol A y Rol B visualice el superset de módulos sin duplicados.
- **CA-31:** Verificar en red que el menú se alimenta exclusivamente del endpoint `GET /api/v1/users/me/menu-layout`.
- **CA-32:** Verificar que un 403 limpia Pinia y dispara el Toast de actualización.

## 4. Estrategia NFR/QA
- **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta `C:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\docs\architecture\arquitecturar.md`.
- Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

## 5. Instrucciones Operativas
- Las pruebas de integración se ejecutarán mediante Playwright para Frontend, y JUnit/REST Assured para Backend.
- Cuando el Arquitecto ordene activar QA, ejecutar los flujos y documentar en `matriz_QA_US-036.md`.
