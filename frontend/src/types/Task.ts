/**
 * DTO que representa una tarea humana devolvidade por el Motor
 */
export interface TaskDto {
    id: string;
    name: string;
    assignee: string | null;
    created: string;
    due: string | null;
    processDefinitionId: string;
    processInstanceId: string;
    tenantId: string | null;
    description: string | null;
    priority: number;
}

/**
 * Paginación Estándar opcional para listas largas
 */
export interface TaskListRequest {
    assignee?: string;
    candidateGroup?: string;
    active?: boolean;
    sortBy?: string;
    sortOrder?: 'asc' | 'desc';
    firstResult?: number;
    maxResults?: number;
}
