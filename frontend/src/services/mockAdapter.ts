import MockAdapter from 'axios-mock-adapter';
import { AxiosInstance } from 'axios';

export const setupMockAdapter = (apiClient: AxiosInstance) => {
    // Activamos el mock global sobre la instancia de apiClient
    const mock = new MockAdapter(apiClient, { delayResponse: 600 });

    // 0. Workdesk / Bandeja de Tareas
    mock.onGet('/tareas/candidatas').reply(200, [
        { id: 'T-002', name: 'Auditoría Legal Incidente', processDefinitionId: 'LEGAL-AUDIT', priority: 80, created: new Date().toISOString() },
        { id: 'T-003', name: 'Carga de Documentos', processDefinitionId: 'SGDEA-INBOX', priority: 40, created: new Date().toISOString() }
    ]);

    mock.onGet('/tareas').reply(200, [
        { id: 'T-001', name: 'Revisar Nómina Enero', processDefinitionId: 'HR-PAYROLL', priority: 30, created: new Date().toISOString() },
        { id: 'T-004', name: 'Envío Tarjeta Crédito', processDefinitionId: 'REQ-CARD', priority: 20, created: new Date().toISOString() }
    ]);

    mock.onPost(/\/tareas\/.*\/claim/).reply(200, { status: 'CLAIMED' });
    mock.onPost(/\/tareas\/.*\/unclaim/).reply(200, { status: 'UNCLAIMED' });
    mock.onPost(/\/tareas\/.*\/reassign/).reply(200, { status: 'REASSIGNED' });

    mock.onGet('/users/peers').reply(200, [
        { id: 'user-001', name: 'Ana García', role: 'Operador SAC' },
        { id: 'user-002', name: 'Carlos Mendoza', role: 'Líder SAC' }
    ]);

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

    // 12. SAC Config Manager (Epic 13)
    mock.onPost('/api/v1/mailboxes/test-connection').reply((config) => {
        const payload = JSON.parse(config.data);
        if (payload.rawClientSecret === 'fail') {
            return [400, { error: 'ConnectionValidationException', message: 'MS Graph rejected the credentials' }];
        }
        return [200, { status: 'SUCCESS', message: 'Conexión a MS Graph validada.' }];
    });

    mock.onPost('/api/v1/mailboxes').reply(() => {
        return [201, { id: `MBOX-${Math.floor(Math.random() * 1000)}` }];
    });

    mock.onGet('/api/v1/mailboxes').reply(() => {
        return [200, [
            {
                id: 'MBOX-1',
                alias: 'Soporte Nivel 1',
                protocol: 'GRAPH',
                tenantId: 'org.onmicrosoft.com',
                clientId: 'appid-xxx-xxx',
                defaultBpmnProcessId: 'process_support_triage',
                active: true,
                createdAt: new Date().toISOString()
            }
        ]];
    });

    mock.onPatch(/\/api\/v1\/mailboxes\/.*\/status/).reply((config) => {
        const { active } = JSON.parse(config.data);
        return [200, { id: config.url?.split('/')[4], active }];
    });

    // 13. Pantalla 10.B (Gantt Execution & Resource Planning)
    mock.onGet(/\/api\/v1\/execution\/projects\/.*\/gantt-tree/).reply(() => {
        return [200, [
            {
                id: "task-1",
                projectId: "proj-123",
                name: "Análisis de Requisitos",
                status: "DONE",
                progress: 100,
                assigneeUserId: "user-001",
                actualBudget: 1500,
                start: "2026-04-01",
                end: "2026-04-05",
                dependencies: ""
            },
            {
                id: "task-2",
                projectId: "proj-123",
                name: "Diseño de Arquitectura",
                status: "IN_PROGRESS",
                progress: 40,
                assigneeUserId: "user-002",
                actualBudget: null,
                start: "2026-04-06",
                end: "2026-04-12",
                dependencies: "task-1"
            },
            {
                id: "task-3",
                projectId: "proj-123",
                name: "Desarrollo Backend",
                status: "PENDING",
                progress: 0,
                assigneeUserId: null,
                actualBudget: null,
                start: "2026-04-13",
                end: "2026-04-25",
                dependencies: "task-2"
            }
        ]];
    });

    mock.onPut(/\/api\/v1\/execution\/projects\/tasks\/.*\/assign/).reply((config) => {
        return [204];
    });

    // 14. Pantalla 8 (Project Template Builder - Epic 8)
    mock.onGet(/\/api\/v1\/design\/projects\/templates\/.*$/).reply(() => {
        return [200, {
            id: "tpl-001",
            name: "Plantilla Standard Construcción",
            description: "Template OOTB para obras civiles",
            status: "DRAFT", // Puede ser PUBLISHED
            phases: [
                {
                    id: "phase-1",
                    name: "Fase 1: Preparación",
                    orderIndex: 0,
                    milestones: [
                        {
                            id: "ms-1",
                            name: "Cimientos y Terreno",
                            orderIndex: 0,
                            isStageGate: true,
                            tasks: [
                                {
                                    id: "task-001",
                                    name: "Estudio Topográfico",
                                    estimatedHours: 40,
                                    formKey: "form_topografia",
                                    orderIndex: 0
                                },
                                {
                                    id: "task-002",
                                    name: "Excavación Primaria",
                                    estimatedHours: 120,
                                    formKey: null, // UX Defensiva debe detectarlo
                                    orderIndex: 1
                                }
                            ]
                        }
                    ]
                }
            ],
            dependencies: []
        }];
    });

    mock.onPost(/\/api\/v1\/design\/projects\/templates$/).reply((config) => {
        const payload = JSON.parse(config.data);
        return [201, payload];
    });

    // 15. Pantalla 1 (Epic 1 - Hybrid Workdesk US-001)
    mock.onGet(/\/api\/v1\/workdesk\/global-inbox/).reply((config) => {
        // Obtenemos param de url o simulamos
        // const params = config.params || { page: 0, size: 50 };
        return [200, {
            content: [
                {
                    unifiedId: "BPMN-9a8b7c",
                    sourceSystem: "BPMN",
                    originalTaskId: "9a8b7c",
                    title: "Aprobación Legal: Contrato ACME",
                    slaExpirationDate: new Date(Date.now() - 3600000).toISOString(), // Expirado hace 1 hr
                    status: "URGENT",
                    assignee: "maria.lopez"
                },
                {
                    unifiedId: "KANBAN-3f2d1a",
                    sourceSystem: "KANBAN",
                    originalTaskId: "3f2d1a",
                    title: "Desarrollo de API Rest",
                    slaExpirationDate: new Date(Date.now() + 86400000).toISOString(), // Vence en 1 día
                    status: "PENDING",
                    assignee: "carlos.dev"
                },
                {
                    unifiedId: "BPMN-1c2b3a",
                    sourceSystem: "BPMN",
                    originalTaskId: "1c2b3a",
                    title: "Revisión Técnica - Componente UI",
                    slaExpirationDate: new Date(Date.now() + 604800000).toISOString(), // Vence en 7 días
                    status: "NORMAL",
                    assignee: null
                }
            ],
            pageable: {
                pageNumber: 0,
                pageSize: 50,
                totalElements: 3
            }
        }];
    });

    return mock;
};
