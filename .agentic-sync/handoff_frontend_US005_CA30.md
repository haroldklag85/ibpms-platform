# Handoff Frontend — US-005, CA-30

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-30 (Límite de Complejidad Parametrizable y Advertencia de Mala Práctica)
> **Estado:** Delegado para Ajustes de Código e Integridad de Trazabilidad

---

## 1. Requerimientos Técnicos

### R1. Actualizar Mensajes de Advertencia en `BpmnDesigner.vue`
Modifique las advertencias de complejidad en `frontend/src/views/admin/Modeler/BpmnDesigner.vue` para que muestren la redacción exacta exigida:

1. **En `handleFileUpload` (aproximadamente línea 1583):**
   - **Mensaje actual:** `'⚠️ Advertencia: Alta complejidad. Proceso con más de 100 nodos.'`
   - **Mensaje esperado:** `⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos. Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor.` (o parametrizar los "100" con el valor de `bpmnComplexityLimit.value` si es dinámico).

2. **En el listener de `commandStack.changed` (aproximadamente línea 1387):**
   - **Mensaje actual:** ``⚠️ Mala Práctica: Diagrama excede [${bpmnComplexityLimit.value}] nodos. Riesgo de mantenimiento y rendimiento motor.``
   - **Mensaje esperado:** ``⚠️ Mala Práctica de Diseño: Este proceso supera los ${bpmnComplexityLimit.value} nodos. Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor.``

### R2. Trazabilidad Obligatoria (Ley Global 3)
Asegúrese de incluir la marca de trazabilidad en ambos bloques:
`// @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable`

### R3. Validar que la compilación Frontend funcione
Ejecute el build de producción del frontend para asegurar que no hay errores de TypeScript o empaquetado:
`npm run build`

---

## 2. Directivas de Validación y Calidad

- **Clean Code:** Mantenga la legibilidad y la estructura original del archivo.
- **TDD:** Asegure que al ejecutar los tests de Vitest, la suite `BpmnDesigner.spec.ts` pase exitosamente.
- Cree su plan de trabajo en `.agentic-sync/approval_request_frontend.md` y espere la aprobación del Arquitecto Líder antes de modificar el archivo.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
