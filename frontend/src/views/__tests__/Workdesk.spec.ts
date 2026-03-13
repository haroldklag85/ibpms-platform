import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import Workdesk from '../Workdesk.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

// Ensure timers are faked for the debounce and SLA interval tests
vi.useFakeTimers();

describe('Workdesk.vue (US-001 Hybrid CQRS & SLA Ticking)', () => {
    let pinia: any;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.clearAllTimers();
    });

    it('Renders the CQRS Native Data Grid <table> with <th> properly', async () => {
        const store = useWorkdeskStore();
        
        // Mocking 10 Tasks (Mix of BPMN and KANBAN)
        store.items = Array.from({ length: 10 }, (_, i) => ({
            unifiedId: `TASK-${i}`,
            sourceSystem: i % 2 === 0 ? 'BPMN' : 'KANBAN',
            originalTaskId: `ORG-${i}`,
            title: `Test Task ${i}`,
            slaExpirationDate: new Date(Date.now() + 86400000).toISOString(), // +1 Day
            status: 'ACTIVE',
            assignee: 'test.user'
        }));
        store.pageInfo.totalElements = 10;
        store.pageInfo.pageSize = 50;

        const wrapper = mount(Workdesk, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        // Check if table is rendered instead of div cards
        const table = wrapper.find('table');
        expect(table.exists()).toBe(true);
        expect(wrapper.findAll('th').length).toBeGreaterThan(0);
        expect(wrapper.findAll('tbody tr').length).toBe(10);
    });

    it('Executes Search with 500ms Debounce altering the Pinia Store call', async () => {
        const store = useWorkdeskStore();
        // Spy on store action
        const fetchSpy = vi.spyOn(store, 'fetchGlobalInbox').mockResolvedValue();

        const wrapper = mount(Workdesk, {
            global: { plugins: [pinia] }
        });

        const searchInput = wrapper.find('input[type="search"]');
        expect(searchInput.exists()).toBe(true);

        // Initial mount triggers loadData()
        expect(fetchSpy).toHaveBeenCalledTimes(1);

        // Trigger input event
        await searchInput.setValue('Urgente');
        
        // Before 500ms, it shouldn't be called again
        vi.advanceTimersByTime(200);
        expect(fetchSpy).toHaveBeenCalledTimes(1);

        // After 500ms debounce
        vi.advanceTimersByTime(300);
        
        expect(fetchSpy).toHaveBeenCalledTimes(2);
        expect(fetchSpy).toHaveBeenCalledWith(0, 50, 'Urgente', ''); // Assert sent query payload
    });

    it('Transitions SLA Colors reactively via Ticking Engine', async () => {
        const store = useWorkdeskStore();
        
        const now = Date.now();
        // Create task expiring in exactly 60 seconds (1 minute)
        store.items = [{
            unifiedId: 'T-1',
            sourceSystem: 'BPMN',
            originalTaskId: 'ORG-1',
            title: 'SLA Ticking Test',
            slaExpirationDate: new Date(now + 60000).toISOString(),
            status: 'ACTIVE',
            assignee: null
        }];

        const wrapper = mount(Workdesk, {
            global: { plugins: [pinia] }
        });

        await wrapper.vm.$nextTick();

        // 1. Initial State: It is green because 60,000 > 0 but it's evaluated against diffHours.
        // Wait, diffHours = (flag - currentTick) = 60000 / 3600000 = 0.016 (< 24).
        // It falls under WARNING (<= 24 but > 0).
        // Let's create one that is > 24 hours (e.g. 25 hours = 90,000,000 ms) so it starts green.
        store.items[0].slaExpirationDate = new Date(now + 90000000).toISOString();
        await wrapper.vm.$nextTick();
        
        let slaBadge = wrapper.find('td span[class*="bg-green-100"]');
        expect(slaBadge.exists()).toBe(true);

        // Mutate DB Mock Date to simulate it is now expiring in 10 minutes (WARNING territory)
        // Wait, the test asks for Native Timer advancement acting on currentTick.
        // If we advance timer by 2 days (172,800,000 ms), the initial task (expiring in 25hrs) will become EXPIRED (< 0).
        
        vi.advanceTimersByTime(172800000); // Fast-forward 2 days
        await wrapper.vm.$nextTick(); // Let Vue react to the currentTick update

        // Should mutate from green to red (bg-red-100) since diffHours < 0
        slaBadge = wrapper.find('td span[class*="bg-red-100"]');
        expect(slaBadge.exists()).toBe(true);
    });
});
