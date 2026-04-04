import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import DlqDashboard from '../../../../views/admin/Integration/DlqDashboard.vue';

// Mock del apiClient
vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(() => Promise.reject(new Error('Network Error'))) // Trigger fallback mock
  }
}));

describe('Dead Letter Queue Dashboard (DLQ)', () => {
    it('Debe renderizar la vista del Dashboard DLQ correctamente', () => {
        const wrapper = mount(DlqDashboard);
        expect(wrapper.text()).toContain('Dead Letter Queue Dashboard');
        expect(wrapper.text()).toContain('Monitorización de RabbitMQ y TaskRescue');
    });

    it('Debe inyectar Fake/Mock Data (Fallback) tras simular un API call y mostrar la tabla', async () => {
        const wrapper = mount(DlqDashboard);
        // Wait for onMounted fetch
        await flushPromises();
        // Fallback delay is 600ms
        await new Promise(r => setTimeout(r, 650));
        await wrapper.vm.$nextTick();

        // 3 Mensajes mockeados deberían estar dibujados
        expect(wrapper.text()).toContain('dlx.exchange'); // Fixed from O365/Exchange
        expect(wrapper.text()).toContain('camunda.task.create');
        expect(wrapper.text()).toContain('Connection Refused: Postgres DB Pool exhausted.');
        
        // Count rows in tbody
        const rows = wrapper.findAll('tbody tr');
        expect(rows.length).toBe(3);
    });

    it('Debe purgar todos los mensajes de la tabla si el usuario hace clic en Purgar Todo', async () => {
        const wrapper = mount(DlqDashboard);
        await flushPromises();
        await new Promise(r => setTimeout(r, 650));
        await wrapper.vm.$nextTick();
        
        // Verificar que el botón Purgar Todo existe y hacer click
        const purgeBtn = wrapper.find('button.bg-red-100');
        expect(purgeBtn.exists()).toBe(true);
        expect(purgeBtn.text()).toContain('Purgar Todo');
        
        await purgeBtn.trigger('click');
        await wrapper.vm.$nextTick();
        
        // Verificar que la tabla ya no esté
        expect(wrapper.text()).toContain('DLQ Limpia');
        const rows = wrapper.findAll('tbody tr');
        expect(rows.length).toBe(0);
    });
});
