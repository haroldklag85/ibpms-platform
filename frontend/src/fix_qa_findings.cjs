const fs = require('fs');

const genericFormStorePath = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\stores\\genericFormStore.ts';
let storeContent = fs.readFileSync(genericFormStorePath, 'utf8');

// Replace interface and ref type
storeContent = storeContent.replace(
  'export interface GenericFormDraft {',
  `export interface UploadedFile {
  file: File;
  progress: number;
  status: 'PENDING' | 'UPLOADING' | 'SUCCESS' | 'ERROR';
  temp_id?: string;
  abortController?: AbortController;
  errorMessage?: string;
}

export interface GenericFormDraft {`
);

storeContent = storeContent.replace(
  'const files = ref<File[]>([])',
  'const files = ref<UploadedFile[]>([])'
);

// Replace submitForm
const newSubmitForm = `const submitForm = async () => {
    isSubmitting.value = true
    try {
      if (files.value.some((f: any) => f.status === 'UPLOADING')) {
        console.error('Hay archivos aún subiendo')
        return false
      }

      const attachmentIds = files.value.filter((f: any) => f.status === 'SUCCESS').map((f: any) => f.temp_id)

      await apiClient.post(\`/workbox/tasks/\${taskId.value}/generic-form-complete\`, {
        observations: observations.value,
        result: result.value,
        panicAction: panicAction.value || undefined,
        panicJustification: panicJustification.value || undefined,
        attachments: attachmentIds
      })

      // On success, clear drafts
      await clearDraft()
      return true
    } catch (e: any) {
      console.error("Submit error", e)
      throw e
    } finally {
      isSubmitting.value = false
    }
  }`;

const submitRegex = /const submitForm = async \(\) => \{[\s\S]*?finally \{\n      isSubmitting\.value = false\n    \}\n  \}/;
storeContent = storeContent.replace(submitRegex, newSubmitForm);

fs.writeFileSync(genericFormStorePath, storeContent);

console.log("Updated genericFormStore.ts");

const genericFormBodyPath = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\components\\forms\\generic\\GenericFormBody.vue';
let bodyContent = fs.readFileSync(genericFormBodyPath, 'utf8');

const newCatch = `} catch (err: any) {
    if (err.response?.status === 409) {
        showSchemaConflictModal.value = true
        feedback.reset()
    } else {
        feedback.setError(err.response?.data?.message || err.message || 'Error desconocido al completar.')
    }
}`;

const catchRegex = /\} catch \(err: any\) \{[\s\S]*?Error desconocido al completar\.'\)\n\s*\}\n\s*\}/;
bodyContent = bodyContent.replace(catchRegex, newCatch);

fs.writeFileSync(genericFormBodyPath, bodyContent);

console.log("Updated GenericFormBody.vue");
