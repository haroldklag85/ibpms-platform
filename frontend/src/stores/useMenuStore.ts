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

    const mapIcon = (mdiIcon: string) => {
        if (!mdiIcon) return 'circle';
        const map: Record<string, string> = {
            'mdi-home': 'home',
            'mdi-desktop-mac': 'desktop_mac',
            'mdi-check-decagram': 'verified',
            'mdi-cog-box': 'settings',
            'mdi-database-plus': 'add_database',
            'mdi-brain': 'psychology',
            'mdi-calendar-alert': 'event_busy',
            'mdi-shield-alert': 'gpp_maybe',
            'mdi-file-tree': 'account_tree',
            'mdi-account-group': 'groups',
            'mdi-filter': 'filter_alt',
            'mdi-text-box-plus': 'post_add',
            'mdi-account-details': 'manage_accounts',
            'mdi-rocket': 'rocket_launch',
            'mdi-hammer-wrench': 'build',
            'mdi-view-dashboard-variant': 'dashboard',
            'mdi-chart-timeline-variant': 'timeline',
            'mdi-chart-bar': 'bar_chart',
            'mdi-monitor-dashboard': 'query_stats',
            'mdi-api': 'api',
            'mdi-book-open-page-variant': 'menu_book',
            'mdi-puzzle-edit': 'extension',
            'mdi-sitemap': 'account_tree',
            'mdi-alert-octagon': 'warning',
            'mdi-folder-lock': 'folder_special',
            'mdi-safe': 'lock',
            'mdi-gavel': 'gavel',
            'mdi-card-account-details': 'badge',
            'mdi-timer-settings': 'timer'
        };
        return map[mdiIcon] || mdiIcon.replace('mdi-', '').replace(/-/g, '_');
    };

    const fetchMenuLayout = async () => {
        // Cache: Si ya tenemos el layout, no lo pedimos de nuevo
        if (layout.value.length > 0) return;
        
        isLoading.value = true;
        try {
             // CA-31: Endpoint Dinámico (Anti-JWT Bloat)
             // Si el endpoint falla o devuelve error, asumimos un layout vacío por Zero-Trust (y se disparará CA-26)
             const { data } = await apiClient.get('/users/me/menu-layout').catch(() => ({
                 data: []
             }));
             
             // Mapeo del formato del backend (MenuItemDTO) al formato del frontend (MenuGroup)
             const mappedLayout: MenuGroup[] = [];
             const rootItems: MenuItem[] = [];
             
             if (Array.isArray(data)) {
                 for (const item of data) {
                     // CA-06: Si es explícitamente un contenedor (tiene propiedad children)
                     if (item.children !== undefined) {
                         // Solo si tiene hijos autorizados lo agregamos (Auto-Collapse)
                         if (item.children.length > 0) {
                             mappedLayout.push({
                                 title: item.title,
                                 items: item.children.map((c: any) => ({
                                     label: c.title,
                                     icon: mapIcon(c.icon),
                                     path: c.path
                                 }))
                             });
                         }
                     } else if (item.path) {
                         // Es un link directo (no tiene children y tiene path válido)
                         rootItems.push({
                             label: item.title,
                             icon: mapIcon(item.icon),
                             path: item.path
                         });
                     }
                 }
             }
             
             // Insertamos los links directos al inicio simulando el grupo "Workdesk"
             if (rootItems.length > 0) {
                 mappedLayout.unshift({
                     title: 'Workdesk', 
                     items: rootItems
                 });
             }

             layout.value = mappedLayout;

             // @Traceability: US-036 - CA-26 (UX Fallback)
             // Si tras la hidratación el menú sigue vacío, redirigimos a una zona neutral
             if (layout.value.length === 0) {
                 console.warn("CA-26: Sin topología de menús detectada. Activando fallback de seguridad.");
                 // Usamos un pequeño delay para asegurar que el ruteador esté listo
                 setTimeout(() => {
                     if (window.location.pathname !== '/') {
                         window.location.href = '/';
                     }
                 }, 500);
             }
        } catch (e) {
             console.error('No se pudo hidratar el Menú Dinámico', e);
             layout.value = [];
        } finally {
             isLoading.value = false;
        }
    };

    // CA-32: Auto-Curación. Purga la topología del cliente
    const purgeTopology = () => {
        layout.value = [];
    };

    return { layout, isLoading, fetchMenuLayout, purgeTopology };
});
