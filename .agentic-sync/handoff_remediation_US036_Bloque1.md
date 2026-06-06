# Handoff de Remediación Integrada: US-036 (RBAC & Identity Governance) - Bloque 1

**Fecha/Hora:** 2026-04-18
**Contexto:** Auditoría Técnica y Forense de Sprint 4 (Seguridad) finalizada por el Agente Arquitecto Líder.
**Alcance:** Remediación de las brechas de seguridad (GAPs) documentadas durante la fase de análisis de los Criterios de Aceptación CA-1 a CA-5 de la Historia de Usuario US-036.

---

## 🛑 Hallazgos y GAPs Detectados (Auditoría Forense)

Tras evaluar exhaustivamente el código del repositorio comparándolo con las especificaciones Gherkin (`epic_E_seguridad_identidad_config.md`), se documentan las siguientes desviaciones (Notando que el CA-4 "Segregación Iniciador vs Ejecutor" y CA-5 "Workdesk RLS" han pasado el proceso de certificación en verde):

1. **GAP Menor (CA-1 - Hibridación):** El frontend (`RbacManagerView.vue`) expone funcionalidad gráfica híbrida y el DDL de base de datos soporta metadatos. Sin embargo, el objeto unificado de entidad central `RoleEntity.java` carece de las propiedades estructurales como `is_template` y `source` (EntraID / Local).
2. **GAP Crítico (CA-2 - Inmutabilidad del Guardián):** La clase `UserService.java` protege al usuario root, pero la interfaz de administración de perfiles `RoleService.java` (`deleteRole` y `updateRole`) **permite alterar de manera destructiva el mismísimo identificador `ROLE_SUPER_ADMIN`**, lo cual rompe el concepto de Inmutabilidad Absoluta y expone una vulnerabilidad severa de escalada.
3. **GAP Funcional (CA-3 - Clonación y Asignación Masiva):** No existe el `Endpoint` habilitador para inyectar una "Clonación / Mass Assignment". Faltan tanto los constructos JPA/Servicios en backend (e.g. `assignTemplateToUsers`) dentro de `RoleAdminController.java`, como el flujo Axios desde la vista en Vue hacia dicha API.

---

## 🛠️ Cuadrilla de Desarrollo: Directivas de Remediación

Se delegan las siguientes tareas tácticas para su cumplimiento bajo tolerancia cero a fallos:

### Para el Agente Backend (Experto Spring Boot & Security)
1. **Hardcoding del Blindaje en `RoleService.java` (CA-2):**
   Interviene los métodos `deleteRole(UUID id)` y `updateRole(RoleEntity role)`.
   Si el registro en cuestión es `ROLE_SUPER_ADMIN`, debes abortar la transacción lanzando una `AccessDeniedException`: *"Mutación/Borrado de Rol Root prohibido por diseño de seguridad."*
2. **Endpoint de Asignación Masiva (CA-3):**
   * Agrega el payload en `RoleAdminController.java`: `@PostMapping("/{templateId}/assign-massively")`.
   * El RequestBody recibirá un `List<UUID> userIds`.
   * En `RoleService.java`, itera sobre las identidades y agrégales el perfil seleccionado en una única transacción `@Transactional`.
3. **Mapeo del Modelo Híbrido (CA-1):**
   * Incorpora `@Column(name = "is_template", nullable = false) private Boolean isTemplate = false;` y `@Column(name = "source", length = 50) private String source = "LOCAL";` en la entidad principal `com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity.java`.

### Para el Agente Frontend (Experto Vue3 & Tailwind)
1. **Bloqueo Defensivo Visual (CA-2):** En `GlobalRolesTable.vue` o `ProcessRolesTable.vue`, intercepta el renderizado condicional de los mandos de interactividad (Borrar/Editar). Evalúa el nodo DOM con un `v-if="role.name !== 'ROLE_SUPER_ADMIN'"` para ocultarlo.
2. **Mass Assignment UI (CA-3):** Acopla en alguna de las pestañas un botón contextual de acción "Asignación Masiva" que dispara la API POST `/api/v1/admin/roles/{id}/assign-massively`. Pasa un Payload duro de Mock como demostrador si es necesario (ej: `[ '0000-0000...' ]`).

### Para el Agente QA (Experto E2E y TDD)
1. **Aserciones Restrictivas Backend:** Añade el Unit Test pertinente para `testPreventSuperAdminRoleDeletion()` donde el Exception thrown se garantiza y atiende de manera correcta.
2. **Defensa de Interfaz E2E (Vitest):** Construye la prueba atómica de Vitest montando el componente `GlobalRolesTable.vue`, emulando un perfil Root en los *Props*, y probando la imposibilidad de encontrar el selector CSS del botón de eliminar.

---

**Protocolo de Conformidad:** Se espera que la cuadrilla reporte el estado "All-Green". Quedo a expensas de vuestro aviso.
