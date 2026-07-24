# 🧠→🎨 Handoff: ARQUITECTO LÍDER → FRONTEND - VUE 3
# sprint-01-DevDavid-BPMN: Corrección del Dropdown FormKey en BpmnDesigner y Eliminación de Mock Fallback (US-005 CA-39/CA-40)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND - VUE 3
**Fecha:** 2026-06-22T15:24:00-05:00
**Sprint:** sprint-01-DevDavid-BPMN
**Prioridad:** 🔴 Alta — Requerimiento urgente del cliente
**Dependencia:** ✅ Backend DEBE haber completado y pusheado `handoff_backend_US005_CA39_CA40_BPMN_FORM_BINDING.md`
**Rama de trabajo:** `DevDavid`

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Arquitectura Core
cat docs/architecture/arquitecturar.md

# 2. Skill principal del agente Frontend
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADR relevante
cat docs/architecture/adr-002-vue3-microfrontends.md

# 5. SSOT
cat docs/requirements/epics/epic_B_formularios_bpmn.md
# Busca: US-005 CA-39 (FormKey Dropdown), CA-40 (Consistencia Patrón)

# 6. Contratos API
cat docs/sprints/gobernanza_pm/API_CONTRACTS.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> comentario `// @Traceability: US-005, CA-39, CA-40`.

> ⚠️ **POLÍTICA ANTIAMNESIA:** Lee los archivos arriba ANTES de tocar código.

---

## 🔬 Diagnóstico del Arquitecto

El BpmnDesigner.vue tiene un dropdown `<select>` para FormKey que funciona técnicamente, pero:
1. El dropdown aparece **vacío** porque la llamada a `GET /api/v1/forms/active` no retorna formularios (bug del Backend, ya asignado)
2. Si la API falla, existe un **fallback a 4 formularios mock hardcodeados** en las líneas ~2601-2607 — esto VIOLA la política zero-mock
3. El mecanismo de carga de forms usa `integrationStore.getForms(processId)` pero el store puede no estar llamando al endpoint correcto

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Mock fallback hardcodeado | `BpmnDesigner.vue:~2601-2607` | 4 formularios falsos aparecen si la API falla |
| Posible endpoint incorrecto en store | `integrationStore` (buscar archivo) | Puede estar llamando a URL incorrecta |
| No hay feedback al usuario si la API falla | `BpmnDesigner.vue` | El dropdown se llena silenciosamente con mocks |

**Componentes Frontend existentes reutilizables:**

| Componente | Ubicación | Propósito |
|-----------|:---------:|---------|
| `BpmnDesigner.vue` | `frontend/src/views/admin/Modeler/BpmnDesigner.vue` | Canvas BPMN con properties panel |
| `useFormDesignerStore.ts` | `frontend/src/stores/useFormDesignerStore.ts` | Store de formularios (tiene formKey refs) |
| Integration Store | Buscar en `frontend/src/stores/` | Store que tiene `getForms()` |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Localizar el store de integración

Busca en `frontend/src/stores/` el archivo que contiene el método `getForms()`. Puede llamarse `useIntegrationStore.ts`, `useBpmnIntegrationStore.ts` o similar. Lee el método completo.

### Paso 2: Verificar la URL del endpoint

El método `getForms()` DEBE llamar a:
```typescript
// @Traceability: US-005, CA-39
const response = await apiClient.get('/api/v1/forms/active')
```

Si tiene un parámetro `processKey` que filtra innecesariamente, hazlo opcional:
```typescript
// @Traceability: US-005, CA-39
async getForms(processKey?: string): Promise<FormCatalogItem[]> {
  const params: Record<string, string> = {}
  if (processKey && processKey.trim() !== '') {
    params.processKey = processKey
  }
  const response = await apiClient.get('/api/v1/forms/active', { params })
  return response.data
}
```

### Paso 3: Eliminar el mock fallback

**Archivo:** `BpmnDesigner.vue` — Busca las líneas ~2601-2607 donde hay formularios hardcodeados.

REMPLAZA el bloque de fallback mock por un manejo de error real:
```typescript
// @Traceability: US-005, CA-39 — Eliminación de mock fallback (Zero-Mock Policy)
try {
  const formsData = await integrationStore.getForms()
  availableForms.value = formsData.map(f => ({
    key: f.technicalName,
    name: f.name,
    type: f.pattern === 'SIMPLE' ? '🟢 Simple' : '🔵 iForm Maestro'
  }))
} catch (error) {
  console.error('[BpmnDesigner] Error cargando catálogo de formularios:', error)
  availableForms.value = []
  // Mostrar toast/snackbar de error al usuario
}
```

**PROHIBIDO** dejar los 4 formularios mock como fallback.

### Paso 4: Verificar el binding del `<select>` al BPMN XML

En las líneas ~522-534 del BpmnDesigner.vue, verifica que:
1. El `<select>` muestra las opciones cargadas de `availableForms`
2. Al seleccionar un formulario, `syncElementProperties('camunda:formKey', selectedFormKey)` escribe el valor en el XML BPMN
3. Al abrir una UserTask existente, `safeGet(bo, 'camunda:formKey')` lee el valor del XML

No modifiques este mecanismo si ya funciona — solo asegura que el dropdown se puebla correctamente.

### Paso 5: Verificar el filtrado por patrón (CA-40)

En el computed `filteredForms`, verifica que filtra correctamente por el patrón del proceso:
```typescript
// @Traceability: US-005, CA-40
const filteredForms = computed(() => {
  if (!processPattern.value) return availableForms.value
  return availableForms.value.filter(f => 
    f.type.includes(processPattern.value === 'SIMPLE' ? 'Simple' : 'Maestro')
  )
})
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El dropdown FormKey muestra formularios reales (no mocks) al seleccionar una UserTask | Abrir `localhost:5173/admin/modeler/bpmn`, crear UserTask, ver dropdown con formularios de BD |
| 2 | NO existen formularios hardcodeados en el código de BpmnDesigner.vue | `grep -n "Aprobación Rápida\|Crédito Base\|mock\|fallback" BpmnDesigner.vue` → 0 resultados |
| 3 | Si la API falla, el dropdown queda vacío con error en consola (NO muestra mocks) | Apagar backend, abrir BPMN designer, verificar dropdown vacío y error en console.log |
| 4 | Al seleccionar un formulario del dropdown, el `camunda:formKey` se escribe correctamente en el XML BPMN | Exportar el XML BPMN y verificar que contiene `camunda:formKey="<technicalName>"` |
| 5 | Build exitoso + commit en rama `DevDavid` | `npm run build` → sin errores |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Ejecutar `git pull origin DevDavid` para obtener cambios del Backend
2. Verificar que el backend está corriendo: `curl -s http://localhost:8080/actuator/health`
3. Verificar que el endpoint funciona: `curl http://localhost:8080/api/v1/forms/active`
4. Localizar el store de integración y corregir la URL del endpoint
5. Eliminar el mock fallback del BpmnDesigner.vue
6. Verificar el binding del `<select>` al BPMN XML
7. Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
8. Probar manualmente: abrir BPMN designer, crear UserTask, ver dropdown con formularios reales
9. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`:
   > "Se corrigió el selector de formularios en el diseñador de procesos BPMN para que muestre los formularios reales creados por el usuario, eliminando datos de prueba que aparecían anteriormente."
10. `git add . && git commit -m "fix(bpmn): eliminar mock fallback y corregir carga de formularios en dropdown CA-39/CA-40" && git push origin DevDavid`

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🎨 Desarrollador Frontend Vue 3.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat docs/architecture/arquitecturar.md
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/adr-002-vue3-microfrontends.md
6. cat .agentic-sync/handoff_frontend_US005_CA39_CA40_BPMN_FORM_BINDING.md

TU MISIÓN:

1. Localizar el store de integración que tiene getForms() y corregir la URL del endpoint
2. Eliminar el mock fallback hardcodeado del BpmnDesigner.vue (líneas ~2601-2607)
3. Verificar que el dropdown FormKey muestra formularios reales de la BD
4. Build: Ejecutar protocolo Zero-Trust UI (.agents/skills/frontend_build_audit/SKILL.md)
5. Bitácora: Agrega entrada en docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md
6. Commit: git add . && git commit -m "fix(bpmn): eliminar mock fallback y corregir carga de formularios en dropdown CA-39/CA-40" && git push origin DevDavid

REGLAS INQUEBRANTABLES:
- PROHIBIDO crear datos mock o fallbacks con datos falsos. Zero-Mock Policy.
- PROHIBIDO modificar el FormDesigner.vue ni el useFormDesignerStore.ts. Solo tocar BpmnDesigner.vue y el store de integración.
- PROHIBIDO romper el canvas bpmn-js ni el mecanismo de propiedades existente.
- Todo código nuevo DEBE tener // @Traceability: US-005, CA-39, CA-40
- Es OBLIGATORIO actualizar el CHANGELOG_NO_TECNICO.md antes del commit final.
```

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND_US005_CA39.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND_US005_CA39.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
7. Finaliza con `git commit` y `git push` en la rama `DevDavid`. PROHIBIDO usar git stash.
