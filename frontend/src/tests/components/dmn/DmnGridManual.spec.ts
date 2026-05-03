import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import DmnIntelligence from '@/views/admin/Modeler/DmnIntelligence.vue';
import DmnGridManual from '@/components/dmn/DmnGridManual.vue';
import { createPinia, setActivePinia } from 'pinia';

describe('DmnGridManual & Split-View (US-007 CA-26 to CA-32)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders_split_view_chat_and_grid (CA-26)', () => {
    const wrapper = mount(DmnIntelligence);
    const chatPanel = wrapper.find('.chat-panel');
    const gridPanel = wrapper.find('.grid-panel');
    // Using exists() and expect them to be present
    expect(chatPanel.exists()).toBe(true);
    expect(gridPanel.exists()).toBe(true);
  });

  it('validates_feel_syntax_realtime (CA-28)', async () => {
    const wrapper = mount(DmnGridManual);
    await wrapper.vm.$nextTick();
    const firstInput = wrapper.find('tbody tr:first-child td input');
    
    // Simulate invalid feel
    await firstInput.setValue('abcd');
    // It should have border-red-500
    expect(firstInput.classes()).toContain('border-red-500');

    // Simulate valid feel
    await firstInput.setValue('"Aprobado"');
    expect(firstInput.classes()).not.toContain('border-red-500');
  });

  it('renders_catch_all_locked_row (CA-29)', () => {
    const wrapper = mount(DmnGridManual);
    const catchAllRow = wrapper.find('tr.catch-all');
    expect(catchAllRow.exists()).toBe(true);
    const deleteBtn = catchAllRow.find('button.delete-row');
    expect(deleteBtn.exists()).toBe(false);
  });

  it('disables_add_button_at_100_rows (CA-31)', async () => {
    const wrapper = mount(DmnGridManual);
    // Push enough rows to hit 100
    while (wrapper.vm.rows.length < 100) {
      wrapper.vm.rows.push({ id: `mock-${wrapper.vm.rows.length}`, inputs: ['""'], outputs: ['""'] });
    }
    await wrapper.vm.$nextTick();
    const addBtn = wrapper.find('button.btn-add-row');
    expect(addBtn.attributes('disabled')).toBeDefined();
  });
});
