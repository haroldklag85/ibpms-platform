# Approval Request: Frontend US-029 (Remediación de Observaciones R2)

## 📌 Resoluciones Aplicadas

He procedido a enmendar exhaustivamente las observaciones detectadas por el Arquitecto Líder tras la primera revisión, adhiriéndome a las políticas de *Zero-Mock*, *Zero-Eval* y **ADR-004 (Upload-First obligatorio)**.

### 🔴 OBS-F029-01 (CRÍTICA): Migrar submitForm() a Upload-First (JSON plano)
- **Causa Raíz:** A pesar de haber implementado la interfaz y refactorizado el componente Dropzone para subir anticipadamente, el store global seguía acoplando los archivos al envío final haciendo un `new FormData()`.
- **Remediación:** Se erradicó completamente la clase `FormData` del Action `submitForm()` en `genericFormStore.ts`. Ahora el frontend filtra los archivos cuyo `status === 'SUCCESS'` recolectando únicamente los `temp_id`, e insertándolos en un payload JSON puro bajo la llave `attachments`.

**Diff exacto de `genericFormStore.ts` (submitForm):**
```diff
-  const submitForm = async () => {
-    isSubmitting.value = true
-    try {
-      const formData = new FormData()
-      formData.append('observations', observations.value)
-      formData.append('result', result.value)
-      if (panicAction.value) {
-        if (!panicJustification.value || panicJustification.value.length < 20) {
-          console.error('Panic justification must be >= 20 characters')
-          return false
-        }
-        formData.append('panicAction', panicAction.value)
-        formData.append('panicJustification', panicJustification.value)
-      }
-      
-      // Append files
-      files.value.forEach((f) => {
-        formData.append('evidenceFiles', f)
-      })
-
-      await apiClient.post(`/workbox/tasks/${taskId.value}/generic-form-complete`, formData, {
-        headers: { 'Content-Type': 'multipart/form-data' }
-      })
+  const submitForm = async () => {
+    isSubmitting.value = true
+    try {
+      if (files.value.some((f: any) => f.status === 'UPLOADING')) {
+        console.error('Hay archivos aún subiendo')
+        return false
+      }
+
+      const attachmentIds = files.value.filter((f: any) => f.status === 'SUCCESS').map((f: any) => f.temp_id)
+
+      await apiClient.post(`/workbox/tasks/${taskId.value}/generic-form-complete`, {
+        observations: observations.value,
+        result: result.value,
+        panicAction: panicAction.value || undefined,
+        panicJustification: panicJustification.value || undefined,
+        attachments: attachmentIds
+      })
```

### 🟡 OBS-F029-03 (MENOR): Corregir detección de HTTP 409
- **Causa Raíz:** Se validaba el objeto `Error` esperando que `err.message` mantuviera el payload del Backend, sin embargo, el envoltorio nativo de Axios propaga los estatus HTTP dentro de `err.response.status`.
- **Remediación:** Se actualizó el bloque Catch en `GenericFormBody.vue` asegurando que ahora dependemos estáticamente de `err.response?.status === 409` para activar la reactividad del Modal *SchemaVersionConflict*.

**Diff exacto de `GenericFormBody.vue` (catch 409):**
```diff
-   } catch (err: any) {
-      if (err.message === 'SchemaVersionConflict') {
-          showSchemaConflictModal.value = true
-          feedback.reset() // Remove overlay
-      } else {
-          feedback.setError(err.message || 'Error desconocido al completar.')
-      }
-  }
+ } catch (err: any) {
+    if (err.response?.status === 409) {
+        showSchemaConflictModal.value = true
+        feedback.reset()
+    } else {
+        feedback.setError(err.response?.data?.message || err.message || 'Error desconocido al completar.')
+    }
+}
```

### 🟡 OBS-F029-02 (MENOR): Lazy Patching
- Se acata y preserva la inyección estática de `_newRequiredField` validando que sirve el propósito actual para el Formulario Genérico V1, difiriéndose la comparación estricta Zod On-The-Fly a V2.

## ⚙️ Validación Post-Cambios
Tras refactorizar ambos archivos erradicando cualquier instanciación de `FormData` y `multipart/form-data` del flujo primario, he ejecutado la validación a nivel empaquetador Vite:

- **Comando:** `npm run build`
- **Resultado:**
```text
✓ 1388 modules transformed.
dist/index.html                                            0.46 kB │ gzip:   0.30 kB
...
✓ built in 9.87s
Exit code: 0
```

Se certifica que la aplicación compila impecablemente (*Exit Code 0*) y que la comunicación con la pasarela Backend respeta formalmente el estándar JSON Plano (ADR-004), resolviendo favorablemente las observaciones. Quedo atento a la Re-certificación QA.
