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
    visibilityCondition?: string; // CA-25: Visibilidad Condicional Dinámica Eval
    enableAuditLog?: boolean; // CA-28: Auditoría Conf
    asyncUrl?: string; // CA-30: Endpoint de recolección asyncrona Typeahead
    tooltipText?: string; // CA-35: Ayudantes locales
    mask?: string; // CA-36: Máscara visual de Input
    minLength?: number; // CA-38: Zod string lengths
    maxLength?: number; // CA-38: Zod string lengths
    maxSizeMb?: number; // CA-39: Peso máximo archivo
    allowedExts?: string; // CA-39: Tipos permitidos
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
                case 'async_select':
                case 'radio':
                case 'date':
                case 'time':
                case 'file':
                case 'signature': // CA-31
                    fieldSchema = z.string();
                    if (field.minLength !== undefined && field.minLength > 0) {
                        fieldSchema = (fieldSchema as z.ZodString).min(field.minLength, `Mínimo ${field.minLength} caracteres`);
                    } else if (field.required) {
                        fieldSchema = (fieldSchema as z.ZodString).min(1, 'Campo requerido');
                    }
                    if (field.maxLength !== undefined && field.maxLength > 0) {
                        fieldSchema = (fieldSchema as z.ZodString).max(field.maxLength, `Máximo ${field.maxLength} caracteres`);
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
