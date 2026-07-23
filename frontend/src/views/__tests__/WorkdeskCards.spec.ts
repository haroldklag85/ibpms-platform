import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import Workdesk from '../Workdesk.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';
import { usePreferencesStore } from '@/stores/usePreferencesStore';
import { useAuthStore } from '@/stores/authStore';

describe('Workdesk.vue - Card Grid View Tests', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('Renders Table view when uiDensity is STANDARD', async () => {
        const preferencesStore = usePreferencesStore();
        preferencesStore.uiDensity = 'STANDARD';

        const workdeskStore = useWorkdeskStore();
        workdeskStore.items = [
            {
                unifiedId: 'TASK-1',
                sourceSystem: 'BPMN',
                originalTaskId: 'ORG-1',
                title: 'Test Task Standard Density',
                slaExpirationDate: new Date(Date.now() + 86400000).toISOString(),
                status: 'ACTIVE',
                assignee: 'test.user'
            }
        ];
        workdeskStore.pageInfo.totalElements = 1;
        workdeskStore.pageInfo.pageSize = 15;

        const wrapper = mount(Workdesk, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="task-list"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="task-cards-grid"]').exists()).toBe(false);
    });

    it('Renders Card Grid view when uiDensity is COMFORTABLE', async () => {
        const preferencesStore = usePreferencesStore();
        preferencesStore.uiDensity = 'COMFORTABLE';

        const workdeskStore = useWorkdeskStore();
        workdeskStore.items = [
            {
                unifiedId: 'TASK-1',
                sourceSystem: 'BPMN',
                originalTaskId: 'ORG-1',
                title: 'Test Task Comfortable Density',
                slaExpirationDate: new Date(Date.now() + 86400000).toISOString(),
                status: 'ACTIVE',
                assignee: 'test.user'
            }
        ];
        workdeskStore.pageInfo.totalElements = 1;
        workdeskStore.pageInfo.pageSize = 15;

        const wrapper = mount(Workdesk, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="task-list"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="task-cards-grid"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="task-card-TASK-1"]').exists()).toBe(true);
    });

    it('Applies correct SLA border colors on cards', async () => {
        const preferencesStore = usePreferencesStore();
        preferencesStore.uiDensity = 'COMFORTABLE';

        const workdeskStore = useWorkdeskStore();
        
        // SLA Vencido (Expired), Crítico (Critical/Red), Advertencia (Warning/Orange), En tiempo (Normal/Indigo)
        const now = Date.now();
        workdeskStore.items = [
            {
                unifiedId: 'T-EXPIRED',
                sourceSystem: 'BPMN',
                originalTaskId: 'ORG-EXP',
                title: 'Expired Task',
                slaExpirationDate: new Date(now - 10000).toISOString(), // Vencido
                status: 'ACTIVE',
                assignee: 'test.user'
            },
            {
                unifiedId: 'T-CRITICAL',
                sourceSystem: 'BPMN',
                originalTaskId: 'ORG-CRIT',
                title: 'Critical Task',
                slaExpirationDate: new Date(now + 10 * 60 * 1000).toISOString(), // Vence en 10 min (<15% restante de 120h)
                status: 'ACTIVE',
                assignee: 'test.user'
            }
        ];
        workdeskStore.pageInfo.totalElements = 2;
        workdeskStore.pageInfo.pageSize = 15;

        const wrapper = mount(Workdesk, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        const cardExpired = wrapper.find('[data-testid="task-card-T-EXPIRED"]');
        expect(cardExpired.classes()).toContain('border-l-slate-400');

        const cardCritical = wrapper.find('[data-testid="task-card-T-CRITICAL"]');
        expect(cardCritical.classes()).toContain('border-l-red-500');
    });

    it('Hides action buttons based on personal vs pool tab', async () => {
        const preferencesStore = usePreferencesStore();
        preferencesStore.uiDensity = 'COMFORTABLE';

        const workdeskStore = useWorkdeskStore();
        workdeskStore.items = [
            {
                unifiedId: 'T-PERSONAL',
                sourceSystem: 'BPMN',
                originalTaskId: 'ORG-PERS',
                title: 'Personal Task',
                slaExpirationDate: new Date().toISOString(),
                status: 'ACTIVE',
                assignee: 'test.user'
            }
        ];
        workdeskStore.pageInfo.totalElements = 1;
        workdeskStore.pageInfo.pageSize = 15;

        // Caso 1: Mi Escritorio (PERSONAL) - Debe mostrar Abrir y Liberar
        workdeskStore.activeView = 'PERSONAL';
        const wrapperPersonal = mount(Workdesk, {
            global: { plugins: [pinia] }
        });
        await wrapperPersonal.vm.$nextTick();

        expect(wrapperPersonal.find('[data-testid="btn-open-task"]').exists()).toBe(true);
        expect(wrapperPersonal.find('[data-testid="btn-release-task"]').exists()).toBe(true);

        // Caso 2: Pool Disponible (POOL) - Debe mostrar Explorar y Reclamar
        workdeskStore.activeView = 'POOL';
        workdeskStore.items[0].assignee = null; // En pool no suele tener asignado
        const wrapperPool = mount(Workdesk, {
            global: { plugins: [pinia] }
        });
        await wrapperPool.vm.$nextTick();

        expect(wrapperPool.find('[data-testid="btn-explore-task"]').exists()).toBe(true);
        expect(wrapperPool.find('[data-testid="claim-button-T-PERSONAL"]').exists()).toBe(true);
    });
});
