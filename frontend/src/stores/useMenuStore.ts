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
                        title: 'Operativo',
                        items: [
                           { path: '/workdesk', icon: 'inbox', label: 'Bandeja Unificada' },
                           { path: '/kanban', icon: 'view_kanban', label: 'Tablero Kanban' }
                        ]
                    },
                    {
                        title: 'Service Delivery',
                        roles: ['Global Admin', 'ROLE_SUPER_ADMIN'],
                        items: [
                           { path: '/intake-triage', icon: 'mark_email_unread', label: 'Inbox Intake' },
                           { path: '/admin/intake', icon: 'post_add', label: 'Intake Manual' },
                           { path: '/admin/customer360', icon: 'person_search', label: 'Customer 360' },
                           { path: '/portal/tracking', icon: 'track_changes', label: 'Portal Cliente' }
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
                        title: 'Configuración',
                        roles: ['ROLE_SUPER_ADMIN'],
                        items: [
                           { path: '/admin/modeler/bpmn', icon: 'account_tree', label: 'Venture Modeler' },
                           { path: '/admin/modeler/dmn', icon: 'rule', label: 'DMN Copilot' },
                           { path: '/admin/modeler/forms', icon: 'dynamic_form', label: 'Form Engine' }
                        ]
                    },
                    {
                        title: 'Integración',
                        roles: ['ROLE_SUPER_ADMIN'],
                        items: [
                           { path: '/admin/integration/catalog', icon: 'hub', label: 'Catálogo Conectores' },
                           { path: '/admin/integration/builder', icon: 'extension', label: 'Constructor API' },
                           { path: '/admin/integration/mapper', icon: 'conversion_path', label: 'Visual Mapper' },
                           { path: '/admin/integration/dlq', icon: 'queue', label: 'DLQ Dashboard' }
                        ]
                    },
                    {
                        title: 'Proyectos',
                        roles: ['ROLE_SUPER_ADMIN', 'Global Admin'],
                        items: [
                           { path: '/admin/project-builder', icon: 'construction', label: 'Project Builder' },
                           { path: '/admin/projects/manager', icon: 'folder_managed', label: 'Gestor Proyectos' },
                           { path: '/admin/projects/agile-hub', icon: 'speed', label: 'Hub Ágil' }
                        ]
                    },
                    {
                        title: 'Administración',
                        roles: ['ROLE_SUPER_ADMIN'],
                        items: [
                           { path: '/admin/security/identity', icon: 'shield_person', label: 'Seguridad (RBAC)' },
                           { path: '/admin/mailboxes', icon: 'mark_email_read', label: 'Buzones SAC' },
                           { path: '/admin/incidents', icon: 'warning', label: 'Centro Incidentes' },
                           { path: '/sgdea/vault', icon: 'inventory_2', label: 'Bóveda Documental' },
                           { path: '/ai/prompts', icon: 'psychology', label: 'Librería Prompts IA' },
                           { path: '/admin', icon: 'settings', label: 'Configuración General' }
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
