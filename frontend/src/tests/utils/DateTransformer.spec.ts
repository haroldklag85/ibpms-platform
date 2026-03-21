import { describe, it, expect } from 'vitest';

// Función Util simulada (En producción residiría en src/utils/PayloadTransformer.ts)
const translateDatesToInt = (formData: Record<string, any>): Record<string, any> => {
    const transformed = { ...formData };
    for (const key in transformed) {
        if (transformed[key] instanceof Date) {
            transformed[key] = transformed[key].getTime(); // Time in milliseconds integer
        } else if (typeof transformed[key] === 'string' && transformed[key].match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/)) {
            // ISO String parse fallback
            transformed[key] = new Date(transformed[key]).getTime();
        }
    }
    return transformed;
};

describe('US-007 CA-9: Transformación Frontend de Variables Conservadoras DMN (Iteración 51)', () => {
    
    it('Muta matemáticamente (Casteo) los DatePickers complejos y ISO Strings a Enteros Inmutables (Timestamps) pre-inyección Cognitiva', () => {
        // Objeto Reactivo generado por FormBuilder / Modeler Properties Panel
        const reactiveDmnContext = {
            montoCredito: 5000,
            tipoCliente: 'VIP',
            fechaNacimientoObj: new Date('1990-05-15T00:00:00Z'),
            fechaExpiracionStr: '2027-12-31T23:59:59Z'
        };

        // Mutación utilitaria aislada
        const translatedPayload = translateDatesToInt(reactiveDmnContext);

        // Aserción Matemática 1: Valores primitivos ilesos
        expect(translatedPayload.montoCredito).toBe(5000);
        expect(translatedPayload.tipoCliente).toBe('VIP');

        // Aserción Matemática 2: Fecha de Objeto Date transformada a Int Absoluto (Evitando desajustes TZ en el Backend / Camunda CamundaSpin)
        expect(typeof translatedPayload.fechaNacimientoObj).toBe('number');
        expect(translatedPayload.fechaNacimientoObj).toBe(new Date('1990-05-15T00:00:00Z').getTime());

        // Aserción Matemática 3: Fecha ISO triturada y mutada a Int (Timestamp Millis)
        expect(typeof translatedPayload.fechaExpiracionStr).toBe('number');
        expect(translatedPayload.fechaExpiracionStr).toBe(new Date('2027-12-31T23:59:59Z').getTime());
    });
});
