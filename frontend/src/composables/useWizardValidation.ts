import { ref } from 'vue';
import { z } from 'zod';

export const useWizardValidation = (schemaConfigs: Record<string, z.ZodSchema>) => {
    const wizardErrors = ref<Record<string, Record<string, string>>>({});
    
    const validateStep = (stepName: string, data: any) => {
        const schema = schemaConfigs[stepName];
        if (!schema) return true;

        wizardErrors.value[stepName] = {};
        const result = schema.safeParse(data);
        
        if (!result.success) {
            const stepErrs: Record<string, string> = {};
            result.error.errors.forEach(e => {
                if (e.path[0]) {
                    stepErrs[e.path[0].toString()] = e.message;
                }
            });
            wizardErrors.value[stepName] = stepErrs;
            return false;
        }
        return true;
    };

    const hasStepErrors = (stepName: string) => {
        return Object.keys(wizardErrors.value[stepName] || {}).length > 0;
    };

    const clearStepErrors = (stepName: string) => {
        wizardErrors.value[stepName] = {};
    };

    return {
        wizardErrors,
        validateStep,
        hasStepErrors,
        clearStepErrors
    };
};
