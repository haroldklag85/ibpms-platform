# 🏗️ Handoff de QA: Archivar un Proceso sin Instancias Activas (US-005, CA-32)

## 1. Metadatos y SSOT
- **Iteración/Sprint:** Sprint 6
- **Rama de trabajo:** `sprint-6`
- **User Story:** US-005 (Desplegar y Versionar un Modelo de Proceso (BPMN))
- **Criterio de Aceptación:** CA-32 (Archivar un Proceso sin Instancias Activas)
- **Path del SSOT:** [epic_B_formularios_bpmn.md](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/docs/requirements/epics/epic_B_formularios_bpmn.md#L1498)
- **Flujo de Trabajo:** Fase de Pruebas (RED) -> Implementación de Lógica (GREEN) -> Verificación.

## 2. Alineación Arquitectónica y ADRs
- **ADR 010 (Testing Pyramid):** Cobertura en frontend a través de pruebas de componentes / unitarias en Vitest.
- **Zero-Mock Policy:** Probar el comportamiento del DOM basándose en la reactividad de Vue 3, comprobando la presencia/ausencia de atributos del botón en base a datos simulados del catálogo.
- **Trazabilidad:** Agregar la etiqueta obligatoria `// @Traceability: US-005, CA-32 Archivar un Proceso sin Instancias Activas` en el archivo de prueba.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a Modificar:** [BpmnDesigner.spec.ts](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts)
- **Estado Actual:** El archivo contiene la suite de pruebas del diseñador, incluyendo la prueba de CA-31. Agregaremos el nuevo describe de CA-32 al final de la suite.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
Debes añadir la siguiente prueba unitaria en [BpmnDesigner.spec.ts](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts):

```typescript
    // @Traceability: US-005, CA-32 Archivar un Proceso sin Instancias Activas
    describe('Pruebas para CA-32 (Archivar un Proceso sin Instancias Activas)', () => {
        it('Debe habilitar el botón Archivar si no existen instancias activas', async () => {
            const store = useIntegrationStore();
            (store as any).getCatalogProcesses = vi.fn().mockResolvedValue({
                data: [
                    { id: '1', name: 'Proceso Activo Sin Instancias', status: 'ACTIVO', version: 1, activeInstances: 0, lastEdited: '2026-05-27', author: 'Autor A' }
                ]
            });

            const wrapper = createWrapper();
            await flushPromises();

            // Abrir el explorador de procesos para renderizar
            wrapper.vm.showCatalog = true;
            await flushPromises();
            await wrapper.vm.$nextTick();

            const archiveBtn = wrapper.find('button[title="Archivar Proceso (CA-32)"]');
            expect(archiveBtn.exists()).toBe(true);
            expect(archiveBtn.attributes('disabled')).toBeUndefined();

            wrapper.unmount();
        });

        it('Debe deshabilitar el botón Archivar y mostrar el tooltip si existen instancias activas', async () => {
            const store = useIntegrationStore();
            (store as any).getCatalogProcesses = vi.fn().mockResolvedValue({
                data: [
                    { id: '1', name: 'Proceso Activo Con Instancias', status: 'ACTIVO', version: 1, activeInstances: 5, lastEdited: '2026-05-27', author: 'Autor A' }
                ]
            });

            const wrapper = createWrapper();
            await flushPromises();

            // Abrir el explorador de procesos para renderizar
            wrapper.vm.showCatalog = true;
            await flushPromises();
            await wrapper.vm.$nextTick();

            // El botón debería tener el título dinámico con la advertencia de instancias
            const archiveBtn = wrapper.find('button[title="No se puede archivar: 5 instancias en ejecución"]');
            expect(archiveBtn.exists()).toBe(true);
            expect(archiveBtn.attributes('disabled')).toBeDefined();

            wrapper.unmount();
        });
    });
```

## 5. Matriz de QA y Testing Atómico
| Test Name | CA Evaluado | Aserción Esperada |
| --- | --- | --- |
| `Debe habilitar el botón Archivar si no existen instancias activas` | CA-32 | `archiveBtn.attributes('disabled') === undefined` |
| `Debe deshabilitar el botón Archivar y mostrar el tooltip si existen instancias activas` | CA-32 | `archiveBtn.attributes('disabled') !== undefined` |

## 6. Mensaje de Despacho
"Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."
