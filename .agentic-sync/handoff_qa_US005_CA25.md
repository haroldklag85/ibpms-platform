# Handoff QA — US-005, CA-25

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-25 (Zoom, Minimap y Navegación Visual)
> **Estado:** Delegado para Creación de Pruebas

---

## 1. Requerimientos Técnicos

### R1. Modificar `BpmnDesigner.spec.ts`
En el archivo `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`, realice los siguientes cambios:
1. Quite el `.skip` de la suite principal (`describe('Pantalla 6: BPMN Designer (Frontend QA)', ...)`) para habilitar la suite.
2. Agregue pruebas unitarias/componentes específicas para **CA-25**:
   - **Prueba 1 (Existencia de Controles):** Debe verificar que los botones de Zoom In (`+`), Zoom Out (`-`) y Zoom Fit (`O`) existen en el lienzo con sus títulos/clases correspondientes.
   - **Prueba 2 (Funcionalidad de Zoom In):** Debe verificar que hacer clic en el botón de Zoom In llama a `canvas.zoom` con un incremento del nivel actual (+0.3).
   - **Prueba 3 (Funcionalidad de Zoom Out):** Debe verificar que hacer clic en el botón de Zoom Out llama a `canvas.zoom` con un decremento del nivel actual (-0.3).
   - **Prueba 4 (Funcionalidad de Zoom Fit):** Debe verificar que hacer clic en el botón de Zoom Fit llama a `canvas.zoom('fit-viewport')`.
   - **Prueba 5 (Minimap Abierto):** Debe verificar que el minimap se inicializa y se abre por defecto al montar el componente.

### R2. Trazabilidad Obligatoria (Ley Global 3)
Asegúrese de incluir la marca de trazabilidad en las pruebas:
`// @Traceability: US-005, CA-25 Zoom y Minimap`

---

## 2. Directivas de Ejecución

- **TDD (Fase Roja):** Diseñe el test de tal forma que inicialmente falle si el mock o el componente no están adecuadamente enlazados, o si los mocks de la suite no soportan el comportamiento de zoom interactivo.
- Cree su plan de trabajo en `.agentic-sync/approval_request_qa.md` y espere la aprobación del Arquitecto Líder antes de modificar el archivo.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia strictly en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
