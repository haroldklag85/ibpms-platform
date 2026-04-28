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
             // CA-31: Endpoint Dinámico (Anti-JWT Bloat)
             // Si el endpoint falla o devuelve error, asumimos un layout vacío por Zero-Trust (y se disparará CA-26)
             const { data } = await apiClient.get('/users/me/menu-layout').catch(() => ({
                 data: []
             }));
             layout.value = data;
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
