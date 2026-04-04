import { defineStore } from 'pinia';
import { ref } from 'vue';
import apiClient from '@/services/apiClient';

export interface MenuItem {
    path: string;
    icon: string;
    label: string;
    roles?: string[];
}

export interface MenuGroup {
    title: string;
    roles?: string[];
    items: MenuItem[];
}

export const useMenuStore = defineStore('menu', () => {
    const layout = ref<MenuGroup[]>([]);
    const isLoading = ref(false);

    const fetchMenuLayout = async () => {
        // Cache: Si ya tenemos el layout, no lo pedimos de nuevo
        if (layout.value.length > 0) return;
        
        isLoading.value = true;
        try {
             // Mock UAT (En V2, esto proviene de /api/v1/menu-layout)
             const { data } = await apiClient.get('/api/v1/menu-layout').catch(() => ({
                 data: [
                    {
                        title: 'Workdesk',
                        items: [
                           { path: '/workdesk', icon: 'inbox', label: 'Bandeja Unificada' },
                           { path: '/inbox', icon: 'mail', label: 'Workdesk (Legacy)' },
                           { path: '/kanban', icon: 'view_kanban', label: 'Tablero Kanban' }
                        ]
                    },
                    {
                        title: 'Directivo',
                        roles: ['ROLE_SUPER_ADMIN', 'Global Admin'],
                        items: [
                           { path: '/admin/analytics/bam', icon: 'insights', label: 'BAM Analytics' },
                           { path: '/admin/pmo/settings', icon: 'chronic', label: 'Centro PMO / SLA' }
                        ]
                    },
                    {
                        title: 'Arquitectura',
                        roles: ['ROLE_SUPER_ADMIN'],
                        items: [
                           { path: '/admin/modeler/bpmn', icon: 'account_tree', label: 'Venture Modeler' },
                           { path: '/admin/modeler/dmn', icon: 'rule', label: 'DMN Copilot' },
                           { path: '/admin/modeler/forms', icon: 'dynamic_form', label: 'Form Engine' }
                        ]
                    },
                    {
                        title: 'Administración',
                        roles: ['ROLE_SUPER_ADMIN'],
                        items: [
                           { path: '/admin/security/identity', icon: 'shield_person', label: 'Seguridad (RBAC)' },
                           { path: '/admin/integration/builder', icon: 'extension', label: 'Extensiones' },
                           { path: '/admin/integration/dlq', icon: 'queue', label: 'DLQ Dashboard' },
                           { path: '/admin/projects/manager', icon: 'folder_managed', label: 'Gestor Proyectos' },
                           { path: '/admin/projects/agile-hub', icon: 'speed', label: 'Hub Ágil' },
                           { path: '/admin/mailboxes', icon: 'mark_email_read', label: 'Buzones SAC' }
                        ]
                    }
                 ]
             }));
             layout.value = data;
        } catch (e) {
             console.error('No se pudo hidratar el Menú Dinámico', e);
        } finally {
             isLoading.value = false;
        }
    };

    const clearMenuCache = () => {
        layout.value = [];
    };

    return { layout, isLoading, fetchMenuLayout, clearMenuCache };
});
