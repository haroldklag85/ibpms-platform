# Handoff Frontend — Remediación CA-12 (Iteración 74-DEV)
## US-005 | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría de CA-12 (Versionamiento Seguro de Reglas DMN) reveló que el `BpmnDesigner.vue` **NO tiene controles UI** para configurar el `camunda:decisionRefBinding` de nodos `BusinessRuleTask`. El SSOT exige que el Arquitecto BPMN pueda elegir entre `LATEST` y `DEPLOYMENT` (default), y que esa configuración se persista en el XML BPMN.

> **Referencia de Auditoría:** `auditoria_us005_ca12_dmn_binding.md`
> **SSOT:** `docs/requirements/v1_user_stories.md` (CA-12 L2165-2171)
> **Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

---

## Tarea 1 — Selector UI para BusinessRuleTask DMN Binding (Brecha Principal)

**Archivo:** `BpmnDesigner.vue`
**Ubicación:** Insertar DESPUÉS de la línea 253 (cierre del bloque `</div>` de Service Task Topics CA-70), ANTES de la línea 255 (Service Task Connector CA-47).

**Código a insertar:**

```vue
<!-- CA-12: Business Rule Task — DMN Binding (Protección de Derechos Adquiridos) -->
<div v-if="selectedElement.type === 'bpmn:BusinessRuleTask'" class="p-3 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded shadow-sm mb-4">
   <label class="block text-xs font-bold text-amber-800 dark:text-amber-300 mb-2 flex items-center justify-between">
      <span>📐 DMN Binding (CA-12)</span>
      <AppTooltip content="Configura si las reglas DMN se evalúan con la versión vigente al desplegar (DEPLOYMENT) o con la última publicada (LATEST). Default: DEPLOYMENT para protección jurídica de derechos adquiridos." />
   </label>
   <p class="text-[10px] text-amber-700 dark:text-amber-400 mb-2">Estrategia de versionamiento de la tabla DMN vinculada a esta tarea.</p>
   <select v-model="selectedElement.props.dmnBinding" 
           @change="syncElementProperties('camunda:decisionRefBinding', selectedElement.props.dmnBinding)" 
           class="w-full text-xs font-mono border-amber-300 dark:border-amber-600 dark:bg-gray-700 dark:text-white rounded p-2 border">
      <option value="deployment">🔒 DEPLOYMENT (Default — Protección de Derechos Adquiridos)</option>
      <option value="latest">⚡ LATEST (Late Binding — Siempre la regla más reciente)</option>
   </select>
   <p class="text-[10px] text-amber-600 dark:text-amber-500 mt-1 leading-tight">
      <strong>DEPLOYMENT:</strong> Los casos en vuelo se evalúan con la DMN activa al nacer el caso.<br>
      <strong>LATEST:</strong> Los casos en vuelo se evalúan con la DMN más reciente publicada.
   </p>
</div>
```

---

## Tarea 2 — Default `DEPLOYMENT` en el State reactivo

**Archivo:** `BpmnDesigner.vue`
**Ubicación:** En el listener `selection.changed` (línea ~1253), agregar `dmnBinding` al objeto `props`:

```typescript
// ANTES (línea 1253-1261):
props: {
    sla: bo.get('camunda:dueDate') || '',
    calledElement: bo.calledElement || '',
    formKey: bo.get('camunda:formKey') || '',
    topic: bo.get('camunda:topic') || '',
    aiTokenLimit: 4000,
    aiTone: 'NEUTRAL'
}

// DESPUÉS:
props: {
    sla: bo.get('camunda:dueDate') || '',
    calledElement: bo.calledElement || '',
    formKey: bo.get('camunda:formKey') || '',
    topic: bo.get('camunda:topic') || '',
    dmnBinding: bo.get('camunda:decisionRefBinding') || 'deployment', // CA-12: Default seguro
    aiTokenLimit: 4000,
    aiTone: 'NEUTRAL'
}
```

**Igualmente**, en el reset de `selectedElement` (línea ~1264), agregar `dmnBinding: 'deployment'`:

```typescript
// ANTES (línea 1264):
selectedElement.value = { id: '', type: '', name: '', props: { aiTokenLimit: 4000, aiTone: 'NEUTRAL', sla: '', calledElement: '', topic: '' } };

// DESPUÉS:
selectedElement.value = { id: '', type: '', name: '', props: { aiTokenLimit: 4000, aiTone: 'NEUTRAL', sla: '', calledElement: '', topic: '', dmnBinding: 'deployment' } };
```

---

## Verificación Obligatoria
1. `npm run build` sin errores de TypeScript.
2. Verificar visualmente en el Modeler: al seleccionar un nodo `BusinessRuleTask`, debe aparecer el panel ámbar con el selector `DEPLOYMENT/LATEST`.
3. Verificar que al cambiar el selector, el XML BPMN exportado contenga `camunda:decisionRefBinding="deployment"` o `"latest"`.
4. Verificar que el default sea siempre `DEPLOYMENT` al crear un nuevo nodo.

---

## Restricciones Arquitectónicas
1. **Vue 3 Composition API** + TypeScript estricto.
2. **No crear componentes nuevos** — inserción in-situ en `BpmnDesigner.vue`.
3. **Usar el mismo patrón** de `syncElementProperties()` que ya emplean los otros selectores (FormKey, Topic, SLA).
4. **Propiedad Camunda correcta:** `camunda:decisionRefBinding` (NO `camunda:decisionBinding`).
5. **No usar** `git stash`. Solo `git commit` + `git push`.
6. **Convención de commit:**
   - `feat(frontend): US-005 CA-12 add DMN Binding selector for BusinessRuleTask (DEPLOYMENT default)`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. **EJECUCIÓN PARALELA CON BACKEND:** No hay dependencia con el Backend para este CA. Puedes ejecutar inmediatamente.
> 2. Ejecuta las 2 tareas y crea un commit atómico.
> 3. Compila con `npm run build` después del commit.
> 4. Rama: `sprint-3/informe_auditoriaSprint1y2`
> 5. Al finalizar, dile al Humano: *"Humano, he implementado el selector CA-12 DMN Binding en el Modeler BPMN. Entrégale este mensaje al Arquitecto Líder."*
