# 🧠→🎨 Handoff Bug-Fix: ARQUITECTO LÍDER → FRONTEND - VUE
# BUG-US005-VERSIONS-FE: Corrección y Limpieza de Mocks en Historial de Versiones

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND - VUE
**Fecha:** 2026-06-06T14:02:00-05:00
**Rama de corrección:** `DevDavid/bugfix/US-005-versions-api`
**Prioridad:** 🔴 Alta
**Dependencia:** Tarea `BUG-US005-VERSIONS-BE` en Backend debe estar completada y desplegada localmente.

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales 0-4)
cat .cursorrules

# 2. Skill principal del agente Frontend
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/addyosmani_sre_discipline/SKILL.md

# 4. Diagnóstico del bug
cat .agentic-sync/bug_diagnosis_us005_versions.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código modificado DEBE llevar
> `// @Traceability: US-005, CA-15, BUG-FIX: Limpiar mocks del historial de versiones y mapear respuesta del backend`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El panel de historial de versiones muestra mocks fijos e inexistentes cuando el backend falla al obtener versiones para un borrador nuevo (HTTP 400). Debemos eliminar los datos mock fijos, mapear la respuesta real del backend e implementar una vista amigable de "lista vacía" en la UI.

| Hallazgo | Ubicación | Detalle |
|----------|:---------:|---------|
| Fallback con datos mock fijos | [BpmnDesigner.vue:2262-2270](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L2262-L2270) | Reemplaza el resultado real por el mock fijo de Carlos M. y Ana García en el bloque `catch`. |
| Renderizado incompleto | [BpmnDesigner.vue:1105-1117](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L1105-L1117) | No maneja el caso de array vacío (`versionHistory.length === 0`). Solo dibuja los elementos si existen. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Limpiar mocks y mapear contrato de backend en Vue logic
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Edita el método `fetchVersions` de la siguiente manera:
- Elimina el array mock en el `catch`. En su lugar, inicializa `versionHistory.value = []`.
- Mapea la respuesta del backend para alinear el contrato de nombres: `versionId` a `version`, `isLatest` a `status` (traducido a `ACTIVO` o `ARCHIVADO`), y conserva `date` y `author`.

```typescript
// @Traceability: US-005, CA-15, BUG-FIX: Limpiar mocks del historial de versiones y mapear respuesta del backend
const fetchVersions = async () => {
  loadingVersions.value = true;
  try {
    const { data } = await integrationStore.getProcessVersions(processId.value);
    if (data && Array.isArray(data)) {
      versionHistory.value = data.map((v: any) => ({
        version: v.versionId,
        date: v.date || 'Sin fecha',
        author: v.author || 'Sistema',
        status: v.isLatest ? 'ACTIVO' : 'ARCHIVADO'
      }));
    } else {
      versionHistory.value = [];
    }
  } catch (err) {
    console.error('Error al obtener versiones del proceso:', err);
    versionHistory.value = [];
  } finally {
    loadingVersions.value = false;
  }
};
```

---

### Paso 2: Manejar lista vacía en el template HTML
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Modifica el div de la lista de versiones en el template (alrededor de la línea 1104) para agregar la directiva `v-else-if` y el mensaje alternativo `v-else`:

```html
        <!-- @Traceability: US-005, CA-15, BUG-FIX: Renderizar mensaje cuando no hay versiones publicadas -->
        <div class="flex-1 overflow-y-auto p-3 space-y-2">
          <div v-if="loadingVersions" class="text-center text-xs text-gray-500 py-4">Cargando versiones...</div>
          <div v-else-if="versionHistory.length > 0" v-for="v in versionHistory" :key="v.version" class="flex justify-between items-center p-2 rounded hover:bg-gray-50 dark:hover:bg-gray-700 text-sm border border-gray-100 dark:border-gray-700 transition group">
            <div>
              <span class="font-bold text-gray-800 dark:text-white">v{{ v.version }}</span>
              <p class="text-[10px] text-gray-500">{{ v.date }} — {{ v.author }}</p>
            </div>
            <div class="flex flex-col items-end gap-1">
              <span :class="v.status === 'ACTIVO' ? 'text-green-600' : 'text-gray-500'" class="text-[10px] font-bold">{{ v.status }}</span>
              <!-- CA-15 Botón Restaurar -> Clonar -->
              <button v-if="v.status !== 'ACTIVO' && !isLocked" @click="restoreVersion(v.version)" class="text-[10px] bg-amber-100 hover:bg-amber-200 text-amber-800 px-2 py-0.5 rounded shadow-sm opacity-0 group-hover:opacity-100 transition disabled:opacity-50" title="Ejecutar Rollback Un Clic">
                Clonar como V_NUEVA (Rollback) ↺
              </button>
            </div>
          </div>
          <div v-else class="text-center text-xs text-gray-500 py-10" data-testid="no-versions-msg">
            No hay versiones publicadas aún.
          </div>
        </div>
```

---

### Paso 3: Agregar Test Unitario en Vitest
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`

Inserta la siguiente especificación de prueba unitaria dentro del `describe('US-005: Activity Timeline (CA-42)')` o al final del archivo:

```typescript
        // @Traceability: US-005, CA-15, BUG-FIX: Test de integración/unitario de versiones vacías
        it('Debe renderizar mensaje de no hay versiones cuando el backend retorna una lista vacia', async () => {
            const store = useIntegrationStore();
            vi.spyOn(store, 'getProcessVersions').mockResolvedValue({ data: [] });

            // Abrimos versiones
            wrapper.vm.showVersions = true;
            await wrapper.vm.fetchVersions();
            await flushPromises();

            expect(wrapper.vm.versionHistory.length).toBe(0);
            
            const msg = wrapper.find('[data-testid="no-versions-msg"]');
            expect(msg.exists()).toBe(true);
            expect(msg.text()).toContain('No hay versiones publicadas aún.');
        });
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Ejecución de pruebas unitarias exitosa | `npm run test` (o Vitest) en frontend corre exitosamente y el nuevo test pasa |
| 2 | Compilación sin warnings o errores de tipado | `npm run build` genera bundle final de manera exitosa |
| 3 | Código documentado con @Traceability | Comentarios de trazabilidad inyectados en `BpmnDesigner.vue` y `BpmnDesigner.spec.ts` |
| 4 | No se usó `git stash` | Confirmar mediante commit atómico directo |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Posicionarse en la rama de bugfix: `git checkout DevDavid/bugfix/US-005-versions-api`.
2. Aplicar las correcciones quirúrgicas en `BpmnDesigner.vue` (script + template).
3. Añadir el test unitario en `BpmnDesigner.spec.ts`.
4. Ejecutar las pruebas unitarias: `npm run test` (o similar de vitest) y certificar que pasen.
5. Ejecutar la compilación del frontend: `npm run build`.
6. Confirmar éxito y hacer push:
   `git add . && git commit -m "fix(frontend): US-005 BUG-FIX versions history clean mocks and show empty view" && git push origin DevDavid/bugfix/US-005-versions-api`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🎨 FRONTEND - VUE (Agente de Corrección Quirúrgica de Bugs).

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos:
1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agentic-sync/bug_diagnosis_us005_versions.md
5. cat .agentic-sync/handoff_bugfix_frontend_us005_versions.md

TU MISIÓN:
1. Posicionarte en la rama: git checkout DevDavid/bugfix/US-005-versions-api
2. Aplicar las correcciones quirúrgicas en BpmnDesigner.vue
3. Agregar el test unitario en BpmnDesigner.spec.ts
4. Ejecutar pruebas unitarias de frontend
5. Ejecutar npm run build para certificar compilación
6. Commit: git add . && git commit -m "fix(frontend): US-005 BUG-FIX versions empty view" && git push

REGLAS INQUEBRANTABLES:
- PROHIBIDO modificar archivos fuera del alcance del handoff.
- PROHIBIDO crear funciones nuevas no especificadas.
- PROHIBIDO omitir @Traceability en el código modificado.
- PROHIBIDO usar git stash.
```
