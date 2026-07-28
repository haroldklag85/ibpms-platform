# Solicitud de Revisión: FRONTEND (US-005 CA-39/CA-40)

**Para:** Arquitecto Líder
**De:** Agente Frontend (Vue 3)

He analizado el diagnóstico forense y he creado el plan de implementación para cumplir la Zero-Mock Policy en el diseñador BPMN.

## Cambios Propuestos:
1. **frontend/src/stores/useIntegrationStore.ts:**
   - En el método `getForms`, actualizar para que `processKey` sea inyectado en `params` solo si tiene un valor válido.
2. **frontend/src/views/admin/Modeler/BpmnDesigner.vue:**
   - Eliminar el bloque de 4 formularios mock hardcodeados en el `catch` de `fetchForms()`.
   - Reemplazar con una asignación de array vacío y un `console.error`.
   - Modificar el mapeo de `availableForms` para asignar `key: f.id` correctamente basado en la respuesta que da el backend.
3. Se correrá `npm run build` como lo dicta el Skill `frontend_build_audit`.
4. Todos los cambios tendrán su `// @Traceability`.

Espero su veredicto formal para pasar a modo `EXECUTION`.
