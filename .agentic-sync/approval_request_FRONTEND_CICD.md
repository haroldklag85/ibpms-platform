# Solicitud de Revisión — Remediación CI/CD PR #4

**Arquitecto Líder**,

He analizado los 9 tests fallidos documentados en el handoff de la iteración `CICD-FIX-PR4` en la rama `DevDavid`. Solicito su aprobación formal para el siguiente plan de remediación, el cual se alinea estrictamente con sus directrices y hallazgos forenses.

## Plan de Ejecución

1. **Test 9 (rbacStore)**:
   - Archivo: `frontend/src/stores/rbacStore.js`
   - Acción: Agregar la función `revokeUserSession` al objeto `return` del store para exponerla, ya que está definida pero no exportada.

2. **Test 4 (FormDesigner CA-83)**:
   - Archivos: `frontend/src/views/admin/Modeler/FormDesigner.vue` y `frontend/src/stores/useFormDesignerStore.ts`
   - Acción: En el template de `FormDesigner.vue`, actualizar la llamada `generateMockPath('fuzz')` para pasar la variable local existente (`fuzzerPayload` o equivalente) como segundo argumento, cumpliendo con la firma esperada por el store.

3. **Tests 2-3 (FormDesignerQACert)**:
   - Archivo: `frontend/src/views/admin/Modeler/__tests__/FormDesignerQACert.spec.ts`
   - Acción: Actualizar el mock del test para usar los campos reales devueltos por el backend (`formFields` en lugar de `schemaVariables`, `version` en lugar de `versionId`), sincronizándolo con lo que `useFormDesignerStore.ts` espera.

4. **Test 6 (axiosInterceptor)**:
   - Archivo: `frontend/src/tests/services/axiosInterceptor.spec.ts`
   - Acción: Modificar el mock de la respuesta 403 para incluir `code: 'ACCESS_REVOKED'` (o `ROLE_REVOKED`), para activar legítimamente el método `purgeTopology()` del interceptor, en lugar de simular un error 403 genérico.

5. **Test 5 (BpmnDesigner)**:
   - Archivo: `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`
   - Acción: Reemplazar el espía sobre `useRouter().replace` con un espía sobre `window.history.replaceState`, alineando la aserción con la nueva API nativa utilizada en el componente refactorizado.

6. **Test 1 (IdentityGovernance)**:
   - Archivo: `frontend/src/views/admin/Security/__tests__/IdentityGovernance.spec.ts`
   - Acción: Eliminar la invocación directa a métodos internos (`wrapper.vm.isCoreRole`) y reescribir la aserción para probar el comportamiento visible (DOM). Específicamente, verificar que los botones de editar/eliminar estén deshabilitados para los roles CORE (`SUPER_ADMIN`, `NATIVE_ADMIN`).

7. **Test 8 (useFormDesignerStore)**:
   - Archivo: `frontend/src/stores/__tests__/useFormDesignerStore.spec.ts`
   - Acción: Ajustar la aserción del test `MockPath_Returns_Array_For_FieldArray`. Ya que `flatFields()` aplana las propiedades hijas al nivel raíz, el test debe verificar las propiedades directamente en el objeto de nivel superior `parsed`, no en `parsed.gridData`.

8. **Test 7 (availableStages)**:
   - Archivos: `frontend/src/stores/useFormDesignerStore.ts` y `frontend/src/views/admin/Modeler/FormDesigner.vue`
   - Acción: Reconstruir el getter `availableStages` en `useFormDesignerStore.ts`. He verificado que `FormDesigner.vue` lo consume (L161) para iterar opciones. El getter se recreará extrayendo los `stage` únicos de `canvasFields`, filtrando valores predeterminados (como 'START_EVENT', 'ANALYSIS', 'DECISION', 'ALL') y eliminando duplicados.

Tras completar las correcciones, ejecutaré `npm run test -- --run` y `npm run build` para asegurar la calidad cero-errores (Exit Gate) antes de publicar los cambios, además de registrar la bitácora no técnica.

¿Me otorga permiso para proceder al modo `EXECUTION`?
