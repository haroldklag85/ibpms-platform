import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import SkeletonCard from '@/components/agile/SkeletonCard.vue';

describe('SkeletonCard.vue (CA-15 al CA-18)', () => {
    it('Renderiza la animación de esqueleto al cargar', () => {
        const wrapper = mount(SkeletonCard);
        // Debe tener el placeholder container
        expect(wrapper.find('.skeleton-container').exists()).toBe(true);
        // Debe tener animacion (pulso)
        expect(wrapper.find('.animate-pulse').exists()).toBe(true);
    });
});
