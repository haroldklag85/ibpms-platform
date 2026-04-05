import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ServiceAccountsTable from '@/views/admin/RbacManager/ServiceAccountsTable.vue';
import apiClient from '@/services/apiClient';

// Mock the API client
vi.mock('@/services/apiClient', () => {
  return {
    default: {
      get: vi.fn(),
      post: vi.fn(),
      delete: vi.fn()
    }
  };
});

describe('ServiceAccountsTable.vue Semaphore Logic', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('CA-22: must correctly render the expiration semaphore (green, yellow, red)', async () => {
    
    // Set up 3 specific dates:
    // 1. Valid (> 30 days) = Green
    // 2. Expiring soon (< 30 days) = Yellow
    // 3. Expired (Past date) = Red
    const now = new Date();
    
    const dateValid = new Date();
    dateValid.setDate(now.getDate() + 45); // 45 days

    const dateYellow = new Date();
    dateYellow.setDate(now.getDate() + 15); // 15 days

    const dateRed = new Date();
    dateRed.setDate(now.getDate() - 5); // -5 days

    const mockAccounts = [
      { id: '1', name: 'Bot-Valid', status: 'ACTIVE', expirationDate: dateValid.toISOString(), maskedKey: 'sk-1***' },
      { id: '2', name: 'Bot-Warning', status: 'ACTIVE', expirationDate: dateYellow.toISOString(), maskedKey: 'sk-2***' },
      { id: '3', name: 'Bot-Expired', status: 'EXPIRED', expirationDate: dateRed.toISOString(), maskedKey: 'sk-3***' }
    ];

    // Mock API response
    (apiClient.get as any).mockResolvedValue({ data: mockAccounts });

    const wrapper = mount(ServiceAccountsTable);
    
    // wait for promises to resolve inside mounted hook
    await new Promise(resolve => setTimeout(resolve, 0));
    await wrapper.vm.$nextTick();

    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(3);

    // Get the spans that represent the semaphore colors
    // In our component, they have the class "w-3 h-3 rounded-full"
    const semaphores = wrapper.findAll('span.w-3.h-3.rounded-full');
    expect(semaphores.length).toBe(3);

    // Bot-Valid => green
    expect(semaphores[0].classes()).toContain('bg-green-500');

    // Bot-Warning => yellow
    expect(semaphores[1].classes()).toContain('bg-yellow-400');

    // Bot-Expired => red
    expect(semaphores[2].classes()).toContain('bg-red-500');
  });
});
