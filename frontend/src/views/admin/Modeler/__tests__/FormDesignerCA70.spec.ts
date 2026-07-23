// @Traceability: US-003 - CA-70
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import PublicIntake from '@/views/public/PublicIntake.vue'
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

describe('CA-70: Modo Trámite Público Perimetral / Bypass JWT Seguro', () => {
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
    
    // cast to any for getBpmnVariables mock
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] });
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] });
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
    
    // Clean document head/body for script checks
    document.querySelectorAll('script').forEach(el => {
      if (el.src.includes('recaptcha')) el.remove();
    });
  })

  it('1. FormDesigner.vue provee un checkbox #publicToggle vinculado al estado isPublic de la tienda', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    const store = useFormDesignerStore();
    
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    const checkbox = wrapper.find('#publicToggle');
    expect(checkbox.exists()).toBe(true);
    expect(checkbox.element.type).toBe('checkbox');
    
    // Verify initial value of checkbox matches store
    expect((checkbox.element as HTMLInputElement).checked).toBe(false);
    expect(store.isPublic).toBe(false);

    // Toggle checkbox and verify store state is updated
    await checkbox.setValue(true);
    expect(store.isPublic).toBe(true);
  })

  it('2. Al activar el acceso público, se genera un token criptográfico seguro y se añade a publicUrl', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    const store = useFormDesignerStore();
    
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    // Verify initial state
    expect(wrapper.vm.publicUrl).not.toContain('token=');

    // Toggle public access on
    const checkbox = wrapper.find('#publicToggle');
    await checkbox.setValue(true);
    await wrapper.vm.$nextTick();

    // Check that publicUrl has token query param
    expect(wrapper.vm.publicUrl).toContain('token=');
    
    // Parse URL and extract token
    const urlObj = new URL(wrapper.vm.publicUrl);
    const token = urlObj.searchParams.get('token');
    expect(token).toBeTruthy();
    
    // Verify cryptographically secure token format (e.g. 16+ hex chars or UUID-like)
    expect(token!.length).toBeGreaterThanOrEqual(16);
  })

  it('3. PublicIntake.vue inyecta dinámicamente la etiqueta script de Google reCAPTCHA v3 en setup y renderiza su badge', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);

    // Mount PublicIntake component
    const wrapper = mount(PublicIntake, mountOptions(pinia));
    await flushPromises();

    // Verify script injection in DOM
    const scripts = Array.from(document.querySelectorAll('script'));
    const hasRecaptchaScript = scripts.some(script => script.src.includes('recaptcha') && script.src.includes('api.js'));
    expect(hasRecaptchaScript).toBe(true);

    // Verify reCAPTCHA badge or element is rendered in template
    const badge = wrapper.find('.g-recaptcha-badge');
    expect(badge.exists()).toBe(true);
  })

  it('4. Al enviar el formulario, PublicIntake.vue muestra una notificación basada en Vue en lugar de un alert() nativo', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);

    // Spy on window.alert to ensure it is NOT called (compliance with LEY 5)
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

    const wrapper = mount(PublicIntake, mountOptions(pinia));
    await flushPromises();

    // Click submit button
    const submitBtn = wrapper.find('button');
    expect(submitBtn.exists()).toBe(true);
    await submitBtn.trigger('click');
    await wrapper.vm.$nextTick();

    // Verify window.alert was NOT called
    expect(alertSpy).not.toHaveBeenCalled();

    // Verify Vue-based notification or toast is rendered/displayed in DOM
    // For example, checking wrapper text for success message
    const textContent = wrapper.text();
    expect(textContent).toContain('exitosamente'); // or whatever success message is defined in Vue notification

    alertSpy.mockRestore();
  })
})
