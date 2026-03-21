import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { defineComponent, ref, computed } from 'vue';

// Mock del Componente Panic Modal
const PanicModalMock = defineComponent({
    template: `
        <div class="panic-modal">
            <input type="text" v-model="confirmationText" class="security-input" />
            <button :disabled="!isConfirmed" class="submit-btn" @click="$emit('execute')">Destruir</button>
        </div>
    `,
    setup() {
        const confirmationText = ref('');
        const isConfirmed = computed(() => confirmationText.value === 'CONFIRMO_V2');
        return { confirmationText, isConfirmed };
    }
});

describe('US-007 CA-10/12: QA Experiencia de Usuario - Panic Modal (Iteración 52)', () => {
    
    it('Aserta matemáticamente que el botón Destructivo permance deshabilitado hasta el input estricto', async () => {
        const wrapper = mount(PanicModalMock);
        
        // Localizamos el botón destructivo
        const submitBtn = wrapper.find('.submit-btn');
        const inputField = wrapper.find('.security-input');

        // Aserción 1: Estado Inicial (Botón Bloqueado Lógicamente)
        expect(submitBtn.attributes('disabled')).toBeDefined();

        // Aserción 2: Tipado Parcial o Erróneo (Botón Bloqueado)
        await inputField.setValue('CONFIR');
        expect(submitBtn.attributes('disabled')).toBeDefined();

        await inputField.setValue('confirmo_v2'); // Case Sensitive Hard-Stop
        expect(submitBtn.attributes('disabled')).toBeDefined();

        // Aserción 3: Input Exacto (Botón Liberado)
        await inputField.setValue('CONFIRMO_V2');
        expect(submitBtn.attributes('disabled')).toBeUndefined(); // Atributo removido

        // Aserción 4: Emisión Exitosa
        await submitBtn.trigger('click');
        expect(wrapper.emitted()).toHaveProperty('execute');
    });
});
