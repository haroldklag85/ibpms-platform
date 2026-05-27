# Plan de Implementación — Pruebas de Complejidad BPMN (US-005, CA-30)

Este plan describe el enfoque para actualizar la prueba unitaria del validador de complejidad BPMN en la fase roja de TDD dentro de `BpmnDesigner.spec.ts`.

## 1. Objetivos de QA
- Eliminar la inyección/simulación estática de `showToast` dentro de la prueba unitaria.
- Simular la interacción real de carga de archivos usando el input `input-import-bpmn` con un archivo sintético de más de 100 nodos.
- Actualizar las aserciones de la prueba de complejidad para requerir exactamente los mensajes contractuales:
  - `"⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos"`
  - `"Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor"`
- Añadir la marca de trazabilidad obligatoria:
  `// @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable`
- Demostrar que el test falla inicialmente al correr la suite (Fase Roja).

## 2. Pasos de Modificación del Test

1. **Ubicación del Test**: localizaremos el test con descripción `"Debe generar un Toast de Advertencia al importar un archivo BPMN de alta complejidad (> 100 nodos)"`.
2. **Reemplazo de la Simulación**:
   - Reemplazaremos el bloque que invoca directamente a `wrapper.vm.showToast(...)`.
   - Buscaremos el elemento input por su testid `[data-testid="input-import-bpmn"]`.
   - Definiremos la propiedad `files` del input con un objeto `File` que contenga un string con más de 100 nodos BPMN.
   - Dispararemos el evento `change` en el input y ejecutaremos `await flushPromises()`.
3. **Actualización de Aserciones**:
   - Cambiaremos `expect(wrapper.vm.toast.msg).toContain('Alta complejidad')` por dos aserciones estrictas sobre `wrapper.vm.toast.msg`:
     - `expect(wrapper.vm.toast.msg).toContain('⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos')`
     - `expect(wrapper.vm.toast.msg).toContain('Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor')`
4. **Agregar Trazabilidad**:
   - Inyectar el comentario de trazabilidad sobre el test modificado:
     `// @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable`

## 3. Fase Roja de TDD
- Ejecutaremos la suite usando:
  `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
- Verificaremos que el test falle con un error de aserción indicando que el mensaje del toast no contiene las nuevas cadenas del contrato.
- El componente `BpmnDesigner.vue` **NO** se modificará en esta fase para respetar el ciclo TDD Rojo.

## 4. Consolidación
- Una vez verificado el fallo del test en la fase roja, realizaremos commit y push directamente a la rama `sprint-6` (sin usar `git stash`).
