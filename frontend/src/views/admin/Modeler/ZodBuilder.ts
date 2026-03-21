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
    minRows?: number; // CA-41: Mínimo filas Grilla
    maxRows?: number; // CA-41: Máximo filas Grilla
    isMultiple?: boolean; // CA-45: Multi-select Pastillas
    minFiles?: number; // CA-49: Min adjuntos
    maxFiles?: number; // CA-49: Max adjuntos
    requiredIfField?: string; // CA-48: Condicional Zod Field Name
    requiredIfValue?: string; // CA-48: Condicional Zod Target Value
    disableCondition?: string; // CA-57: Candado de Solo-Lectura Basado en Fórmulas
    timerMode?: 'manual' | 'background' | 'api'; // CA-58: Cronómetro
    columns?: number; // CA-55: Layout Multicolumna
    children?: FormFieldMetadataDTO[]; // CA-8: Recursive Nested Support
    isPII?: boolean; // GAP-1: Shift-Left Security PII
}

export class ZodBuilder {
    /**
     * Construye dinámicamente un esquema Zod real ejecutable en memoria a partir del arreglo de metadatos de campos visuales.
     */
    static buildSchema(fields: FormFieldMetadataDTO[], formRules?: any[]): z.ZodTypeAny {
        const shape: Record<string, ZodTypeAny> = {};

        const flatFields = (arr: FormFieldMetadataDTO[]): FormFieldMetadataDTO[] => {
            let res: FormFieldMetadataDTO[] = [];
            for (const f of arr) {
                if (['container', 'tabs', 'tab_pane', 'accordion', 'accordion_panel'].includes(f.type) && f.children) res = res.concat(flatFields(f.children));
                else if (!['container', 'tabs', 'tab_pane', 'accordion', 'accordion_panel', 'info_modal'].includes(f.type) && !f.type.startsWith('button_')) res.push(f);
            }
            return res;
        };

        flatFields(fields).forEach(field => {
            let fieldSchema: ZodTypeAny;

            if (field.type === 'field_array') {
                const subSchema = ZodBuilder.buildSchema(field.children || []);
                let arrSchema = z.array(subSchema);
                if (field.minRows !== undefined && field.minRows > 0) {
                   arrSchema = arrSchema.min(field.minRows, `Mínimo ${field.minRows} filas requeridas`);
                }
                if (field.maxRows !== undefined && field.maxRows > 0) {
                   arrSchema = arrSchema.max(field.maxRows, `Máximo ${field.maxRows} filas permitidas`);
                }
                shape[field.camundaVariable || field.id] = arrSchema;
                return; // Skip the rest of the loop for field_array
            }

            switch (field.type) {
                case 'checkbox':
                    fieldSchema = z.boolean();
                    break;
                case 'hidden':
                    fieldSchema = z.string();
                    fieldSchema = fieldSchema.optional();
                    break;
                case 'number':
                case 'timer':
                    fieldSchema = z.number({ invalid_type_error: 'Debe ser un número válido' });
                    break;
                case 'text':
                case 'textarea':
                case 'date':
                case 'time':
                case 'radio':
                case 'password':
                case 'email':
                case 'url':
                case 'qr':
                    fieldSchema = z.string();
                    if (field.type === 'email') fieldSchema = (fieldSchema as z.ZodString).email('Debe ser un email válido');
                    if (field.type === 'url') fieldSchema = (fieldSchema as z.ZodString).url('Debe ser una URL válida');
                    
                    if (field.minLength !== undefined && field.minLength > 0) {
                        fieldSchema = (fieldSchema as z.ZodString).min(field.minLength, `Mínimo ${field.minLength} caracteres`);
                    } else if (field.required) {
                        fieldSchema = (fieldSchema as z.ZodString).min(1, 'Campo requerido');
                    }
                    if (field.maxLength !== undefined && field.maxLength > 0) {
                        fieldSchema = (fieldSchema as z.ZodString).max(field.maxLength, `Máximo ${field.maxLength} caracteres`);
                    }
                    break;
                case 'select':
                case 'async_select':
                    if (field.isMultiple) {
                        fieldSchema = z.array(z.string());
                        if (field.required) {
                            fieldSchema = (fieldSchema as z.ZodArray<z.ZodString>).min(1, 'Seleccione al menos una opción');
                        }
                    } else {
                        fieldSchema = z.string();
                        if (field.required) {
                            fieldSchema = (fieldSchema as z.ZodString).min(1, 'Campo requerido');
                        }
                    }
                    break;
                case 'file':
                case 'signature': // CA-31
                    fieldSchema = z.string().uuid({ message: "Se requiere un UUID de Puntero S3" });
                    if (field.required) {
                        fieldSchema = (fieldSchema as z.ZodString).min(1, 'Campo requerido');
                    }
                    break;
                default:
                    fieldSchema = z.any();
            }

            if (!field.required && field.type !== 'checkbox' && !field.isMultiple) { // isMultiple arrays are handled above
                fieldSchema = fieldSchema.optional();
            }

            if (field.isPII) {
            fieldSchema = fieldSchema.describe(JSON.stringify({ isPII: true }));
        }
            const bindingKey = field.camundaVariable || field.id;
            shape[bindingKey] = fieldSchema;
        });
        const baseSchema = z.object(shape);

        if (formRules && formRules.length > 0) {
            return baseSchema.superRefine((data, ctx) => {
                formRules.forEach(rule => {
                    const valA = data[rule.fieldA];
                    const valB = data[rule.fieldB];
                    let isInvalid = false;
                    
                    if (valA !== undefined && valB !== undefined) {
                        // Coerción temporal para fechas si son strings ISO
                        const a = !isNaN(Date.parse(valA)) && isNaN(Number(valA)) ? new Date(valA).getTime() : Number(valA) || valA;
                        const b = !isNaN(Date.parse(valB)) && isNaN(Number(valB)) ? new Date(valB).getTime() : Number(valB) || valB;

                        switch (rule.operator) {
                            case '>': isInvalid = a <= b; break;
                            case '<': isInvalid = a >= b; break;
                            case '==': isInvalid = a !== b; break;
                            case '!=': isInvalid = a === b; break;
                        }
                    }

                    if (isInvalid) {
                        ctx.addIssue({
                            code: z.ZodIssueCode.custom,
                            message: rule.errorMessage || 'Validación cruzada fallida',
                            path: [rule.fieldA]
                        });
                    }
                });
            });
        }

        return baseSchema;
    }
}
