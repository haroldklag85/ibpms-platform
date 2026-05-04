# Handoff Frontend - US-036 Identity Governance (CA-01 a CA-06)

## 1. Metadatos y SSOT
- **Iteración:** 03-DEV-DAVID
- **Rama:** `DevDavid`
- **US:** US-036 (RBAC & Identity Governance)
- **CAs:** CA-01, CA-02, CA-03, CA-04, CA-05, CA-06
- **SSOT:** [epic_E_seguridad_identidad_config.md](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/docs/requirements/epics/epic_E_seguridad_identidad_config.md)

## 2. Alineación Arquitectónica
- **ADR-002 (Vue 3 Patterns):** Uso de Pinia (`useRbacStore`) y `apiClient`.
- **Zero-Trust UI:** El rol "🔒 ROOT" debe estar visualmente protegido (botones deshabilitados o v-if).

## 3. Rutas Exactas y Contexto
- **Vista Global Roles:** [GlobalRolesTable.vue](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/RbacManager/GlobalRolesTable.vue)
- **Vista Proceso Roles:** [ProcessRolesTable.vue](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/RbacManager/ProcessRolesTable.vue)
- **Store:** [rbacStore.js](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/stores/rbacStore.js)

## 4. Snippets Prescriptivos

### CA-01: Importar desde EntraID
Agrega un botón "Importar EntraID" en `GlobalRolesTable.vue`. Al hacer clic, abre un modal que liste los grupos obtenidos de `GET /api/v1/admin/roles/entraid-groups`.

```vue
<!-- Ejemplo de Modal de Importación -->
<div v-if="importModalOpen" class="modal">
  <h3>Sincronizar Grupos EntraID</h3>
  <div v-for="group in entraIdGroups" :key="group.id">
     <span>{{ group.displayName }}</span>
     <button @click="importGroup(group)">Importar</button>
  </div>
</div>
```

### CA-04: Matriz Iniciador vs Ejecutor
En `ProcessRolesTable.vue`, reemplaza la visualización simple por una tabla que permita marcar:
- `[ ] Puede Iniciar Proceso`
- `[ ] Puede Ejecutar Tareas`

### CA-06: Herencia Piramidal
El selector `select-parent-role` ya existe en el modal de creación. Asegúrate de que al editar un rol también se pueda cambiar el padre, y que la UI refleje si un permiso es heredado o propio.

## 5. Matriz de QA (Frontend)
| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| `renderRootLock` | CA-02 | Botón borrar no existe para ROLE_SUPER_ADMIN |
| `toggleProcessPermissions` | CA-04 | El checkbox dispara PATCH a `/admin/roles/{id}/permissions` |

## 6. Mensaje de Despacho
"Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."
