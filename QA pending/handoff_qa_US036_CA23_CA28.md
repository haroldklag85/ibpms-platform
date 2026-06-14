# Handoff QA (Diferido): US-036 (CA-23 al CA-28)

## 1. Metadatos
- **Iteración:** 07-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-23, CA-24, CA-25, CA-26, CA-27, CA-28
- **Rama Git:** DevDavid
- **Estrategia NFR/QA:** desarrollar sobre la arquitectura en la ruta "C:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\docs\architecture\arquitecturar.md"

## 2. Contexto de Pruebas
Esta iteración implementó la resolución de delegaciones in-flight, la protección contra edición de roles de sistema (SUPER_ADMIN) y la generación de reportes ISO 27001 on-demand, junto con mecanismos de UX Fallback para prevenir FOUC y bloqueos por falta de permisos.

## 3. Criterios a Validar en Futura Sesión QA
- **E2E - Delegación In-Flight (CA-23):** Crear un usuario, asignarle tareas, delegar rol a un suplente. Verificar que el suplente vea las tareas en su Workdesk. Expirar la delegación y validar que el suplente pierda acceso y el original lo recupere.
- **E2E - Reporte ISO 27001 (CA-24):** Iniciar sesión como Súper Admin, hacer clic en `[Generar Reporte Matrizal]`. Validar descarga de CSV/Excel y corroborar registro + Hash SHA-256 en base de datos `ibpms_audit_reports`.
- **E2E - UX Fallback (CA-26):** Iniciar sesión con usuario cuyo rol tiene CERO permisos de menú. Validar que es redirigido a una página de Bienvenida sin errores HTTP ni menús fantasma.
- **E2E - Roles Nativos Inmutables (CA-27):** Ir a Pantalla 14, intentar editar los permisos de `SUPER_ADMIN`. Validar que la interfaz está bloqueada y si se intercepta la llamada API con POSTman, el Backend retorna HTTP 403.
- **E2E - Módulos Macro (CA-28):** Validar que la interfaz expone sólo los 7 Módulos Macro (Workdesk, Service Delivery, BAM, Modeler, Integración, Proyectos, Administración).

## 4. Instrucción Obligatoria (Diferida)
Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
