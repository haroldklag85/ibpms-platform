export const TENANTS = {
  ALPHA: { id: 'tenant_alpha', name: 'Alpha Corp', domain: 'alpha.com' },
  BETA: { id: 'tenant_beta', name: 'Beta Inc', domain: 'beta.com' },
} as const;

export const USERS = {
  ADMIN_ALPHA: { 
    email: 'admin@alpha.com', 
    password: 'Test123!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_SUPER_ADMIN'] 
  },
  ANALISTA_N1: { 
    email: 'analista_n1@alpha.com', 
    password: 'Test123!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_OPERARIO'] 
  },
  PERITO_A: {
    email: 'perito_a@alpha.com',
    password: 'Test123!',
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_OPERARIO']
  },
  PERITO_B: {
    email: 'perito_b@alpha.com',
    password: 'Test123!',
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_OPERARIO']
  },
  DIRECTOR_1: {
    email: 'director_1@alpha.com',
    password: 'Test123!',
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_SUPERVISOR']
  },
  VIP_DIRECTOR: {
    email: 'vip_director@alpha.com',
    password: 'Test123!',
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_ALTA_DIRECCION']
  }
} as const;


export const API = {
  BASE_URL: 'http://localhost:8080',
  COPILOT_SESSION: '/api/v1/ai/copilot/session',
  WEBHOOK_LEGACY: '/inbound/email-webhook',
  WEBHOOK_NEW: '/intake/webhook',
  KANBAN_BOARDS: '/api/v1/kanban-tasks/boards',
  KANBAN_TASK_STATE: '/api/v1/kanban-tasks/tasks', // + /{id}/state
  KILL_SWITCH: '/api/v1/admin/users', // + /{userId}/revoke-session
} as const;
