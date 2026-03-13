import { defineStore } from 'pinia';
import { ref, watch } from 'vue';

type UIDensity = 'COMPACT' | 'STANDARD' | 'COMFORTABLE';

export const usePreferencesStore = defineStore('preferences', () => {
    // Inicializar desde localStorage o default a STANDARD
    const savedDensity = (localStorage.getItem('ibpms_density') as UIDensity) || 'STANDARD';
    const uiDensity = ref<UIDensity>(savedDensity);

    // Watcher estricto: Todo cambio muta root attribute y localStorage sin repintados forzados (Zero-If Rule)
    watch(uiDensity, (newDensity) => {
        document.body.setAttribute('data-density', newDensity);
        localStorage.setItem('ibpms_density', newDensity);
    }, { immediate: true });

    return {
        uiDensity
    };
});
