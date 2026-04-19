export enum WebSocketEventType {
    TASK_CLAIMED = 'TASK_CLAIMED',
    TASK_UNCLAIMED = 'TASK_UNCLAIMED',
    TASK_COMPLETED = 'TASK_COMPLETED',
    TASK_EXPIRED = 'TASK_EXPIRED',
    TASK_POOL_REFRESH = 'TASK_POOL_REFRESH',
    TASKS_BULK_UPDATED = 'TASKS_BULK_UPDATED',
    TASK_FORCE_UNCLAIMED = 'TASK_FORCE_UNCLAIMED'
}

export interface WsMessage {
    eventType: WebSocketEventType;
    taskId?: string;
    payload?: any;
    message?: string;
}
