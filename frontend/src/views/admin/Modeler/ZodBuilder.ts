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
    isPrefilled?: boolean; // CA-12: Data Binding I/O
    isOutputToken?: boolean; // CA-13: Data Binding I/O
    children?: FormFieldMetadataDTO[]; // CA-8: Recursive Nested Support
}

export class ZodBuilder {
    /**
     * Construye dinámicamente un esquema Zod real ejecutable en memoria a partir del arreglo de metadatos de campos visuales.
     */
    static buildSchema(fields: FormFieldMetadataDTO[]): z.ZodObject<any> {
        const shape: Record<string, ZodTypeAny> = {};

        const flatFields = (arr: FormFieldMetadataDTO[]): FormFieldMetadataDTO[] => {
            let res: FormFieldMetadataDTO[] = [];
            for (const f of arr) {
                if (f.type === 'container' && f.children) res = res.concat(flatFields(f.children));
                else if (f.type !== 'container' && !f.type.startsWith('button_')) res.push(f);
            }
            return res;
        };

        flatFields(fields).forEach(field => {
            let fieldSchema: ZodTypeAny;

            switch (field.type) {
                case 'checkbox':
                    fieldSchema = z.boolean();
                    break;
                case 'number':
                    fieldSchema = z.number({ invalid_type_error: 'Debe ser un número válido' });
                    break;
                case 'text':
                case 'textarea':
                case 'select':
                case 'radio':
                case 'date':
                case 'time':
                case 'file':
                    fieldSchema = z.string();
                    if (field.required) {
                        fieldSchema = (fieldSchema as z.ZodString).min(1, 'Campo requerido');
                    }
                    break;
                default:
                    fieldSchema = z.any();
            }

            if (!field.required && field.type !== 'checkbox') {
                fieldSchema = fieldSchema.optional();
            }

            const bindingKey = field.camundaVariable || field.id;
            shape[bindingKey] = fieldSchema;
        });

        return z.object(shape);
    }
}
