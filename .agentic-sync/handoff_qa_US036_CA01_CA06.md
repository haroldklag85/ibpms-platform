# Handoff QA - US-036 Identity Governance (CA-01 a CA-06)

## 1. Metadatos y SSOT
- **Iteración:** 03-DEV-DAVID
- **Rama:** `DevDavid`
- **US:** US-036 (RBAC & Identity Governance)
- **CAs:** CA-01 a CA-06
- **SSOT:** [epic_E_seguridad_identidad_config.md](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/docs/requirements/epics/epic_E_seguridad_identidad_config.md)

## 2. Alineación Arquitectónica
- **ADR-010 (Testing Pyramid):** Foco en Pruebas E2E reales (Zero-Mock) contra la base de datos Dockerizada.
- **Protocolo QA:** Seguir `qa_e2e_validation_audit/SKILL.md`.

## 3. Rutas Exactas y Contexto
- **Archivo de Test:** `frontend/e2e/us-036-rbac-core.spec.ts` [NUEVO]

## 4. Escenarios de Prueba Obligatorios

### Escenario 1: Importación EntraID (CA-01)
1. Navegar a Pantalla 14 (RbacManager).
2. Abrir Modal de Importación.
3. Seleccionar grupo "GG_IBPMS_Admins".
4. Verificar que el rol se crea con `source: ENTRA_ID`.

### Escenario 2: Blindaje ROOT (CA-02)
1. Intentar borrar el rol `ROLE_SUPER_ADMIN` vía API (directamente) y vía UI.
2. La UI no debe mostrar el botón.
3. La API debe responder `403 Forbidden` o `AccessDenied`.

### Escenario 3: Herencia Piramidal (CA-06)
1. Crear `Rol_Padre` con permiso `Proceso_A:Iniciar`.
2. Crear `Rol_Hijo` heredando de `Rol_Padre`.
3. Asignar `Rol_Hijo` a `Usuario_Prueba`.
4. Verificar que `Usuario_Prueba` puede iniciar `Proceso_A`.

## 5. Matriz de QA (E2E)
| Test Case | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| `importEntraIdGroup` | CA-01 | Role list length increases by 1 |
| `preventRootDeletion` | CA-02 | DOM element `btn-delete-role` for ID 1 is null |
| `validateRLS` | CA-05 | Search for User B task with User A session returns 0 results |

## 6. Mensaje de Despacho
"Ejecución empírica obligatoria: Sigue el protocolo `qa_e2e_validation_audit/SKILL.md`. No se aceptan reportes de éxito sin capturas de pantalla de Playwright y logs de base de datos que confirmen la persistencia real."
