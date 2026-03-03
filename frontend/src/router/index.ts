import { createRouter, createWebHistory } from 'vue-router';
import MainLayout from '@/layouts/MainLayout.vue';
import Workdesk from '@/views/Workdesk.vue';
import Portal from '@/views/Portal.vue';

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
            name: 'DocumentGrid',
            component: () => import('@/views/admin/SGDEA/DocumentGrid.vue'),
            meta: { requiresAuth: true }
        }
    ]
});

// Navigation Guard estricto (Temporal mock)
router.beforeEach((to, _from, next) => {
    const isAuthenticated = localStorage.getItem('ibpms_token'); // TODO: Conectar a Pinia

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
    } else {
        next();
    }
});

export default router;
