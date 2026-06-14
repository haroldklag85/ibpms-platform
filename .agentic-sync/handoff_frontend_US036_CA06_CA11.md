# Handoff Técnico: Frontend — US-036 (Identity Governance) - Fase 2

## 1. Contexto de la Tarea
- **Iteración:** 04-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-06, CA-07, CA-08, CA-09, CA-10, CA-11
- **Rama:** `DevDavid`
- **SSOT:** [epic_E_seguridad_identidad_config.md](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/docs/requirements/epics/epic_E_seguridad_identidad_config.md)

## 2. Alineación Arquitectónica (ADR Compliance)
- **ADR-002 (Vue 3 Patterns):** Uso de `IdentityGovernance.vue` como orquestador de pestañas (Tabs).
- **Zero-Trust UI:** Ofuscación de API Keys y labels de protección para usuarios inactivos.

## 3. Requerimientos Técnicos por Criterio (Frontend)

### CA-07: Soft-Delete (Visual)
- En la tabla de gestión de usuarios, los usuarios con estado `INACTIVE` deben mostrar el sello `[Usuario Inactivo]` en color gris/neutral.
- Deshabilitar el botón de "Eliminar" (ya que ahora es un toggle de estado) y el de "Editar" para usuarios inactivos.

### CA-09: Panel de Delegación Autónoma
- Crear una nueva pestaña "Delegación" en la Pantalla 14.
- Interfaz para que el usuario actual pueda seleccionar un suplente de una lista desplegable.
- Campos obligatorios: `Fecha Inicio` y `Fecha Fin` (DatePickers).
- Botón "Activar Delegación".

### CA-10: Cuentas de Servicio (M2M)
- Crear una nueva pestaña "Cuentas de Servicio" en la Pantalla 14.
- Grilla que liste: `Nombre de la Cuenta`, `Rol Asignado`, `Fecha de Expiración`.
- Modal "Nueva Cuenta":
  - Formulario: Nombre, Selección de Rol, Fecha de Expiración.
  - Al guardar, mostrar un Modal de éxito con la **API Key generada** (valor único visible solo una vez) y un botón "Copiar al portapapeles".

### CA-11: No MFA Propio
- Verificar que en el flujo de Login local y SSO no existan pantallas de OTP/MFA. La arquitectura asume confianza en el IDP.

## 4. NFR/QA Strategy
- Desarrollar sobre la arquitectura en `docs/architecture/arquitecturar.md`.
- URL de trabajo principal: `/admin/security/identity`.
- Usuario de prueba: `root@ibpms.local` / `Root#Temp4Sys`.

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado in `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

**📚 SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
- **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
