// @Traceability: US-003 - CA-71
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import { createPinia, setActivePinia } from 'pinia'
import { useRoute } from 'vue-router'
import apiClient from '@/services/apiClient'
import { useFormDesignerStore } from '@/stores/useFormDesignerStore'

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

describe('CA-71: Máquina del Tiempo JSON / Soft-Versioning Local', () => {
  const mountOptions = (pinia: any) => ({
    global: {
      plugins: [pinia],
      stubs: {
        VueMonacoEditor: true,
        VueDraggableNext: true,
        Vue3Lottie: true,
        Teleport: true
      }
    }
  });

  beforeEach(() => {
    (useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: { id: 'test-id', formKey: 'test-process-key' },
      params: { processKey: 'test-process-key' }
    });
    vi.clearAllMocks();
    localStorage.clear();
    
    // cast to any for getBpmnVariables mock
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] });
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] });
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
  })

  it('1. FormDesigner.vue mantiene una lista local de instantáneas de canvasFields', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    const store = useFormDesignerStore();

    // Set initial canvasFields
    store.canvasFields = [{ id: 'field1', type: 'text', label: 'Campo 1' }];

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Modify canvasFields to trigger snapshot capture
    store.canvasFields.push({ id: 'field2', type: 'number', label: 'Campo 2' });
    await wrapper.vm.$nextTick();
    
    // We expect some localSnapshots in localStorage under 'form_local_snapshots'
    const saved = localStorage.getItem('form_local_snapshots');
    expect(saved).toBeTruthy();
    const parsed = JSON.parse(saved!);
    expect(parsed.length).toBeGreaterThan(0);
    expect(parsed[0].canvasFields).toEqual([{ id: 'field1', type: 'text', label: 'Campo 1' }]);
  })

  it('2. Modificar canvasFields captura automáticamente una nueva instantánea tras un cambio (deep watcher)', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    const store = useFormDesignerStore();

    store.canvasFields = [{ id: 'field1', type: 'text', label: 'Campo 1' }];
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Modify a property inside a field deeply
    store.canvasFields[0].label = 'Campo 1 Modificado';
    await wrapper.vm.$nextTick();
    
    vi.useFakeTimers();
    store.canvasFields.push({ id: 'field3', type: 'date', label: 'Campo 3' });
    vi.advanceTimersByTime(2000); // advance if we have a debounce
    await wrapper.vm.$nextTick();
    vi.useRealTimers();

    const saved = localStorage.getItem('form_local_snapshots');
    expect(saved).toBeTruthy();
    const parsed = JSON.parse(saved!);
    const lastSnapshot = parsed[parsed.length - 1];
    expect(lastSnapshot.canvasFields).toContainEqual(expect.objectContaining({ id: 'field3' }));
  })

  it('3. Al hacer clic en "Historial JSON" bajo el dropdown se abre el modal de instantáneas locales', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    
    // Seed some snapshots
    const mockSnapshots = [
      { id: '1', timestamp: Date.now() - 15 * 60000, canvasFields: [{ id: 'f1', type: 'text', label: 'Old' }] }
    ];
    localStorage.setItem('form_local_snapshots', JSON.stringify(mockSnapshots));

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Find the button "Historial JSON" and click it
    const buttons = wrapper.findAll('button');
    const historyBtn = buttons.find(btn => btn.text().includes('Historial JSON'));
    expect(historyBtn).toBeTruthy();
    
    await historyBtn!.trigger('click');
    await wrapper.vm.$nextTick();

    // Verify modal is open. We look for a title like "Historial de Instantáneas Locales"
    expect(wrapper.html()).toContain('Historial de Instantáneas Locales');
  })

  it('4. La lista de instantáneas muestra un indicador de tiempo relativo basado en Date.now()', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    
    const now = Date.now();
    const mockSnapshots = [
      { id: '1', timestamp: now - 15 * 60 * 1000, canvasFields: [] }, // 15 mins ago
      { id: '2', timestamp: now - 60 * 60 * 1000, canvasFields: [] }, // 1 hour ago
    ];
    localStorage.setItem('form_local_snapshots', JSON.stringify(mockSnapshots));

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Open local history modal
    const buttons = wrapper.findAll('button');
    const historyBtn = buttons.find(btn => btn.text().includes('Historial JSON'));
    await historyBtn!.trigger('click');
    await wrapper.vm.$nextTick();

    // Verify relative times are displayed
    const htmlContent = wrapper.html();
    expect(htmlContent).toContain('Hace 15 minutos');
    expect(htmlContent).toContain('Hace 1 hora');
  })

  it('5. Al hacer clic en "Restaurar" para una instantánea específica, se restablecen los canvasFields de la tienda', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    const store = useFormDesignerStore();

    store.canvasFields = [{ id: 'f-current', type: 'text', label: 'Current Field' }];

    const now = Date.now();
    const mockSnapshots = [
      { id: 'snap-1', timestamp: now - 15 * 60 * 1000, canvasFields: [{ id: 'f-old', type: 'number', label: 'Old Field' }] }
    ];
    localStorage.setItem('form_local_snapshots', JSON.stringify(mockSnapshots));

    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Open local history modal
    const buttons = wrapper.findAll('button');
    const historyBtn = buttons.find(btn => btn.text().includes('Historial JSON'));
    await historyBtn!.trigger('click');
    await wrapper.vm.$nextTick();

    // Find the restore button inside the modal and click it
    const restoreButtons = wrapper.findAll('button').filter(btn => btn.text().includes('Restaurar'));
    expect(restoreButtons.length).toBeGreaterThanOrEqual(1);

    await restoreButtons[0].trigger('click');
    await wrapper.vm.$nextTick();

    // Verify store canvasFields has been restored
    expect(store.canvasFields).toEqual([{ id: 'f-old', type: 'number', label: 'Old Field' }]);
  })
})
