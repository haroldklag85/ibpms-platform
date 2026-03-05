import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import SacConfigManager from '@/views/admin/AI/SacConfigManager.vue';

// Mocking Pinia and Axios if necessary - but here we just need to test the UI logic
import apiClient from '@/services/apiClient';
vi.mock('@/services/apiClient', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

describe('SacConfigManager.vue (Pantalla 15)', () => {

    beforeEach(() => {
        vi.clearAllMocks();
        (apiClient.get as any).mockResolvedValue({ data: [] });
    });

    it('debe mantener el boton de Guardar deshabilitado hasta que Test Connection sea Exitoso', async () => {
        const wrapper = mount(SacConfigManager);

        // Esperar a que renderice y resuelva onMounted
        await wrapper.vm.$nextTick();

        // Abrimos el modal
        await wrapper.find('button').trigger('click'); // Click "Añadir Buzón"

        // Asegurarnos que el modal abrió
        const modalTitle = wrapper.find('#modal-title');
        expect(modalTitle.exists()).toBe(true);

        // Encontrar el botón de Guardar Configuración (está en el footer del modal)
        // Buscamos un botón que contenga el texto "Guardar Configuración"
        const buttons = wrapper.findAll('button');
        const saveButton = buttons.find(b => b.text().includes('Guardar Configuración'));

        expect(saveButton).toBeDefined();

        // Aserción QA Estricta: El botón NACE DESHABILITADO
        expect(saveButton?.attributes('disabled')).toBeDefined();

        // Llenamos el formulario
        await wrapper.find('input[placeholder="Ej: Soporte VIP"]').setValue('Buzón Test');

        // Encontrar el boton de Test Connection
        const testButton = buttons.find(b => b.text().includes('Probar Conexión'));
        await testButton?.trigger('click');

        // Como Mockeamos falla si los inputs de secrets están vacios:
        // La aserción de que se habilitó requiere simular el éxito, en Vue se puede mockear la respuesta de apiClient
        (apiClient.post as any).mockResolvedValueOnce({ data: { message: '200 OK', status: 'SUCCESS' } });

        // Asignar variables directamente al VM para simular exito de test (o llenar inputs)
        (wrapper.vm as any).form.tenantId = 'T-1';
        (wrapper.vm as any).form.clientId = 'C-1';
        (wrapper.vm as any).form.rawClientSecret = 'Secret-1';

        await (wrapper.vm as any).testLiveConnection();
        await wrapper.vm.$nextTick();

        // Aserción: El botón AHORA ESTÁ HABILITADO
        expect(saveButton?.attributes('disabled')).toBeUndefined();
    });
});
