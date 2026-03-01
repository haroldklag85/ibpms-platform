export interface KanbanItem {
    id: string;
    title: string;
    status: string; // Columna a la que pertenece
    assignee?: string;
    priority?: number;
}

export interface KanbanColumnDef {
    id: string; // e.g., 'TODO', 'DOING', 'DONE'
    title: string;
    color: string;
}

export interface KanbanBoard {
    boardId: string;
    columns: KanbanColumnDef[];
    items: KanbanItem[];
}
