import { z, ZodTypeAny } from 'zod';

export interface FormFieldMetadataDTO {
    id: string;
    type: string;
    label: string;
    desc?: string;
    placeholder?: string;
    required: boolean;
    stage?: string;
    camundaVariable: string;
    options?: string[];
}

export class ZodBuilder {
    /**
     * Construye dinámicamente un esquema Zod real ejecutable en memoria a partir del arreglo de metadatos de campos visuales.
     */
    static buildSchema(fields: FormFieldMetadataDTO[]): z.ZodObject<any> {
        const shape: Record<string, ZodTypeAny> = {};

        fields.forEach(field => {
            let fieldSchema: ZodTypeAny;

            switch (field.type) {
                case 'text':
                case 'select':
                case 'file': // File uploads often start as string references or base64 in JSON payload
                    fieldSchema = z.string();
                    if (field.required) {
                        fieldSchema = (fieldSchema as z.ZodString).min(1, 'Campo requerido');
                    }
                    break;
                case 'number':
                    fieldSchema = z.number({ invalid_type_error: 'Debe ser un número válido' });
                    break;
                default:
                    fieldSchema = z.any();
            }

            // Si no es requerido, lo hacemos opcional (permite undefined)
            if (!field.required) {
                fieldSchema = fieldSchema.optional();
            }

            // Bind the schema to the actual Camunda variable name if defined, else fallback to standard ID
            const bindingKey = field.camundaVariable || field.id;
            shape[bindingKey] = fieldSchema;
        });

        return z.object(shape);
    }
}
