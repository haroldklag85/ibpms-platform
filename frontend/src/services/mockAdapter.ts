import MockAdapter from 'axios-mock-adapter';
import apiClient from './apiClient';

// Activamos el mock global sobre la instancia de apiClient
const mock = new MockAdapter(apiClient, { delayResponse: 600 });

// 1. AI Correct (Partial Regeneration CA-28)
mock.onPost('/ai/correct').reply((config) => {
    const { delta } = JSON.parse(config.data);
    return [200, { correctedText: `[IA Regenerado vía Mock: ${delta}]` }];
});

// 2. Service Delivery (Pantalla 16)
mock.onPost('/service-delivery/manual-start').reply(() => {
    return [201, { id: `PROC-MOCK-${Math.floor(Math.random() * 10000)}`, status: 'CREATED' }];
});

// 3. Customer 360 (Pantalla 17)
mock.onGet(/\/customers\/.*\/360/).reply((config) => {
    const crmId = config.url?.split('/')[2] || 'CRM-DEV';
    return [200, {
        id: crmId,
        name: 'Corporación iBPMS Mock',
        email: 'test@ibpms-mock.co',
        segment: 'Enterprise B2B',
        lastInteraction: 'Reciente',
        activeCases: [
            { id: 'TRM-M01', service: 'Migración a Nube', status: 'En Progreso', assignee: 'DevBot' },
            { id: 'TRM-M02', service: 'Auditoría', status: 'En Riesgo', assignee: 'DevBot' }
        ]
    }];
});

// 4. Project Templates (Pantalla 8)
mock.onPost('/projects/templates').reply(() => {
    return [201, { id: 'TPL-MOCK-001', status: 'SAVED' }];
});

// 5. BPMN Draft (Pantalla 6)
mock.onPut(/\/design\/processes\/.*\/draft/).reply(() => {
    return [200, { status: 'DRAFT_SAVED' }];
});

// 6. BPMN Sandbox (Pantalla 6)
mock.onPost(/\/design\/processes\/.*\/sandbox/).reply(() => {
    return [200, { status: 'SIMULATED', logId: 'SIM-999' }];
});

// 7. BAM Analytics - Process Health (Pantalla 5)
mock.onGet('/analytics/process-health').reply(() => {
    return [200, [
        { name: 'Crédito de Consumo', activeInstances: 142, avgSlaStatus: 'Verde', errorRate: '1.2%' },
        { name: 'Onboarding Jurídico', activeInstances: 58, avgSlaStatus: 'Amarillo', errorRate: '4.5%' },
        { name: 'Aprobación Proveedores', activeInstances: 91, avgSlaStatus: 'Rojo', errorRate: '8.1%' }
    ]];
});

// 8. BAM Analytics - AI Metrics (Pantalla 5)
mock.onGet('/analytics/ai-metrics').reply(() => {
    return [200, {
        generatedDocuments: 1542,
        autoApprovals: 430,
        costSaved: 8400,
        averageTokenUsage: 1250,
        confidenceScore: 0.94
    }];
});

// 9. Kanban Status Update (Pantalla 3)
mock.onPatch(/\/kanban\/items\/.*\/status/).reply(() => {
    return [200, { status: 'UPDATED' }];
});

// 10. AI DMN Translate (Pantalla 4/15)
mock.onPost('/ai/dmn/translate').reply(() => {
    return [200, {
        confidence: '99.9%',
        rules: [
            { condition: 'Mock Condition', output: 'Mock Action' }
        ]
    }];
});

// 11. Public Tracking (Pantalla 18)
mock.onGet(/\/public\/tracking\/.*/).reply((config) => {
    const code = config.url?.split('/').pop() || 'XXX';
    return [200, {
        trackingCode: code,
        currentPhase: { name: 'Validación Mock', percentage: 45 },
        status: 'EN_PROGRESO',
        history: [
            { date: new Date().toISOString(), event: 'Ingreso al Sistema' }
        ]
    }];
});

export default mock;
