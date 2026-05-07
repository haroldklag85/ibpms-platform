import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { nextTick } from 'vue';
import { setActivePinia, createPinia } from 'pinia';
import { usePreferencesStore } from '@/stores/usePreferencesStore';

describe('usePreferencesStore', () => {
    let mockGetItem: ReturnType<typeof vi.spyOn>;
    let mockSetItem: ReturnType<typeof vi.spyOn>;

    beforeEach(() => {
        setActivePinia(createPinia());
        mockGetItem = vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(null);
        mockSetItem = vi.spyOn(Storage.prototype, 'setItem');
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('Default: Sin localStorage, uiDensity = "STANDARD"', () => {
        const store = usePreferencesStore();
        expect(store.uiDensity).toBe('STANDARD');
    });

    it('Mutación persiste: Cambiar uiDensity a "COMPACT" muta el localStorage', async () => {
        const store = usePreferencesStore();
        store.uiDensity = 'COMPACT';
        await nextTick();
        expect(mockSetItem).toHaveBeenCalledWith('ibpms_density', 'COMPACT');
    });

    it('Body attribute: Cambiar densidad -> document.body data-density es el nuevo valor', async () => {
        const store = usePreferencesStore();
        store.uiDensity = 'COMFORTABLE';
        await nextTick();
        
        // El watcher en el store normalmente hace esto, vamos a asegurarnos que se reflejó si el store lo implementa
        expect(document.body.getAttribute('data-density')).toBe('COMFORTABLE');
    });
});
