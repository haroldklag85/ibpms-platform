import { createRouter, createWebHistory } from 'vue-router';
import MainLayout from '@/layouts/MainLayout.vue';
import Workdesk from '@/views/Workdesk.vue';
import Portal from '@/views/Portal.vue';
import DocumentGrid from '@/views/admin/SGDEA/DocumentGrid.vue';
import PromptLibrary from '@/views/admin/AI/PromptLibrary.vue';
import IdentityGovernance from '@/views/admin/Security/IdentityGovernance.vue';

// ── Interceptors ─────────────────────────────────────────────
import { rbacGuard } from './RouteGuards';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/Login.vue'),
        },
        {
            path: '/',
            component: MainLayout,
            // Requiere autenticación
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    name: 'Portal',
                    component: Portal, // Pantalla 0
                },
                {
                    path: 'workdesk',
                    name: 'Workdesk',
                    component: Workdesk, // Pantalla 1
                },
                {
                    path: 'kanban',
                    name: 'KanbanBoard',
                    component: () => import('@/views/kanban/KanbanView.vue'),
                },
                {
                    path: 'admin',
                    name: 'AdminSettings',
                    component: () => import('@/views/admin/SettingsView.vue'),
                },
                {
                    path: 'admin/modeler/bpmn',
                    name: 'BpmnDesigner',
                    component: () => import('@/views/admin/Modeler/BpmnDesigner.vue'),
                },
                {
                    path: 'admin/modeler/forms',
                    name: 'FormDesigner',
                    component: () => import('@/views/admin/Modeler/FormDesigner.vue'),
                },
                {
                    path: 'admin/modeler/dmn',
                    name: 'DmnIntelligence',
                    component: () => import('@/views/admin/Modeler/DmnIntelligence.vue'),
                }
            ]
        },
        {
            path: '/inbox',
            name: 'InboxMailbox',
            component: () => import('@/views/inbox/InboxView.vue'),
            meta: { requiresAuth: true }
        },
        // --- Bloque 3: Service Delivery Intake ---
        {
            path: '/admin/intake',
            name: 'IntakeManual',
            component: () => import('@/views/admin/ServiceDelivery/IntakeManual.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/admin/customer360',
            name: 'Customer360',
            component: () => import('@/views/admin/ServiceDelivery/Customer360.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/portal/tracking',
            name: 'CustomerPortal',
            component: () => import('@/views/public/CustomerPortal.vue'),
            // No auth required
            meta: { requiresAuth: false }
        },
        // --- Bloque A: Project Builder ---
        {
            path: '/admin/project-builder',
            name: 'ProjectBuilder',
            component: () => import('@/views/admin/ProjectBuilder/ProjectBuilder.vue'),
            meta: { requiresAuth: true }
        },
        // --- Bloque C: Dashboards BAM & Analytics ---
        {
            path: '/admin/analytics/bam',
            name: 'DashboardBAM',
            component: () => import('@/views/admin/Analytics/DashboardBAM.vue'),
            meta: { requiresAuth: true }
        },
        // --- Bloque F: Integration Hub (Pantalla 11) ---
        {
            path: '/admin/integration/catalog',
            name: 'ConnectorCatalog',
            component: () => import('@/views/admin/Integration/ConnectorCatalog.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/admin/integration/builder',
            name: 'ConnectorBuilder',
            component: () => import('@/views/admin/Integration/ConnectorBuilder.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/admin/integration/mapper',
            name: 'VisualMapper',
            component: () => import('@/views/admin/Integration/VisualMapper.vue'),
            meta: { requiresAuth: true }
        },
        // --- Bloque G: SGDEA (Pantalla 12) ---
        {
            path: '/sgdea/vault',
            name: 'SGD_Vault',
            component: DocumentGrid,
            meta: { title: 'Bóveda Documental', requiresAuth: true }
        },
        {
            path: '/ai/prompts',
            name: 'AI_PromptLibrary',
            component: PromptLibrary,
            meta: { title: 'Librería de Prompts', requiresAuth: true, roles: ['Global Admin', 'prompt_engineer'] } // CA-7 Role guard futuro
        },
        // --- Bloque J: Identity Governance (Pantalla 14) ---
        {
            path: '/admin/security/identity',
            name: 'IdentityGovernance',
            component: IdentityGovernance,
            meta: { title: 'Gobernanza de Identidades', requiresAuth: true, roles: ['Global Admin'] }
        }
    ]
});

// Navigation Guard estricto
router.beforeEach(rbacGuard);

export default router;
