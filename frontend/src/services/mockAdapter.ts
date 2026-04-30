import MockAdapter from 'axios-mock-adapter';
import { AxiosInstance } from 'axios';

export const setupMockAdapter = (apiClient: AxiosInstance) => {
    // Activamos el mock global sobre la instancia de apiClient
    const mock = new MockAdapter(apiClient, { delayResponse: 600 });

    // [REMOVIDO PARA UAT REAL J-04] 0. Workdesk / Bandeja de Tareas
    /* mock.onGet('/tareas/candidatas').reply(200, [ ... ]); */
    /* mock.onGet('/tareas').reply(200, [ ... ]); */
    /* mock.onPost(/\/tareas\/.*\/claim/).reply( ... ); */

    mock.onGet('/users/peers').reply(200, [
        { id: 'user-001', name: 'Ana García', role: 'Operador SAC' },
        { id: 'user-002', name: 'Carlos Mendoza', role: 'Líder SAC' }
    ]);

    // 1. AI Correct (Partial Regeneration CA-28)
    mock.onPost('/ai/correct').reply((config) => {
        const { delta } = JSON.parse(config.data);
        return [200, { correctedText: `[IA Regenerado vía Mock: ${delta}]` }];
    });

    // [REMOVIDO PARA UAT REAL J-04] US-002: Workbox Tasks
    /* mock.onPost(/\/api\/v1\/workbox\/tasks\/.*\/claim/).reply(...) */
    /* mock.onPost(/\/api\/v1\/workbox\/tasks\/.*\/complete/).reply(...) */
    /* mock.onPut(/\/api\/v1\/workbox\/tasks\/.*\/draft/).reply(...) */
    // US-007: DMN Generate
    mock.onPost('/api/v1/dmn/generate').reply(() => {
        return [200, { dmnXml: "<?xml version='1.0'?><definitions id='dmn_mock'></definitions>", explanation: "Reglas autogeneradas vía Mock" }];
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

    // [REMOVIDO PARA UAT REAL J-04] 9. Kanban Status Update (Pantalla 3)
    /* mock.onPatch(/\/kanban\/items\/.*\/status/).reply(...) */

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

    // [REMOVIDO PARA UAT REAL J-04] 15. Pantalla 1 (Epic 1 - Hybrid Workdesk US-001)
    /* mock.onGet(/\/workdesk\/global-inbox/).reply(...) */

    // 16. Sprint 5 - Iteración 2: Timebox & SLA
    mock.onGet(/\/api\/v1\/agile\/tasks\/.*\/sla-log/).reply(() => {
        return [200, {
            content: [
                { id: 'log-1', taskId: 'taskId', requestSlaStart: '2026-04-10T10:00:00Z', grantedSlaEnd: '2026-04-12T10:00:00Z', reason: 'Falta de documentos', changedBy: 'operator-1' }
            ],
            pageable: { pageNumber: 0, pageSize: 20 },
            totalElements: 1
        }];
    });

    const idempotencyCache = new Set<string>();
    mock.onPost(/\/api\/v1\/agile\/tasks\/.*\/timebox/).reply((config) => {
        const idempotencyKey = config.headers ? config.headers['Idempotency-Key'] : null;
        if (idempotencyKey && idempotencyCache.has(idempotencyKey)) {
            // QA Scenario: Simulamos que una key ya usada retiene idempotencia y falla o avisa
            return [409, { code: 'IDEMPOTENCY_CONFLICT', message: 'Doble sumisión detectada. Se retiene idempotencia.' }];
        }
        if (idempotencyKey) {
            idempotencyCache.add(idempotencyKey);
        }
        
        return [200, { status: 'TIMEBOX_EXTENDED', newDueDate: new Date(Date.now() + 86400000).toISOString() }];
    });

    // ═══════════════════════════════════════════════════════════════════
    // R2: Mocks Faltantes para Cabecera BpmnDesigner y Pantallas Admin
    // ═══════════════════════════════════════════════════════════════════

    // Auth: Effective Roles & Token Refresh
    mock.onGet('/auth/effective-roles').reply(200, ['ROLE_USER', 'ROLE_APPROVER', 'ROLE_SUPER_ADMIN', 'Global Admin']);
    mock.onPost('/auth/refresh').reply(200, { token: localStorage.getItem('ibpms_token') || 'MOCK_REFRESHED_JWT' });

    // BPMN Process Versions (CA-6)
    mock.onGet(/\/design\/processes\/.*\/versions/).reply(200, [
        { version: 3, deployedBy: 'Harolt Gómez', deployedAt: new Date(Date.now() - 86400000).toISOString(), comment: 'Agregado gateway de aprobación' },
        { version: 2, deployedBy: 'Ana García', deployedAt: new Date(Date.now() - 172800000).toISOString(), comment: 'Refactorización de lanes' },
        { version: 1, deployedBy: 'System', deployedAt: new Date(Date.now() - 604800000).toISOString(), comment: 'Versión inicial' }
    ]);

    // BPMN Process Lock (CA-7 / CA-66)
    mock.onGet(/\/design\/processes\/.*\/lock/).reply(200, { active: false, owner: null, since: null });
    mock.onPost(/\/design\/processes\/.*\/lock\/heartbeat/).reply(204);
    mock.onDelete(/\/design\/processes\/.*\/lock\/force/).reply(200, { status: 'UNLOCKED' });

    // BPMN Audit Logs (CA-42)
    mock.onGet(/\/design\/processes\/.*\/audit-logs/).reply(200, [
        { action: 'IMPORT_XML', user: 'Harolt Gómez', date: new Date(Date.now() - 3600000).toISOString(), version: 3, details: 'Importación manual desde archivo' },
        { action: 'VALIDATE', user: 'System', date: new Date(Date.now() - 7200000).toISOString(), version: 3, details: 'Pre-flight completado sin errores' },
        { action: 'REQUEST_DEPLOY', user: 'Ana García', date: new Date(Date.now() - 14400000).toISOString(), version: 2, details: 'Solicitud enviada a Release Manager' },
        { action: 'DEPLOY', user: 'Carlos R.', date: new Date(Date.now() - 86400000).toISOString(), version: 2, details: 'Desplegado con estrategia COEXIST' }
    ]);

    // BPMN Deploy Requests (CA-69)
    mock.onGet(/\/design\/processes\/.*\/deploy-requests/).reply(200, [
        { id: 'DR-001', requestedBy: 'Ana García', requestedAt: new Date(Date.now() - 3600000).toISOString(), status: 'PENDING', comment: 'Listo para producción' }
    ]);
    mock.onPost(/\/design\/deploy-requests\/.*\/approve/).reply(200, { status: 'APPROVED' });
    mock.onPost(/\/design\/deploy-requests\/.*\/reject/).reply(200, { status: 'REJECTED' });

    // BPMN Request Deployment (CA-25)
    mock.onPost(/\/design\/processes\/.*\/request-deployment/).reply(201, { id: 'DR-002', status: 'PENDING' });

    // BPMN Deploy (multipart)
    mock.onPost('/design/processes/deploy').reply(200, {
        deployment_id: 'DEP-MOCK-' + Date.now(),
        version: 4,
        deployed_at: new Date().toISOString(),
        generatedRoles: ['Lane_Operador', 'Lane_Supervisor']
    });

    // BPMN Validate
    mock.onPost('/design/processes/validate').reply(200, { valid: true, warnings: [], errors: [] });

    // BPMN Catalog & Templates
    mock.onGet('/design/processes/catalog').reply(200, [
        { id: 'proc-001', name: 'Crédito de Consumo', version: 3, status: 'ACTIVO', deployedAt: new Date().toISOString() },
        { id: 'proc-002', name: 'Onboarding Jurídico', version: 1, status: 'BORRADOR', deployedAt: null },
        { id: 'proc-003', name: 'Reclamación Seguros', version: 2, status: 'ACTIVO', deployedAt: new Date().toISOString() }
    ]);
    mock.onGet('/design/processes/templates').reply(200, [
        { id: 'tpl-credit', name: 'Plantilla Crédito Estándar', xml: null },
        { id: 'tpl-onboard', name: 'Plantilla Onboarding', xml: null }
    ]);

    // BPMN Archive (CA-32)
    mock.onPost(/\/design\/processes\/.*\/archive/).reply(200, { status: 'ARCHIVED' });

    // BPMN Rollback (CA-15)
    mock.onPost(/\/design\/processes\/.*\/rollback\/.*/).reply(200, { status: 'RESTORED' });

    // BPMN Sandbox Spawn (CA-41) — la ruta real que usa runSandbox()
    mock.onPost('/design/processes/sandbox-spawn').reply(200, { status: 'SIMULATED', logId: 'SIM-' + Date.now(), executionTime: '1.2s' });

    // BPMN Variables (CA-17 / CA-49)
    mock.onGet(/\/design\/processes\/.*\/variables/).reply(200, [
        { name: 'montoSolicitado', type: 'Double', defaultValue: '0.0' },
        { name: 'clienteId', type: 'String', defaultValue: '' },
        { name: 'aprobado', type: 'Boolean', defaultValue: 'false' },
        { name: 'scoreRiesgo', type: 'Integer', defaultValue: '0' }
    ]);

    // Integration Connectors (CA-45 / CA-49)
    mock.onGet('/integrations/connectors').reply(200, [
        { id: 'rest-generic', name: 'REST Genérico', type: 'REST', icon: '🌐' },
        { id: 'soap-legacy', name: 'SOAP Legacy', type: 'SOAP', icon: '📦' },
        { id: 'graph-o365', name: 'Microsoft Graph', type: 'GRAPH', icon: '🔷' },
        { id: 'smtp-email', name: 'Email SMTP', type: 'SMTP', icon: '📧' }
    ]);
    mock.onGet(/\/integrations\/connectors\/.*\/schema/).reply(200, {
        inputs: [{ name: 'url', type: 'string', required: true }, { name: 'method', type: 'enum', values: ['GET', 'POST', 'PUT', 'DELETE'] }],
        outputs: [{ name: 'responseBody', type: 'object' }, { name: 'statusCode', type: 'integer' }]
    });

    // External Task Topics (CA-70)
    mock.onGet('/design/external-task-topics').reply(200, [
        'topic-email-notification',
        'topic-document-generation',
        'topic-score-calculation',
        'topic-sap-integration'
    ]);

    // DMN Definitions (Sprint 6.1 CA-12)
    mock.onGet('/dmn-models/definitions').reply(200, [
        { id: 'dmn-scoring-v1', name: 'Scoring Crédito', version: 1 },
        { id: 'dmn-tarifario-v2', name: 'Tarifario Productos', version: 2 },
        { id: 'dmn-riesgo-v1', name: 'Evaluación de Riesgo', version: 1 },
        { id: 'dmn-elegibilidad-v3', name: 'Elegibilidad Cliente', version: 3 }
    ]);

    // Forms (CA-30)
    mock.onGet('/forms').reply(200, [
        { id: 'form-001', name: 'Formulario Solicitud Crédito', type: 'MAESTRO', version: 2 },
        { id: 'form-002', name: 'Formulario Aprobación Rápida', type: 'SIMPLE', version: 1 },
        { id: 'form-003', name: 'Checklist Documentos', type: 'SIMPLE', version: 1 }
    ]);
    mock.onGet(/\/forms\/.*\/versions/).reply(200, [
        { version: 2, createdAt: new Date().toISOString(), createdBy: 'Harolt Gómez' },
        { version: 1, createdAt: new Date(Date.now() - 604800000).toISOString(), createdBy: 'System' }
    ]);
    mock.onPost(/\/forms\/.*/).reply(200, { status: 'SAVED', version: 3 });

    // Admin Settings — BPMN Complexity Limit (CA-30)
    mock.onGet('/admin/settings/bpmn-complexity-limit').reply(200, { limit: 100 });

    // [REMOVIDO PARA UAT REAL J-04] Kanban Board (Sprint 6.1 B2)
    /* mock.onGet('/kanban/board').reply(...) */

    // Data Mappings (CA-68)
    mock.onPost(/\/design\/processes\/.*\/tasks\/.*\/mappings/).reply(200, { status: 'SAVED' });

    // Incidents DRP (CA-13)
    mock.onGet('/admin/incidents').reply(200, [
        { id: 'INC-001', type: 'EXECUTION_ERROR', processKey: 'Crédito Consumo', message: 'NullPointerException en ServiceTask', timestamp: new Date().toISOString(), retryable: true },
        { id: 'INC-002', type: 'TIMEOUT', processKey: 'Onboarding', message: 'Timer SLA expirado sin resolución', timestamp: new Date().toISOString(), retryable: false }
    ]);
    mock.onPost(/\/admin\/incidents\/.*\/retry/).reply(200, { status: 'RETRIED' });
    mock.onDelete(/\/admin\/incidents\/.*/).reply(200, { status: 'ABORTED' });

    // AI Copilot Session (CA-04)
    mock.onDelete('/ai/copilot/session').reply(204);
    mock.onPost(/\/ai\/copilot\/bpmn\/.*/).reply(200, { analysis: 'Cumplimiento ISO 9001 al 92%. Recomendación: agregar gateway de escalamiento.' });

    // Forensics ISO Override (CA-09)
    mock.onPost('/forensics/iso-override').reply(201, { logged: true });

    // Menu Layout (CA-6 — el endpoint real que MainLayout intentará llamar primero)
    mock.onGet('/api/v1/menu-layout').reply(404);

    // Habilitar PassThrough para Auth UAT
    mock.onPost('/auth/emergency-login').passThrough();
    mock.onAny().passThrough();

    return mock;
};
