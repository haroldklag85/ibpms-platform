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
            component: () => import('inbox_remote/InboxPage')
        },
        {
            path: '/dashboard',
            name: 'Dashboard',
            component: () => import('inbox_remote/DashboardPage')
        },
        {
            path: '/designer',
            name: 'Designer',
            component: () => import('admin_remote/ProcessDesignerPage')
        }
    ]
})

export default router
