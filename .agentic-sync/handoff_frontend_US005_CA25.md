# Handoff Frontend — US-005, CA-25

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-25 (Zoom, Minimap y Navegación Visual)
> **Estado:** Delegado para Ajustes de Código e Integridad de Trazabilidad

---

## 1. Requerimientos Técnicos

### R1. Modificar Comentario de Zoom Controls en `BpmnDesigner.vue`
En el archivo `frontend/src/views/admin/Modeler/BpmnDesigner.vue` (aproximadamente en la línea 1838), localice el encabezado de controles de zoom:
- **Actual:** `// ── Zoom Controls (CA-16) ────────────────────────────────────`
- **Esperado:** `// @Traceability: US-005, CA-25 Zoom y Minimap`

### R2. Validar que la compilación Frontend funcione
Ejecute el build de producción del frontend para asegurar que no hay errores de TypeScript o empaquetado:
`npm run build`

---

## 2. Directivas de Validación y Calidad

- **Clean Code:** Mantenga la legibilidad y la estructura original del archivo.
- **TDD:** Asegure que al ejecutar los tests de Vitest, la suite `BpmnDesigner.spec.ts` pase exitosamente (10 pruebas en verde).
- Cree su plan de trabajo en `.agentic-sync/approval_request_frontend.md` y espere la aprobación del Arquitecto Líder antes de modificar el archivo.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
