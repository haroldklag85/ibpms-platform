# Handoff QA — US-005, CA-30

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-30 (Límite de Complejidad Parametrizable y Advertencia de Mala Práctica)
> **Estado:** Delegado para Creación de Pruebas

---

## 1. Requerimientos Técnicos

### R1. Modificar `BpmnDesigner.spec.ts`
En el archivo `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`, localice el test de complejidad:
- **Test actual:** `"Debe generar un Toast de Advertencia al importar un archivo BPMN de alta complejidad (> 100 nodos)"`.
- **Modificación:** 
  1. Actualizar las aserciones para verificar de manera estricta que el mensaje del toast (`wrapper.vm.toast.msg`) contenga exactamente el texto contractual del CA-30:
     - `"⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos"`
     - `"Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor"`
  2. Asegurar que no se simule de manera estática el toast inyectándole directamente el string anterior en el test; en su lugar, la lógica de importación real o el método de importación simulada en el wrapper debe desencadenar la advertencia.

### R2. Trazabilidad Obligatoria (Ley Global 3)
Asegúrese de incluir la marca de trazabilidad en las pruebas:
`// @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable`

---

## 2. Directivas de Ejecución

- **TDD (Fase Roja):** Diseñe el test de tal forma que inicialmente falle al correr la suite, indicando que las aserciones contractuales no se cumplen en el componente actual.
- Cree su plan de trabajo en `.agentic-sync/approval_request_qa.md` y espere la aprobación del Arquitecto Líder antes de modificar el archivo.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia strictly en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
