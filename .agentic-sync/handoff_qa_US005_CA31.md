# 🏗️ Handoff de QA: Etiquetas de Estado en el Catálogo de Procesos (US-005, CA-31)

## 1. Metadatos y SSOT
- **Iteración/Sprint:** Sprint 6
- **Rama de trabajo:** `sprint-6`
- **User Story:** US-005 (Desplegar y Versionar un Modelo de Proceso (BPMN))
- **Criterio de Aceptación:** CA-31 (Etiquetas de Estado en el Catálogo de Procesos)
- **Path del SSOT:** [epic_B_formularios_bpmn.md](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/docs/requirements/epics/epic_B_formularios_bpmn.md#L1491)
- **Flujo de Trabajo:** Fase de Pruebas (RED) -> Implementación de Lógica (GREEN) -> Verificación.

## 2. Alineación Arquitectónica y ADRs
- **ADR 010 (Testing Pyramid):** Garantizar la cobertura lógica en Frontend usando pruebas unitarias en `vitest` con `@vue/test-utils`.
- **Zero-Mock Policy:** Probar la lógica interna de Vue y las aserciones directamente contra la reactividad del componente, simulando datos en el catálogo y verificando el DOM renderizado.
- **Trazabilidad:** Inclusión del comentario obligatorio `// @Traceability: US-005, CA-31 Etiquetas de Estado en el Catálogo` en el archivo de prueba.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a Modificar:** [BpmnDesigner.spec.ts](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts)
- **Estado Actual:** En `BpmnDesigner.vue`, las llamadas a la API se delegan al store. Como `getCatalogProcesses` no está declarado estáticamente en las acciones del store, `vi.spyOn` falla. Debemos asignar el mock directamente: `store.getCatalogProcesses = vi.fn().mockResolvedValue(...)`.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
Debes importar `useIntegrationStore` en la parte superior de [BpmnDesigner.spec.ts](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts):
```typescript
import { useIntegrationStore } from '@/stores/useIntegrationStore';
```

Y reemplazar el bloque del test por el siguiente:

```typescript
    // @Traceability: US-005, CA-31 Etiquetas de Estado en el Catálogo de Procesos
    describe('Pruebas para CA-31 (Etiquetas de Estado en el Catálogo de Procesos)', () => {
        it('Debe renderizar las etiquetas visuales de estado exactas en el catálogo de procesos', async () => {
            const store = useIntegrationStore();
            // Asignar directamente la función mockeada para evitar errores de vi.spyOn por métodos dinámicos
            (store as any).getCatalogProcesses = vi.fn().mockResolvedValue({
                data: [
                    { id: '1', name: 'Proceso Borrador', status: 'BORRADOR', version: 1, lastEdited: '2026-05-27', author: 'Autor A' },
                    { id: '2', name: 'Proceso Activo', status: 'ACTIVO', version: 3, lastEdited: '2026-05-27', author: 'Autor B' },
                    { id: '3', name: 'Proceso Archivado', status: 'ARCHIVADO', version: 2, lastEdited: '2026-05-27', author: 'Autor C' }
                ]
            });

            const wrapper = createWrapper();
            await flushPromises();

            // Abrir el explorador de procesos para renderizar (esto disparará el watch)
            wrapper.vm.showCatalog = true;
            await flushPromises();
            await wrapper.vm.$nextTick();

            const items = wrapper.findAll('.space-y-3 > div');
            expect(items.length).toBe(3);

            // Verificar estados formateados con emojis y versiones
            // Para BORRADOR -> "📝 BORRADOR"
            // Para ACTIVO -> "✅ ACTIVO (v3)"
            // Para ARCHIVADO -> "📦 ARCHIVADO"
            const statusSpans = wrapper.findAll('.space-y-3 > div span.uppercase');
            expect(statusSpans.length).toBe(3);

            expect(statusSpans[0].text()).toBe('📝 BORRADOR');
            expect(statusSpans[1].text()).toBe('✅ ACTIVO (v3)');
            expect(statusSpans[2].text()).toBe('📦 ARCHIVADO');

            wrapper.unmount();
        });
    });
```

## 5. Matriz de QA y Testing Atómico
| Test Name | CA Evaluado | Aserción Esperada |
| --- | --- | --- |
| `Debe renderizar las etiquetas visuales de estado exactas en el catálogo de procesos` | CA-31 | `statusSpans[0].text() === '📝 BORRADOR'`, `statusSpans[1].text() === '✅ ACTIVO (v3)'`, `statusSpans[2].text() === '📦 ARCHIVADO'` |

## 6. Mensaje de Despacho
"Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."
