import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import DmnIntelligence from '@/views/admin/Modeler/DmnIntelligence.vue';
import { createPinia, setActivePinia } from 'pinia';
import apiClient from '@/services/apiClient';

vi.mock('@/services/apiClient', () => ({
  default: {
    post: vi.fn().mockResolvedValue({})
  }
}));

global.confirm = vi.fn(() => true);

describe('DmnPublishModal & Panic / Rollback (GAP-10)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('requires CONFIRMO_V2 to enable deploy button', async () => {
    const wrapper = mount(DmnIntelligence, {
      global: { stubs: { Teleport: true } }
    });
    
    wrapper.vm.authStore = { hasAnyRole: () => true };
    wrapper.vm.dmnDraft = { hasData: true };
    wrapper.vm.isFormValid = true;
    await wrapper.vm.$nextTick();
    
    wrapper.vm.openPublishModal();
    await wrapper.vm.$nextTick();
    
    let btn = wrapper.findAll('button').filter(b => b.text().includes('Publicar Ahora'))[0];
    expect((btn.element as HTMLButtonElement).disabled).toBe(true);
    
    wrapper.find('input[placeholder="CONFIRMO_V2"]').setValue('CONFIRMO_V2');
    await wrapper.vm.$nextTick();
    
    btn = wrapper.findAll('button').filter(b => b.text().includes('Publicar Ahora'))[0];
    expect((btn.element as HTMLButtonElement).disabled).toBe(false);
  });

  it('rollback button invokes POST /api/v1/dmn/{id}/rollback', async () => {
    const wrapper = mount(DmnIntelligence, {
      global: { stubs: { Teleport: true } }
    });
    
    wrapper.vm.authStore = { hasAnyRole: () => true };
    wrapper.vm.dmnDraft = { hasData: true };
    await wrapper.vm.$nextTick();
    
    await wrapper.vm.resetToV1();
    expect(apiClient.post).toHaveBeenCalledWith('/dmn/current-dmn-id/rollback');
  });
});
