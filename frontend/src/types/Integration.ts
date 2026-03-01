export type WebhookDirection = 'INBOUND' | 'OUTBOUND';

export interface WebhookConfig {
    id: string;
    name: string;
    direction: WebhookDirection;
    targetUrl?: string; // Solo requerido en OUTBOUND
    triggerEvent?: string; // Ej: "Process_Started", "Task_Completed"
    processDefinitionKey?: string; // A qué proceso pertenece
    status: 'ACTIVE' | 'DISABLED';
    secretToken: string; // Enmascarado "****"
}
