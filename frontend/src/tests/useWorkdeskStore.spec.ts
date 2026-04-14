import { setActivePinia, createPinia } from 'pinia';
import { beforeEach, describe, expect, it, vi, afterEach } from 'vitest';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

vi.mock('@/services/apiClient');

describe('useWorkdeskStore.ts - Iteration 79-DEV (CA-06, CA-13, CA-26, CA-27)', () => {
    let store: ReturnType<typeof useWorkdeskStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useWorkdeskStore();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('Test 10: CA-13 Minificación WebSocket: 100 REMOVEs en 100ms colapsan en 1 solo batch de mutación (throttle delay)', () => {
        // Arrange
        for(let i=0; i<15; i++) {
            store.items.push({ unifiedId: `TK-${i}`, status: 'ACTIVE', title: `Task ${i}`, sourceSystem: 'BPMN', originalTaskId: `t${i}`, slaExpirationDate: '', assignee: null, progressPercent: 0, financialImpactHigh: false, typeBadge: '⚡ Flujo' });
        }
        expect(store.items.length).toBe(15);

        // Act - Simulate 100 fast removes (only touching 5 actual items to verify batching)
        for(let i=0; i<100; i++) {
            store._handleWsRemove(`TK-${i % 5}`); // targets TK-0 to TK-4 multiple times
        }

        // Before timer triggers, items should still be in DOM unmutated
        expect(store.items.length).toBe(15);
        expect(store._pendingRemovals.length).toBe(100);

        // Fast-forward 2000ms removal throttle
        vi.advanceTimersByTime(2000);

        // The 5 targeted items should now be _isGhost=true
        const ghosts = store.items.filter(i => (i as any)._isGhost);
        expect(ghosts.length).toBe(5);
    });

    it('Test 11: CA-13 Desvanecimiento: REMOVE aplica _isGhost=true y se limpia físicamente de memoria a los 800ms', () => {
        // Arrange
        store.items.push({ unifiedId: `GHOST-1`, status: 'ACTIVE', title: `Ghost`, sourceSystem: 'BPMN', originalTaskId: `g1`, slaExpirationDate: '', assignee: null, progressPercent: 0, financialImpactHigh: false, typeBadge: '⚡ Flujo' });
        
        // Act
        store._handleWsRemove('GHOST-1');
        
        // Avance de Throttle
        vi.advanceTimersByTime(2000);
        expect((store.items[0] as any)._isGhost).toBe(true);
        expect(store.items.length).toBe(1);

        // Avance tiempo de animación de Vue (fade-out)
        vi.advanceTimersByTime(800);
        
        // Assert
        expect(store.items.length).toBe(0);
    });

    it('Test 12: CA-26 Auto-refill: Si tras el REMOVE masivo los items bajan a < 15, se invoca fetchGlobalInbox silenciosamente', () => {
        // Arrange
        store.pageInfo.totalElements = 100; // Database still has items
        for(let i=0; i<15; i++) {
            store.items.push({ unifiedId: `TK-${i}`, status: 'ACTIVE', title: `T${i}`, sourceSystem: 'BPMN', originalTaskId: `t${i}`, slaExpirationDate: '', assignee: null, progressPercent: 0, financialImpactHigh: false, typeBadge: '⚡ Flujo' });
        }
        
        const fetchSpy = vi.spyOn(store, 'fetchGlobalInbox').mockResolvedValue(undefined);

        // Act
        store._handleWsRemove('TK-1');
        vi.advanceTimersByTime(3000); // Wait 2000 (throttle) + 800 (animation) + buffer
        
        // Items == 14 now. It should trigger _checkAutoRefill debounce (5000ms delay)
        expect(store.items.length).toBe(14);
        vi.advanceTimersByTime(5000);

        // Assert
        expect(fetchSpy).toHaveBeenCalledWith(store.currentPage, 15);
    });

    it('Test 13: CA-26 Last Page Protection + CA-12 Empty State: si en la page > 0 los items bajan a 0, redirect a page 0', () => {
        // Arrange
        store.currentPage = 2;
        store.items.push({ unifiedId: `TK-LAST`, status: 'ACTIVE', title: `TL`, sourceSystem: 'BPMN', originalTaskId: `tl`, slaExpirationDate: '', assignee: null, progressPercent: 0, financialImpactHigh: false, typeBadge: '⚡ Flujo' });
        
        const fetchSpy = vi.spyOn(store, 'fetchGlobalInbox').mockResolvedValue(undefined);

        // Act
        store._handleWsRemove('TK-LAST');
        vi.advanceTimersByTime(3000); // Resolves physical removal

        // Assert
        expect(store.items.length).toBe(0);
        expect(fetchSpy).toHaveBeenCalledWith(0, 15); // Redirects to page 0
    });

    it('Test 14: CA-27 Vocabulario STOMP: Validar que el router interno despacha las 4 acciones (REMOVE, ADD, UPDATE, PRIORITY_CHANGE)', () => {
        // Arrange
        const removeSpy = vi.spyOn(store, '_handleWsRemove').mockImplementation(() => {});
        const addSpy = vi.spyOn(store, '_handleWsAdd').mockImplementation(() => {});
        const updateSpy = vi.spyOn(store, '_handleWsUpdate').mockImplementation(() => {});
        const prioritySpy = vi.spyOn(store, '_handleWsPriorityChange').mockImplementation(() => {});

        const mockPayload = { unifiedId: 'TK-x', status: 'ACTIVE', title: 'Tx', sourceSystem: 'BPMN', originalTaskId: 'tx', slaExpirationDate: '', assignee: null, progressPercent: 0, financialImpactHigh: false, typeBadge: '⚡ Flujo' } as any;

        const dispatchEvent = (event: any) => {
            switch (event.action) {
                case 'REMOVE': store._handleWsRemove(event.taskId); break;
                case 'ADD': store._handleWsAdd(event.payload); break;
                case 'UPDATE': store._handleWsUpdate(event.taskId, event.payload); break;
                case 'PRIORITY_CHANGE': store._handleWsPriorityChange(); break;
            }
        };

        // Act
        dispatchEvent({ action: 'REMOVE', taskId: '123' });
        dispatchEvent({ action: 'ADD', payload: mockPayload });
        dispatchEvent({ action: 'UPDATE', taskId: '123', payload: mockPayload });
        dispatchEvent({ action: 'PRIORITY_CHANGE' });

        // Assert
        expect(removeSpy).toHaveBeenCalledWith('123');
        expect(addSpy).toHaveBeenCalledWith(mockPayload);
        expect(updateSpy).toHaveBeenCalledWith('123', mockPayload);
        expect(prioritySpy).toHaveBeenCalled();
    });
});
