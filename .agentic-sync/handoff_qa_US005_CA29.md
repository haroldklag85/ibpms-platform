# Handoff QA — US-005, CA-29

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-29 (Copiar y Pegar Fragmentos entre Procesos)
> **Estado:** Delegado para Creación de Pruebas

---

## 1. Requerimientos Técnicos

### R1. Modificar `BpmnDesigner.spec.ts`
En el archivo `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`, realice los siguientes cambios:
1. Actualice la simulación (mock) de `bpmn-js/lib/Modeler` para que cuando se solicite el servicio `'clipboard'` (`modeler.get('clipboard')`), este retorne un objeto simulando la interfaz del clipboard nativo de `diagram-js`:
   ```typescript
   if (name === 'clipboard') {
       return {
           get: vi.fn(),
           set: vi.fn(),
           clear: vi.fn(),
           isEmpty: vi.fn()
       };
   }
   ```
2. Agregue pruebas unitarias específicas dentro de una suite `describe('Pruebas para CA-29 (Copiar y Pegar Fragmentos entre Procesos)')`:
   - **Prueba 1 (Guardado en LocalStorage al Copiar):** Debe verificar que al invocar `clipboard.set` con un árbol de elementos copiado, este se serializa y guarda en `localStorage` bajo la clave `bpmn_shared_clipboard`.
   - **Prueba 2 (Recuperación desde LocalStorage al Pegar):** Debe verificar que al invocar `clipboard.get` habiendo datos válidos en `localStorage`, estos son recuperados y retornados de forma correcta.

### R2. Trazabilidad Obligatoria (Ley Global 3)
Asegúrese de incluir la marca de trazabilidad en las pruebas:
`// @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos`

---

## 2. Directivas de Ejecución

- **TDD (Fase Roja):** Diseñe el test de tal forma que inicialmente falle si el decorador de clipboard no está implementado en `BpmnDesigner.vue`.
- Cree su plan de trabajo en `.agentic-sync/approval_request_qa.md` y espere la aprobación del Arquitecto Líder antes de modificar el archivo.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia strictly en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
