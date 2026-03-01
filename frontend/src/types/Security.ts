export interface AppRole {
    roleId: string; // Ej: ROL_APROBADOR
    name: string;
    permissions: string[]; // READ_TASK, CLAIM_TASK, etc.
}

export interface UserProfile {
    userId: string;
    email: string;
    fullName: string;
    department: string;
    assignedRoles: string[]; // Lista de RoleIds
    isActive: boolean;
}
