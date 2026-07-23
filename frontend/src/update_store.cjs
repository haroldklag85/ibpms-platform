const fs = require('fs');
const file = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\stores\\genericFormStore.ts';
let content = fs.readFileSync(file, 'utf8');

const replacement = `
export interface UploadedFile {
  file: File;
  progress: number;
  status: 'PENDING' | 'UPLOADING' | 'SUCCESS' | 'ERROR';
  temp_id?: string;
  abortController?: AbortController;
  errorMessage?: string;
}

export interface GenericFormDraft {
`;

content = content.replace('export interface GenericFormDraft {', replacement);

content = content.replace('const files = ref<File[]>([])', 'const files = ref<UploadedFile[]>([])');

const newSubmit = `const submitForm = async () => {
    isSubmitting.value = true
    try {
      // FRONT-029-05: Upload-First Check
      // We assume files are already uploaded by the component (or we can await uploading here if pending).
      const allSuccess = files.value.every(f => f.status === 'SUCCESS' && f.temp_id);
      if (!allSuccess && files.value.length > 0) {
        console.error("No todos los archivos han subido exitosamente");
        return false;
      }
      
      const payload: any = {
        observations: observations.value,
        result: result.value,
        attachments: files.value.map(f => f.temp_id)
      };

      if (panicAction.value) {
        if (!panicJustification.value || panicJustification.value.length < 20) {
          console.error('Panic justification must be >= 20 characters')
          return false
        }
        payload.panicAction = panicAction.value;
        payload.panicJustification = panicJustification.value;
      }

      await apiClient.post(\`/workbox/tasks/\${taskId.value}/generic-form-complete\`, payload);

      // On success, clear drafts
      await clearDraft()
      return true
    } catch (err: any) {
      console.error("Submit error", err)
      if (err.response && err.response.status === 409 && err.response.data?.error === 'SchemaVersionConflict') {
        throw new Error('SchemaVersionConflict');
      }
      return false
    } finally {
      isSubmitting.value = false
    }
  }`;

const submitRegex = /const submitForm = async \(\) => \{[\s\S]*?return true\n    \} catch \(e\) \{[\s\S]*?finally \{\n      isSubmitting\.value = false\n    \}\n  \}/;
content = content.replace(submitRegex, newSubmit);

fs.writeFileSync(file, content);
console.log('Done genericFormStore');
