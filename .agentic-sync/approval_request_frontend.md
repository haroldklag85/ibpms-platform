# Solicitud de Aprobación de Plan de Trabajo — Modificaciones Frontend (US-005, CA-30)

**Para:** Arquitecto Líder
**De:** Desarrollador Frontend AI (Subagent)
**Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
**Criterio de Aceptación:** CA-30 (Límite de Complejidad Parametrizable y Advertencia de Mala Práctica)

---

## Resumen del Plan de Trabajo

### 1. Modificaciones de Código en `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
* **Bloque 1: Carga de Archivos (`handleFileUpload`)**
  * **Ubicación aproximada:** Línea 1583.
  * **Acción:** Reemplazar el mensaje actual por la redacción exacta exigida e incluir la marca de trazabilidad.
  * **Código propuesto:**
    ```typescript
    // @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable
    if (nodeCount > 100) {
      showToast('⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos. Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor.', 'error');
    } else {
    ```

* **Bloque 2: Cambios en Caliente (`commandStack.changed`)**
  * **Ubicación aproximada:** Línea 1387.
  * **Acción:** Reemplazar el mensaje actual por la redacción parametrizada exacta e incluir la marca de trazabilidad.
  * **Código propuesto:**
    ```typescript
    // @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable
    if (count > bpmnComplexityLimit.value) {
      showToast(`⚠️ Mala Práctica de Diseño: Este proceso supera los ${bpmnComplexityLimit.value} nodos. Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor.`, 'error');
    }
    ```

---

## 2. Fase de Verificación y Compilación
1. Ejecutar pruebas unitarias de Vitest:
   `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts` en la carpeta `frontend/`.
2. Producir el build de producción para certificar integridad de Typescript/Vue:
   `npm run build` en la carpeta `frontend/`.

---

## 3. Control de Versiones
1. Confirmar que no hay archivos temporales ni stashes.
2. Hacer commit de los cambios en la rama `sprint-6`.
3. Hacer push directo de la rama a control de versiones.

---

*Quedo a la espera de la aprobación formal del Arquitecto Líder para proceder con la implementación.*
