import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import PmoSettings from '@/views/admin/PMO/PmoSettings.vue';
import apiClient from '@/services/apiClient';
import { nextTick } from 'vue';

describe('PmoSettings.vue', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    setActivePinia(createPinia());
  });

  it('CA-3/CA-5: onMounted fetches business-hours and holidays correctly', async () => {
    const getSpy = vi.spyOn(apiClient, 'get').mockImplementation((url) => {
      if (url === '/admin/sla/business-hours') {
        return Promise.resolve({
          data: { startTime: '09:00', endTime: '17:00', timezone: 'America/New_York' }
        });
      }
      if (url === '/admin/sla/holidays') {
        return Promise.resolve({
          data: [
            { id: '123', holidayDate: '2026-12-25', description: 'Navidad' }
          ]
        });
      }
      return Promise.resolve({ data: null });
    });

    const wrapper = mount(PmoSettings, {
      global: { plugins: [createPinia()] }
    });
    
    // Wait for onMounted promises to resolve
    await flushPromises();

    expect(getSpy).toHaveBeenCalledWith('/admin/sla/business-hours');
    expect(getSpy).toHaveBeenCalledWith('/admin/sla/holidays');

    // Verify DOM updates
    expect(wrapper.text()).toContain('Navidad');
    expect(wrapper.text()).toContain('2026-12-25');
  });

  it('CA-3: Retroactive SLA Apply triggers 202 Modal', async () => {
    vi.spyOn(apiClient, 'get').mockResolvedValue({ data: null });
    
    // For the PUT business-hours call
    const putSpy = vi.spyOn(apiClient, 'put').mockResolvedValue({ data: {} });
    // For the POST apply call
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({ status: 202, data: {} });

    const wrapper = mount(PmoSettings, {
      global: { plugins: [createPinia()] }
    });
    await flushPromises();

    // Toggle applyRetroactive
    const checkbox = wrapper.find('input[type="checkbox"]');
    await checkbox.setValue(true);

    // Find and click 'Actualizar SLA'
    const button = wrapper.find('button.bg-indigo-600');
    await button.trigger('click');

    await flushPromises();

    expect(putSpy).toHaveBeenCalled();
    expect(postSpy).toHaveBeenCalledWith('/admin/sla/apply', null, { params: { applyRetroactively: true } });

    // Verify modal content from document body since it teleports
    expect(document.body.innerHTML).toContain('Recálculo Cíclico en Progreso');
    expect(document.body.innerHTML).toContain('HTTP 202 (Accepted)');
  });

  it('CA-5: Can delete a holiday from the UI', async () => {
    let holidaysCalled = false;
    vi.spyOn(apiClient, 'get').mockImplementation((url) => {
      if (url === '/admin/sla/holidays' && !holidaysCalled) {
        holidaysCalled = true;
        return Promise.resolve({
          data: [
            { id: 'uuid-to-delete', holidayDate: '2026-07-20', description: 'Dia de Independencia' }
          ]
        });
      }
      return Promise.resolve({ data: null });
    });

    const deleteSpy = vi.spyOn(apiClient, 'delete').mockResolvedValue({ status: 204 });

    const wrapper = mount(PmoSettings, {
      global: { plugins: [createPinia()] }
    });
    await flushPromises();

    // Should render the holiday
    expect(wrapper.text()).toContain('Dia de Independencia');

    // Click delete
    const deleteBtn = wrapper.find('button.text-red-400');
    expect(deleteBtn.exists()).toBe(true);
    await deleteBtn.trigger('click');
    await flushPromises();

    expect(deleteSpy).toHaveBeenCalledWith('/admin/sla/holidays/uuid-to-delete');
    
    // UI should no longer contain it (or at least we mocked its disappearance)
    expect(wrapper.text()).not.toContain('Dia de Independencia');
  });
});

// Helper for Vue test utils
const flushPromises = () => new Promise(resolve => setTimeout(resolve, 0));
