import { z } from 'zod';

export interface FormFieldSchema {
    id: string;
    type: 'TEXT' | 'NUMBER' | 'BOOLEAN' | 'SELECT';
    label: string;
    required?: boolean;
    options?: { label: string; value: string }[];
    minLength?: number;
    maxLength?: number;
}

export interface FormTemplate {
    formId: string;
    fields: FormFieldSchema[];
}

export class FormEngineService {
    /**
     * Mapea un template JSON a un Schema ejecutable de Zod de manera dinámica.
     * US-003: Validador Cero-UI
     */
    static buildZodSchema(template: FormTemplate): z.ZodObject<any, any, any> {
        const schemaShape: Record<string, z.ZodTypeAny> = {};

        for (const field of template.fields) {
            let fieldSchema: z.ZodTypeAny;

            switch (field.type) {
                case 'TEXT':
                    let textSchema = z.string();
                    if (field.minLength) textSchema = textSchema.min(field.minLength, `Mínimo ${field.minLength} caracteres`);
                    if (field.maxLength) textSchema = textSchema.max(field.maxLength, `Máximo ${field.maxLength} caracteres`);
                    fieldSchema = textSchema;
                    break;
                case 'NUMBER':
                    fieldSchema = z.number({ invalid_type_error: "Debe ser un número" });
                    break;
                case 'BOOLEAN':
                    fieldSchema = z.boolean({ invalid_type_error: "Debe ser booleano" });
                    break;
                case 'SELECT':
                    const optionValues = field.options?.map(o => o.value) || [];
                    if (optionValues.length > 0) {
                        fieldSchema = z.enum(optionValues as [string, ...string[]]);
                    } else {
                        fieldSchema = z.string();
                    }
                    break;
                default:
                    fieldSchema = z.any();
                    break;
            }

            if (!field.required) {
                fieldSchema = fieldSchema.optional().nullable();
            }

            schemaShape[field.id] = fieldSchema;
        }

        return z.object(schemaShape);
    }
}
