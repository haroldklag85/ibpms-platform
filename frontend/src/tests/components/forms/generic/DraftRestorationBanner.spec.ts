import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import DraftSyncIndicator from '@/components/forms/generic/DraftSyncIndicator.vue';
import { createPinia, setActivePinia } from 'pinia';
import { useGenericFormStore } from '@/stores/genericFormStore';

describe('Draft Validation UX Tests', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('QA-039-12 (QA-TEST-04): El indicador de sync se muestra si hay draft local (Borrador en Navegador)', async () => {
        const store = useGenericFormStore();
        const wrapper = mount(DraftSyncIndicator);
        
        // Simular draft que no está guardado
        store.$patch({ syncState: 'LOCAL_ONLY' });
        await wrapper.vm.$nextTick();
        
        expect(wrapper.text()).toContain('Borrador en Navegador');
    });

    it('QA-039-13 (QA-TEST-04): El Banner o Indicator vuelve a SYNCED tras restaurar (guardar auto)', async () => {
        const store = useGenericFormStore();
        const wrapper = mount(DraftSyncIndicator);
        
        store.$patch({ syncState: 'SYNCED' });
        await wrapper.vm.$nextTick();
        
        expect(wrapper.text()).toContain('Sincronizado');
    });
    
    it('QA-039-15 (QA-TEST-04): Estado de ERROR', async () => {
        const store = useGenericFormStore();
        const wrapper = mount(DraftSyncIndicator);
        
        store.$patch({ syncState: 'ERROR' });
        await wrapper.vm.$nextTick();
        
        expect(wrapper.text()).toContain('Error de Sincronización');
    });
});
