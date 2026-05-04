import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import EvidenceDropzone from '@/components/forms/generic/EvidenceDropzone.vue';
import { createPinia, setActivePinia } from 'pinia';
import { useGenericFormStore } from '@/stores/genericFormStore';

describe('EvidenceDropzone.vue UX Tests', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.spyOn(window, 'alert').mockImplementation(() => {});
        vi.spyOn(console, 'warn').mockImplementation(() => {});
    });

    it('QA-039-16 (QA-TEST-05): Validación de tamaño de archivo (debe de rechazar > 25MB)', async () => {
        const store = useGenericFormStore();
        const wrapper = mount(EvidenceDropzone);

        // Simulamos un file > 25MB (26MB)
        const bigFile = new File(['a'.repeat(26 * 1024 * 1024)], 'evidencia_pesada.pdf', { type: 'application/pdf' });

        // Activamos evento Drop artificial
        const dropEvent = new Event('drop');
        Object.defineProperty(dropEvent, 'dataTransfer', {
            value: { files: [bigFile] }
        });

        await wrapper.find('.border-dashed').element.dispatchEvent(dropEvent);

        expect(window.alert).toHaveBeenCalledWith(expect.stringContaining('excede el límite de 25MB'));
        expect(store.files.length).toBe(0); // El file no debió anexarse
    });
});
