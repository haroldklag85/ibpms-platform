export const TENANTS = {
  ALPHA: { id: 'tenant_alpha', name: 'Alpha Corp', domain: 'alpha.com' },
  BETA: { id: 'tenant_beta', name: 'Beta Inc', domain: 'beta.com' },
} as const;

export const USERS = {
  ADMIN_ALPHA: { 
    email: 'admin@alpha.com', 
    password: 'Test1234!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_SUPER_ADMIN'] 
  },
  OPERARIO_ALPHA: { 
    email: 'operario@alpha.com', 
    password: 'Test1234!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_OPERARIO'] 
  },
  ARQUITECTO_ALPHA: { 
    email: 'arquitecto@alpha.com', 
    password: 'Test1234!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_PROCESS_ARCHITECT'] 
  },
  ADMIN_BETA: { 
    email: 'admin@beta.com', 
    password: 'Test1234!', 
    tenant: TENANTS.BETA.id,
    roles: ['ROLE_SUPER_ADMIN'] 
  },
  OPERARIO_BETA: { 
    email: 'operario@beta.com', 
    password: 'Test1234!', 
    tenant: TENANTS.BETA.id,
    roles: ['ROLE_OPERARIO'] 
  },
} as const;


export const API = {
  BASE_URL: 'http://localhost:8080',
  COPILOT_SESSION: '/api/v1/ai/copilot/session',
  WEBHOOK_LEGACY: '/inbound/email-webhook'
};
