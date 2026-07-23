# 🏗️ Handoff Arquitectónico - Frontend

## 1. Metadatos y SSOT
- **Iteración:** 84-DEV-LANE-ROLE-UAT-BUGS
- **User Story:** US-005 + US-036 (Lane-Role Assignment — Corrección UAT)
- **Criterios de Aceptación:** Corrección de Bugs UAT B-01, B-02, B-03, B-04 (Frontend)
- **Rama Git:** `DevDavid`
- **Exclusiones:** V2, funcionalidades IA Cognitiva, CRM/Portal, QA automatizado
- **Necesita QA:** `no` (Validación por UAT humano post-corrección)
- **SSOT:** `.agentic-sync/po_instruction_84DEV_UAT_BUGS.md`

## 2. Alineación Arquitectónica y ADRs
- **ADR-002-vue3-microfrontends.md**: Se respeta el ecosistema Vue 3 y el patrón de TailwindCSS. CERO uso de Bootstrap o clases ajenas.
- **Trazabilidad:** Se aplica estrictamente el copiado de patrones visuales pre-existentes de los paneles Task y ServiceTask, garantizando consistencia en el Modeler (Zero Alucinación en UI).

> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

## 3. Rutas Exactas y Contexto Preexistente / 4. Snippets Prescriptivos

### B-01 y B-02: Lane Inputs Bloqueados y Estilos Rotos
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
**Contexto:** `:value` usado erróneamente con `businessObject`, `@input` refrescando en cada tecla, y clases Bootstrap inexistentes en el panel de Lane.

**Acción 1: Modificar handler `selection.changed` (ZONA: Líneas 3118-3150)**
Agrega las propiedades `assignee` y `candidateGroups` al objeto `props`. **NO MUEVAS NI ELIMINES las propiedades existentes**.
```typescript
// Dentro de selectedElement.value = { ... props: { ... } }, agregar después de dmnBinding:
assignee: safeGet(bo, 'camunda:assignee') || '',
candidateGroups: safeGet(bo, 'camunda:candidateGroups') || '',

// Y en el bloque else (aprox línea 3148), agregar en el reset:
assignee: '',
candidateGroups: '',
```

**Acción 2: Reemplazar el panel Lane COMPLETO (ZONA: Líneas 773-838)**
Reemplaza EXCLUSIVAMENTE el bloque de código del Lane con el siguiente (Copiado textual para garantizar consistencia Tailwind con Task/ServiceTask):
```html
        <!-- INICIO: Panel de Propiedades Lane (US-005/US-036 Extension) -->
        <div v-else-if="selectedElement && (selectedElement.type === 'bpmn:Lane' || selectedElement.type === 'bpmn:Participant')" class="space-y-5">
          <!-- Nombre del Lane -->
          <div>
            <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
              Nombre del {{ selectedElement.type === 'bpmn:Lane' ? 'Lane' : 'Participante' }}
            </label>
            <input
              type="text"
              v-model="selectedElement.name"
              @change="syncElementProperties('name', selectedElement.name)"
              class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border"
              placeholder="Ej: Departamento de Contabilidad"
              data-testid="lane-name-input"
            />
          </div>
          <!-- Actor / Participante -->
          <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
            <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2">
              👤 Actor / Participante
            </label>
            <p class="text-[10px] text-gray-500 mb-2">Persona o departamento responsable de este carril.</p>
            <input
              type="text"
              v-model="selectedElement.props.assignee"
              @change="syncElementProperties('camunda:assignee', selectedElement.props.assignee)"
              class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border"
              placeholder="Ej: Departamento de Contabilidad"
              data-testid="lane-actor-input"
            />
          </div>
          <!-- Rol RBAC Vinculado -->
          <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
            <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
              <span>🔐 Rol RBAC Vinculado</span>
            </label>
            <p class="text-[10px] text-gray-500 mb-2">Rol del sistema de seguridad asociado a este carril.</p>
            <select
              v-model="selectedElement.props.candidateGroups"
              @change="syncElementProperties('camunda:candidateGroups', selectedElement.props.candidateGroups)"
              class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border"
              data-testid="lane-linked-role-select"
            >
              <option value="">-- Sin rol vinculado --</option>
              <option v-for="role in rbacStore.roles" :key="role.id" :value="role.name">
                {{ role.name }}
              </option>
            </select>
          </div>
          <!-- Indicador visual de vinculación -->
          <div class="flex items-center gap-2 px-1" data-testid="lane-link-badge">
            <span v-if="selectedElement.props.candidateGroups" class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300">
              ✅ Rol vinculado: {{ selectedElement.props.candidateGroups }}
            </span>
            <span v-else class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300">
              ⚠️ Sin rol RBAC vinculado
            </span>
          </div>
        </div>
        <!-- FIN: Panel de Propiedades Lane (US-005/US-036 Extension) -->
```
*Justificación de clases: copiadas textualmente de los paneles Task y ServiceTask (L510, L660, L726, L648, L650, L651) que usan Tailwind estándar.*

### B-03: Errores API 404
**Archivos a corregir y cambios exactos:**
1. `frontend/src/views/admin/Modeler/InstancesManager.vue` (Aprox. L114):
   Ajustar la URL de `/design/processes/${props.processId}/instances` a `/design/processes/${props.processId}/instances/migratable`, y pasar `sourceVersion`/`targetVersion` según requiere el backend.
2. `frontend/src/services/apiClient.ts` (Aprox. L333):
   Cambiar `/design/external-task-topics` por `/design/processes/external-task-topics`.
3. `frontend/src/stores/useIntegrationStore.ts` (Aprox. L86):
   Cambiar `/design/external-task-topics` por `/design/processes/external-task-topics`.

### B-04 Frontend: FormDesigner Lienzo en Blanco
**Archivo:** `frontend/src/views/admin/Modeler/FormDesigner.vue`
**Contexto:** `showPatternModal` no se está ocultando tras cargar exitosamente un formulario.
**Acción:** En la función `onMounted` (L1258-1269 aprox), DESPUÉS de la línea que verifica `if (res.success)`, AGREGAR:
```typescript
showPatternModal.value = false;
```

## 5. Matriz de QA y Testing Atómico
*Omitido por requerimiento (`Necesita QA = no`). Validación delegada a UAT Humano.*

## 6. Mensaje de Despacho
> Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
> 6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
> 7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
