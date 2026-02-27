import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/inbox'
        },
        {
            path: '/inbox',
            name: 'Inbox',
            component: () => import('../views/InboxPage.vue')
        },
        {
            path: '/dashboard',
            name: 'Dashboard',
            component: () => import('../views/DashboardPage.vue')
        },
        {
            path: '/designer',
            name: 'Designer',
            component: () => import('../views/ProcessDesignerPage.vue')
        }
    ]
})

export default router
