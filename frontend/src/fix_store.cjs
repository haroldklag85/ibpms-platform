const fs = require('fs');
const storeFile = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\stores\\genericFormStore.ts';
let storeContent = fs.readFileSync(storeFile, 'utf8');

const newSubmit = `const submitForm = async () => {
    isSubmitting.value = true
    try {
      const allSuccess = files.value.every((f: any) => f.status === 'SUCCESS' && f.temp_id);
      if (!allSuccess && files.value.length > 0) {
        throw new Error('UploadIncomplete');
      }

      const payload: any = {
        observations: observations.value,
        result: result.value,
        attachments: files.value.filter((f: any) => f.temp_id).map((f: any) => f.temp_id)
      };

      if (panicAction.value) {
        if (!panicJustification.value || panicJustification.value.length < 20) {
          throw new Error('Panic justification must be >= 20 characters');
        }
        payload.panicAction = panicAction.value;
        payload.panicJustification = panicJustification.value;
      }

      await apiClient.post(\`/workbox/tasks/\${taskId.value}/generic-form-complete\`, payload)

      // On success, clear drafts
      await clearDraft()
      return true
    } catch (e: any) {
      console.error("Submit error", e)
      throw e;
    } finally {
      isSubmitting.value = false
    }
  }`;

const submitRegex = /const submitForm = async \(\) => \{[\s\S]*?finally \{\n      isSubmitting\.value = false\n    \}\n  \}/;
storeContent = storeContent.replace(submitRegex, newSubmit);

fs.writeFileSync(storeFile, storeContent);

const vueFile = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\components\\forms\\generic\\GenericFormBody.vue';
let vueContent = fs.readFileSync(vueFile, 'utf8');

const newCatch = `} catch (err: any) {
      if (err.response?.status === 409 || err.response?.data?.error === 'SchemaVersionConflict') {
          showSchemaConflictModal.value = true
          feedback.reset() // Remove overlay
      } else {
          feedback.setError(err.response?.data?.message || err.message || 'Error desconocido al completar.')
      }
  }`;

const catchRegex = /} catch \(err: any\) \{[\s\S]*?Error desconocido al completar\.'\)\n      \}\n  \}/;
vueContent = vueContent.replace(catchRegex, newCatch);

fs.writeFileSync(vueFile, vueContent);

console.log("Done fixing OBS-F029-01 and OBS-F029-03");
