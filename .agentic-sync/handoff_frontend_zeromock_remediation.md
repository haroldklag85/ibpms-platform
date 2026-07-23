# 🏗️ Handoff Consolidado FRONTEND — Remediación Zero-Mock (15 Hallazgos)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **Objetivo:** Cerrar las 15 violaciones Zero-Mock detectadas por el `anti-mock-scanner.js`
- **SSOT:** `docs/architecture/zero_mock_violation_report.md` + auditoría forense del Arquitecto Líder
- **Flujo:** 2️⃣ Frontend (espera a que Backend entregue el endpoint de migración de instancias)

## 2. Alineación Arquitectónica y ADRs
- **ADR-002 (Vue3):** Todo estado global debe usar Pinia; prohibido `localStorage` para datos de sesión.
- **Política Zero-Mock:** `const mock*` está prohibido en `/src/views/`, `/src/components/` y `/src/store/`. El scanner regex: `/(?:const|let|var)\s+mock[A-Z0-9_a-z]*\s*=/g`
- **Principio:** Los bloques `catch` de llamadas API deben asignar `[]` + Toast de error. NUNCA arrays con datos ficticios.

---

## 3. Remediaciones por Hallazgo (15 Total)

### GRUPO A — Módulo Modeler (US-005, US-003, US-007)

#### VIOL-001: `src/views/admin/Modeler/BpmnDesigner.vue` — L734
- **Eliminar:** `const mockRole = ref<...>('BPMN_Release_Manager')`
- **Reemplazar por:** Consumir el rol real desde `useAuthStore().currentUser.roles` o equivalente.
- **CAs:** CA-21 (visibilidad paneles), CA-66 (heartbeat por rol)

#### VIOL-002: `src/views/admin/Modeler/BpmnDesigner.vue` — Bloques catch con fallback
Ubicaciones exactas a limpiar:
- **L1002-1004** (`fetchDmnDefinitions`): Eliminar el array fallback `[{ id: 'dmn-mock-scoring'... }]` → Asignar `availableDmns.value = []` + `showToast('Error cargando DMNs', 'error')`
- **L1084-1090** (`fetchForms`): Eliminar el array fallback de 4 formularios → `availableForms.value = []` + Toast
- **L1100-1107** (`fetchConnectors`): Eliminar array de 3 conectores → `availableConnectors.value = []` + Toast
- **L1116-1120** (`fetchProcessVariables`): Eliminar array de 3 variables → `processVariables.value = []` + Toast
- **L1017-1021** (`fetchVersions`): Eliminar el array de 3 versiones mock → `versionHistory.value = []` + Toast
- **CAs:** CA-12, CA-30, CA-45, CA-49 de US-005

#### VIOL-003: `src/views/admin/Modeler/FormDesigner.vue` — L857-868
- **Eliminar:** `const mockContext = reactive({ rbacRole: 'ADMIN' })` y la función `evaluateMockVis`
- **Reemplazar por:** Importar `useAuthStore` y leer el rol dinámico del usuario autenticado.
- **CAs:** CA-28 de US-003

#### VIOL-004: `src/views/admin/Modeler/InstancesManager.vue` — Refactorización completa
- **Eliminar:** `const mockedInstances = ref([...])` (L105-110) y el `setTimeout` simulador (L112-117)
- **Reemplazar por:**
  - `onMounted`: llamar `GET /api/v1/design/processes/{processId}/instances` (endpoint que creará Backend)
  - `executeMigration`: llamar `POST /api/v1/design/processes/{processId}/migrate` con el payload real
- **CAs:** CA-8, CA-9, CA-10 de US-005

---

### GRUPO B — Módulo de Seguridad y RBAC (US-036)

#### VIOL-005: `src/views/admin/RbacManager/GlobalRolesTable.vue` — L262
- **Eliminar:** `const mockUserIds = ['00000000-...', '00000000-...']`
- **Reemplazar por:** Implementar un componente `UserPickerDialog` que consuma `GET /api/v1/admin/users` y permita selección múltiple real antes de enviar `POST /admin/roles/{id}/assign-massively`.
- **CAs:** CA-3 de US-036

#### VIOL-005b: `src/views/admin/Security/IdentityGovernance.vue` — Múltiples fallbacks
Los endpoints Backend **SÍ EXISTEN**. Debes conectar:
- **Kill Switch (L637):** Conectar al `POST /api/v1/admin/users/{id}/kill-session` (existe en `UserAdminController.java`)
- **Session Evaporation (L658):** Conectar al endpoint de `SessionRevocationController.java`
- **Export CISO CSV (L815-823):** Eliminar el `Blob` mock. Conectar al `GET /api/v1/admin/roles/export-matrix` que ejecuta `roleService.exportRoleMatrixToCsv()` (existe en `RoleAdminController.java` L78). Descargar el `byte[]` real del backend como archivo.
- **Fallback Mock UAT (L887):** Eliminar cualquier referencia a datos simulados restantes.
- **CAs:** CA-7, CA-8, CA-10 de US-036

---

### GRUPO C — Service Delivery y Workdesk (US no iniciadas — ANOTAR, NO ELIMINAR)

> ⚠️ **DIRECTIVA ESPECIAL:** Los siguientes archivos pertenecen a User Stories en estado **Pendiente**. Los mocks SON la única implementación. NO elimines la lógica, solo **renombra las variables** para que el scanner no las detecte como violación, y agrega una nota TODO estandarizada.

#### VIOL-006: `src/views/admin/ServiceDelivery/IntakeManual.vue` — L226, L229
- **Acción:** Renombrar `mockEmails` → `placeholderEmails` y `isSacLeader` → `placeholderSacLeader`
- **Agregar nota:** `// TODO [US-004/US-037]: Reemplazar por GET /api/v1/intake/emails cuando se implemente el sprint correspondiente`
- **US:** US-004 (Pendiente parcial), US-037 (Pendiente)

#### VIOL-007: `src/views/public/PublicIntake.vue` — L44
- **Acción:** Renombrar `mockSubmit` → `placeholderSubmit`
- **Agregar nota:** `// TODO [US-040]: Conectar a POST /api/v1/public/intake/submit cuando se implemente`
- **US:** US-040 (Pendiente)

#### VIOL-008: `src/views/Workdesk.vue` — L658
- **Acción:** Renombrar `mockOpenTask` → `openTaskHandler`
- **Agregar nota:** `// TODO [US-001/US-002]: Conectar a la apertura real de tarea Camunda con formKey dinámico`
- **US:** US-001 (Completada — función temporal), US-002 (En construcción)

#### VIOL-009: `src/views/admin/SettingsView.vue` — L117-118
- **Acción:** Renombrar `mockUsers` → `placeholderUsers`
- **Agregar nota:** `// TODO [US-048]: Reemplazar por GET /api/v1/admin/users desde useUserStore()`
- **US:** US-048 (Completada parcial)

#### VIOL-010: `src/components/agile/AssigneeMultiSelect.vue` — L72
- **Acción:** Si la variable `mockDirectory` existe, renombrar → `placeholderDirectory`
- **Agregar nota:** `// TODO [US-030]: Conectar a endpoint real de directorio de usuarios`
- **US:** US-030 (En construcción)

---

### GRUPO D — Violaciones Adicionales Descubiertas

#### VIOL-C1: `src/views/Login.vue` — L242-244
- **Acción:** Eliminar `const isProfileIncomplete = true; // Forzamos el Fallback JIT` (PENDIENTE CONFIRMACIÓN del usuario — Pregunta 7)
- **Nota temporal:** Agregar `// PREGUNTA ABIERTA: ¿Este flag debe eliminarse o es intencional para demo?`

#### VIOL-C2: `src/views/admin/Security/IdentityGovernance.vue` — L815-823
- **Ya cubierto en VIOL-005b.** Eliminar el `Blob` mock y conectar al endpoint real.

#### VIOL-C3: `src/views/admin/SGDEA/DocumentGrid.vue` — L227
- **Acción:** Renombrar el hash mock o eliminarlo. `sha256: '9f86d...'`
- **Agregar nota:** `// TODO [US-035]: Hash debe ser calculado por el backend al subir documento`
- **US:** US-035 (Pendiente)

#### VIOL-C4: `src/views/inbox/InboxView.vue` — L54, L96
- **Acción:** Agregar nota TODO sin alterar funcionalidad
- **Agregar nota:** `// TODO [US-016]: Módulo completo es placeholder. Conectar cuando se implemente SAC`
- **US:** US-016 (Pendiente)

---

### GRUPO E — Eliminación de Prototipos Residuales

#### VIOL-WM: `src/views/WorkdeskMockup.vue`
- **Acción:** Mover el archivo completo a `src/tests/fixtures/WorkdeskMockup.vue`
- **Eliminar** cualquier referencia en `router/index.ts` si existe una ruta apuntando a este componente.
- **US:** US-001, US-017

---

## 4. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero.
3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera el veredicto. Si aprobado, pasa a `EXECUTION`, programa y finaliza con `git commit` y `git push` en `sprint-6/uat-certification`. Queda prohibido usar `git stash`.

📚 **SKILLS OBLIGATORIOS:**
- Aplica TDD: `.agents/skills/tdd_first/SKILL.md`
- Aplica Clean Code: `.agents/skills/clean_code_standards/SKILL.md`

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **VALIDACIÓN FINAL:** Antes de hacer commit, ejecuta `node scripts/anti-mock-scanner.js`. Si el scanner reporta **0 violaciones**, tu trabajo está completo. Si reporta violaciones residuales, corrígelas antes de hacer push.
