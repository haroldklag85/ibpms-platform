export type FieldType = 'string' | 'number' | 'boolean' | 'date' | 'select' | 'radio' | 'group' | 'array';

export interface SelectOption {
    label: string;
    value: string | number;
}

export interface FieldMetadata {
    placeholder?: string;
    min?: number;
    max?: number;
    minLength?: number;
    maxLength?: number;
    pattern?: string;
    customClass?: string;
}

export interface FormField {
    key: string;
    label: string;
    type: FieldType;
    required?: boolean;
    disabled?: boolean;
    defaultValue?: any;
    options?: SelectOption[]; // Solo para selects o radios
    metadata?: FieldMetadata;
    fields?: FormField[]; // Solo para tipos 'group' o 'array' (recursividad)
}

export interface FormSchema {
    formId: string;
    title?: string;
    description?: string;
    fields: FormField[];
}
