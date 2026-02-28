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
                }
            ]
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
