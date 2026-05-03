import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach } from 'vitest';
import DmnIntelligence from '@/views/admin/Modeler/DmnIntelligence.vue';
import DmnGridManual from '@/components/dmn/DmnGridManual.vue';
import { createPinia, setActivePinia } from 'pinia';

describe('DmnGridManual & Split-View (US-007 CA-26 to CA-32)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('gap-08: virtual_scrolling_renders_limited_items', async () => {
    const wrapper = mount(DmnGridManual);
    wrapper.vm.rows = [];
    for(let i=0; i<50; i++) {
        wrapper.vm.rows.push({ id: `row-${i}`, inputs: ['""', '""'], outputs: ['""'] });
    }
    await wrapper.vm.$nextTick();
    const trs = wrapper.findAll('tbody tr');
    expect(trs.length).toBeLessThan(50);
  });

  it('gap-21: in_app_search_highlights_matches', async () => {
    const wrapper = mount(DmnGridManual);
    wrapper.vm.rows = [
      { id: '1', inputs: ['"MatchThis"'], outputs: ['"Output"'] }
    ];
    await wrapper.vm.$nextTick();
    
    wrapper.vm.onSearchHighlight({ query: 'match', matches: [{ rowIdx: 0, colIdx: 'in-0' }], currentIndex: 0 });
    await wrapper.vm.$nextTick();
    
    const activeMatch = wrapper.find('.active-match');
    expect(activeMatch.exists()).toBe(true);
  });

  it('gap-25: xml_load_retains_editability', async () => {
    const wrapper = mount(DmnGridManual);
    wrapper.vm.rows = [
      { id: 'xml-1', inputs: ['"FromXML"'], outputs: ['"FromXMLOut"'] }
    ];
    await wrapper.vm.$nextTick();
    
    const input = wrapper.find('tbody tr:first-child td input');
    expect(input.attributes('readonly')).toBeUndefined();
  });

  it('renders_split_view_chat_and_grid (CA-26)', () => {
    const wrapper = mount(DmnIntelligence);
    const chatPanel = wrapper.find('.chat-panel');
    const gridPanel = wrapper.find('.grid-panel');
    expect(chatPanel.exists()).toBe(true);
    expect(gridPanel.exists()).toBe(true);
  });

  it('validates_feel_syntax_realtime (CA-28)', async () => {
    const wrapper = mount(DmnGridManual);
    await wrapper.vm.$nextTick();
    const firstInput = wrapper.find('tbody tr:first-child td input');
    
    await firstInput.setValue('abcd');
    expect(firstInput.classes()).toContain('border-red-500');

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
    while (wrapper.vm.rows.length < 100) {
      wrapper.vm.rows.push({ id: `mock-${wrapper.vm.rows.length}`, inputs: ['""'], outputs: ['""'] });
    }
    await wrapper.vm.$nextTick();
    const addBtn = wrapper.find('button.btn-add-row');
    expect(addBtn.attributes('disabled')).toBeDefined();
  });
});
