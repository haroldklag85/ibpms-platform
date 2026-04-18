import { ref, watch, unref, type MaybeRef } from 'vue';
import { z } from 'zod';
import { ZodBuilder } from '@/views/admin/Modeler/ZodBuilder';

/**
 * useZodFactory.ts
 * TIN-2: Composable Reactive Factory para Runtime Zod
 */
export function useZodFactory(schemaJsonRef: MaybeRef<any[]>, formDataRef: MaybeRef<Record<string, any>>) {
  const schemaObj = unref(schemaJsonRef);
  const formData = unref(formDataRef);
  
  const schema = ref<z.ZodObject<any, any>>(z.object({}));
  const errors = ref<Record<string, string>>({});

  const rebuildSchema = () => {
    try {
      schema.value = ZodBuilder.buildSchema(unref(schemaJsonRef));
    } catch (e) {
      console.error('Error rebuilding Zod Schema:', e);
    }
  };

  watch(() => unref(schemaJsonRef), () => {
    rebuildSchema();
  }, { deep: true, immediate: true });

  const validateOnBlur = (fieldId: string) => {
     const data = unref(formDataRef);
     const result = schema.value.safeParse(data);
     if (!result.success) {
         const issue = result.error.issues.find(iss => iss.path[0] === fieldId);
         if (issue) errors.value[fieldId] = issue.message;
         else delete errors.value[fieldId];
     } else {
         delete errors.value[fieldId];
     }
  };

  const validateAll = () => {
     const data = unref(formDataRef);
     const result = schema.value.safeParse(data);
     errors.value = {};
     if (!result.success) {
         result.error.issues.forEach(iss => {
             if (iss.path[0]) errors.value[iss.path[0].toString()] = iss.message;
         });
         return false;
     }
     return true;
  };

  return {
    schema,
    errors,
    validateOnBlur,
    validateAll,
    rebuildSchema
  };
}
