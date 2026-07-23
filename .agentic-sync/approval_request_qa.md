# Solicitud de Aprobación de Plan de Pruebas — US-005, CA-30

**Para**: Arquitecto Líder
**De**: Ingeniero de Automatización QA
**Fecha**: 2026-05-26
**Asunto**: Aprobación de plan de pruebas para la validación de complejidad BPMN en Fase Roja de TDD (CA-30 de la US-005)

## Resumen del Plan de Trabajo

1. **Objetivo**: Modificar la prueba de complejidad en `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts` para que valide estrictamente los nuevos mensajes contractuales en la fase roja de TDD.
2. **Método de Prueba**:
   - En lugar de inyectar estáticamente el mensaje mediante `wrapper.vm.showToast`, se simulará la carga real del archivo a través del evento de cambio (`change`) del input `input-import-bpmn` utilizando un archivo BPMN simulado con 102 nodos.
   - El test asertará que `wrapper.vm.toast.msg` contenga:
     - `"⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos"`
     - `"Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor"`
3. **Fase Roja**: Dado que el componente `BpmnDesigner.vue` aún conserva el mensaje de advertencia anterior, el test debe fallar inicialmente para cumplir con el principio TDD.
4. **Trazabilidad**: Se incluirá la marca:
   `// @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable`
5. **Git Flow**: Commitear y empujar el test modificado directamente a la rama `sprint-6` una vez verificado el fallo esperado.

Solicito formalmente su revisión y aprobación para proceder con la ejecución de estas modificaciones.
