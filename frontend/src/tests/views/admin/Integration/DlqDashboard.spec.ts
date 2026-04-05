import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import DlqDashboard from '../../../../views/admin/Integration/DlqDashboard.vue';
import fs from 'fs';
import path from 'path';

// Mock del apiClient
vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: [] })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} }))
  }
}));

describe('Dead Letter Queue Dashboard (DLQ) CA-8', () => {

    let componentSource: string;

    beforeEach(() => {
        // Leemos el componente fuente para análisis estático
        const componentPath = path.resolve(__dirname, '../../../../../src/views/admin/Integration/DlqDashboard.vue');
        componentSource = fs.readFileSync(componentPath, 'utf-8');
    });

    it('TEST-F01: Debe renderizar la vista y NO contener datos MOCK hardcodeados en el script', () => {
        // Verificamos que el componente no usa arreglos literales de mock, que han sido removidos.
        const mockArrayPattern = /const\s+fallbackMessages\s*=\s*\[/g;
        expect(mockArrayPattern.test(componentSource)).toBe(false);

         // Debe de todas maneras montar bien
        const wrapper = mount(DlqDashboard);
        expect(wrapper.text()).toContain('Dead Letter Queue Dashboard');
    });

    it('TEST-F02: Debe verificar la AUSENCIA de alert() nativo', () => {
        const hasAlert = /window\.alert\(|alert\(/g.test(componentSource);
        expect(hasAlert).toBe(false);
    });

    it('TEST-F03: Debe existir el modal de confirmación para Purga con text area justificación', async () => {
        const wrapper = mount(DlqDashboard);
        await flushPromises();

        // Forzamos abrir el modal purgando summary local
        wrapper.vm.summary.totalMessages = 5;
        wrapper.vm.isPurgeModalOpen = true;
        await wrapper.vm.$nextTick();

        const purgeModalText = wrapper.text();
        expect(purgeModalText).toContain('Confirmar Purga Masiva');
        expect(purgeModalText).toContain('Esta acción es destructiva');
        
        const textArea = wrapper.find('textarea');
        expect(textArea.exists()).toBe(true);
        expect(textArea.attributes('placeholder')).toContain('Justifique');

        const confirmBtn = wrapper.find('button.bg-rose-600');
        expect(confirmBtn.exists()).toBe(true);
    });

    it('TEST-F04: Debe existir el modal de confirmación para Reintento con advertencia de Idempotencia', async () => {
        const wrapper = mount(DlqDashboard);
        await flushPromises();

        wrapper.vm.summary.totalMessages = 5;
        wrapper.vm.isRetryModalOpen = true;
        await wrapper.vm.$nextTick();

        const retryModalText = wrapper.text();
        expect(retryModalText).toContain('Preparar Reintento');
        expect(retryModalText).toContain('Los Workers de consumo deben ser estrictamente idempotentes');
        expect(retryModalText).toContain('CA-5');
    });

    it('TEST-F05: Debe verificar que la ruta está protegida con requiredRole ADMIN_IT', () => {
        // Leemos router.ts
        const routerPath = path.resolve(__dirname, '../../../../../src/router/index.ts');
        const routerSource = fs.readFileSync(routerPath, 'utf-8');

        // Regex simple para atrapar la defincion de la ruta dlq
        const dlqRoutePattern = /path:\s*['"`]\/admin\/integration\/dlq['"`][\s\S]*?meta:\s*\{[^}]*requiredRole:\s*['"`]ADMIN_IT['"`]/g;
        expect(dlqRoutePattern.test(routerSource)).toBe(true);
    });
});
