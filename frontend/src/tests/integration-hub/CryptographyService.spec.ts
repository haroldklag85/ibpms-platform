/**
 * @vitest-environment jsdom
 */
import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import PgpValidator from '@/views/admin/Integration/PgpValidator.vue';

// ── QA Instruction 2: PGP Payload UI Validator (CA-12) ──
describe('Pantalla 8: Integration Hub - Cryptography UI', () => {

    it('Debe renderizar un bloque PGP (ASCII Armor) al encriptar el payload', async () => {
        const wrapper = mount(PgpValidator);

        // Inyectar Payload JSON y Llave Pública simulada
        const jsonInput = wrapper.find('[data-test="raw-json-input"]');
        await jsonInput.setValue('{"status": "CONFIDENTIAL"}');

        const keyInput = wrapper.find('[data-test="pub-key-input"]');
        await keyInput.setValue('-----BEGIN PGP PUBLIC KEY BLOCK-----...');

        // Trigger encrypt
        await wrapper.find('[data-test="encrypt-btn"]').trigger('click');
        await wrapper.vm.$nextTick();

        const encryptedOutput = wrapper.find('[data-test="encrypted-output"]');

        expect(encryptedOutput.exists()).toBe(true);
        expect(encryptedOutput.text()).toContain('-----BEGIN PGP MESSAGE-----');
        expect(encryptedOutput.text()).toContain('-----END PGP MESSAGE-----');
        expect(encryptedOutput.text()).not.toContain('CONFIDENTIAL'); // La interfaz solo muestra el armor block
    });

});
