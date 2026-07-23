import { describe, it, expect } from 'vitest';
import { FormEngineService, FormTemplate } from '@/services/FormEngineService';

describe('FormEngineService - Parseador JSON a Zod (Shift-Left Test US-003)', () => {
    
    it('Debe crear un esquema Zod válido y aceptar un JSON que cumpla todas las reglas', () => {
        const mockTemplate: FormTemplate = {
            formId: 'TEST-FORM-1',
            fields: [
                { id: 'firstName', type: 'TEXT', label: 'Nombre', required: true, minLength: 2 },
                { id: 'age', type: 'NUMBER', label: 'Edad', required: false },
                { id: 'acceptTerms', type: 'BOOLEAN', label: 'Acepta Términos', required: true },
                { id: 'country', type: 'SELECT', label: 'País', required: true, options: [{label: 'Colombia', value: 'CO'}, {label: 'México', value: 'MX'}] }
            ]
        };

        const schema = FormEngineService.buildZodSchema(mockTemplate);

        const payloadCorrecto = {
            firstName: 'Ana',
            age: 25,
            acceptTerms: true,
            country: 'CO'
        };

        const result = schema.safeParse(payloadCorrecto);
        expect(result.success).toBe(true);
    });

    it('Debe rechazar payloads que no cumplan con el atributo required', () => {
        const mockTemplate: FormTemplate = {
            formId: 'TEST-FORM-2',
            fields: [
                { id: 'firstName', type: 'TEXT', label: 'Nombre', required: true }
            ]
        };

        const schema = FormEngineService.buildZodSchema(mockTemplate);

        const payloadIncompleto = {};

        const result = schema.safeParse(payloadIncompleto);
        expect(result.success).toBe(false);
    });

    it('Debe rechazar longitudes de texto inválidas según minLength', () => {
        const mockTemplate: FormTemplate = {
            formId: 'TEST-FORM-3',
            fields: [
                { id: 'shortName', type: 'TEXT', label: 'Nombre', required: true, minLength: 5 }
            ]
        };

        const schema = FormEngineService.buildZodSchema(mockTemplate);

        // 'Ana' tiene 3 caracteres, el mínimo es 5.
        const result = schema.safeParse({ shortName: 'Ana' });
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues[0].message).toContain('Mínimo 5');
        }
    });

    it('Debe forzar validación estricta enum en los inputs SELECT', () => {
        const mockTemplate: FormTemplate = {
            formId: 'TEST-FORM-4',
            fields: [
                { id: 'country', type: 'SELECT', label: 'País', required: true, options: [{label: 'Colombia', value: 'CO'}] }
            ]
        };

        const schema = FormEngineService.buildZodSchema(mockTemplate);

        // 'AR' no es una opción válida
        const result = schema.safeParse({ country: 'AR' }); 
        expect(result.success).toBe(false);
    });
});
