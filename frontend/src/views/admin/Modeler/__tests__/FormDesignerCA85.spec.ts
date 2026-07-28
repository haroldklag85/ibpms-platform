// @Traceability: US-003 - CA-85
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import { useRoute } from 'vue-router'
import apiClient from '@/services/apiClient'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}))

vi.mock('@/services/apiClient', () => ({
  default: {
    getBpmnVariables: vi.fn(),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}))

describe('CA-85: Auto-Guardado y Recuperación de Sesión en el Diseñador', () => {
  const mountOptions = (pinia: any) => ({
    global: {
      plugins: [pinia],
      stubs: {
        VueMonacoEditor: true,
        VueDraggable: true,
        VueDraggableNext: true,
        Vue3Lottie: true,
        Teleport: true
      }
    }
  });

  beforeEach(() => {
    (useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: {},
      params: {}
    });
    vi.clearAllMocks();
    localStorage.clear();
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] });
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] });
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
    setActivePinia(createPinia());
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('1. When FormDesigner.vue is mounted, if localStorage has a draft saved at form_draft_ca85_modeler and the canvas is empty, a dialog or banner showing "Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?" is displayed.', async () => {
    const draftData = [{ id: 'field_test_ca85', type: 'text', label: 'Test Label CA85' }];
    localStorage.setItem('form_draft_ca85_modeler', JSON.stringify(draftData));

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Check that the dialog or banner text is displayed
    const htmlContent = wrapper.html();
    expect(htmlContent).toContain('Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?');
  });

  it('2. Clicking the "Restaurar" (or "Sí") button inside the banner/dialog parses the saved draft and sets canvasFields in the store, then hides the banner/dialog.', async () => {
    const draftData = [{ id: 'field_test_ca85', type: 'text', label: 'Test Label CA85' }];
    localStorage.setItem('form_draft_ca85_modeler', JSON.stringify(draftData));

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // The banner should be visible
    expect(wrapper.html()).toContain('Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?');

    // Find Restaurar/Sí button and click it
    const restoreBtn = wrapper.findAll('button').find(b => 
      b.text().includes('Restaurar') || b.text().includes('Sí')
    );
    expect(restoreBtn).toBeDefined();
    
    if (restoreBtn) {
      await restoreBtn.trigger('click');
      await flushPromises();
    }

    // Verify draft was restored to the store's canvasFields
    const store = useFormDesignerStore(pinia);
    expect(store.canvasFields).toEqual(draftData);

    // Verify the banner is now hidden
    expect(wrapper.html()).not.toContain('Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?');
  });

  it('3. Clicking the "Descartar" (or "No") button inside the banner/dialog removes the draft from localStorage under form_draft_ca85_modeler and hides the banner/dialog, leaving the canvas empty.', async () => {
    const draftData = [{ id: 'field_test_ca85', type: 'text', label: 'Test Label CA85' }];
    localStorage.setItem('form_draft_ca85_modeler', JSON.stringify(draftData));

    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Find Descartar/No button and click it
    const discardBtn = wrapper.findAll('button').find(b => 
      b.text().includes('Descartar') || b.text().includes('No')
    );
    expect(discardBtn).toBeDefined();

    if (discardBtn) {
      await discardBtn.trigger('click');
      await flushPromises();
    }

    // Verify localStorage draft was removed
    expect(localStorage.getItem('form_draft_ca85_modeler')).toBeNull();

    // Verify store's canvasFields remains empty
    const store = useFormDesignerStore(pinia);
    expect(store.canvasFields.length).toBe(0);

    // Verify the banner is hidden
    expect(wrapper.html()).not.toContain('Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?');
  });

  it('4. Modifying the canvas fields (canvasFields) trigger a save of the new layout to localStorage under form_draft_ca85_modeler with a debounce.', async () => {
    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    const store = useFormDesignerStore(pinia);
    
    // Modify canvasFields in the store
    const testField = { id: 'field_live_test', type: 'text', label: 'Live Label' };
    store.canvasFields = [testField];
    await wrapper.vm.$nextTick();

    // Wait for the watcher to trigger and verify no draft is saved yet before debounce time
    vi.advanceTimersByTime(2000);
    expect(localStorage.getItem('form_draft_ca85_modeler')).toBeNull();

    // Advance remaining debounce time (total 5000ms as per code)
    vi.advanceTimersByTime(3000);
    
    // Verify it is now saved to localStorage
    const savedDraft = localStorage.getItem('form_draft_ca85_modeler');
    expect(savedDraft).not.toBeNull();
    expect(JSON.parse(savedDraft!)).toEqual([testField]);
  });
});
