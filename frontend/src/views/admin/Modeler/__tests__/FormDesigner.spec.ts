import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue'
import { createPinia } from 'pinia'
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

describe('US-003: Form Designer CA-39 File Upload Constraints', () => {
  const mountOptions = (pinia: any) => ({
    global: { 
      plugins: [pinia],
      stubs: {
        VueMonacoEditor: true,
        VueDraggableNext: true,
        Vue3Lottie: true,
        Teleport: true,
        VueDraggable: true
      }
    }
  });

  beforeEach(() => {
    (useRoute as ReturnType<typeof vi.fn>).mockReturnValue({
      query: { id: 'test-id' },
      params: {}
    });
    vi.clearAllMocks();
    (vi.mocked(apiClient) as any).getBpmnVariables.mockResolvedValue({ data: [] });
    vi.mocked(apiClient.get).mockResolvedValue({ data: { schemaVariables: "[]" } });
  })

  it('CA-39: Renderiza input S3 Bucket UUID', async () => {
    const pinia = createPinia();
    const wrapper = mount(FormDesigner, mountOptions(pinia));
    await flushPromises();

    Reflect.set(wrapper.vm, 'editingField', {
      id: 'fileField1',
      type: 'file',
      label: 'Upload Document',
      s3BucketUuid: ''
    });

    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('UUID de la Bucket S3 (Testing)');
    
    const inputs = wrapper.findAll('input');
    const s3Input = inputs.find(input => input.html().includes('550e8400-e29b-41d4-a716-446655440000'));
    expect(s3Input).toBeDefined();

    if (s3Input) {
      await s3Input.setValue('test-uuid-1234');
      expect((wrapper.vm as any).editingField.s3BucketUuid).toBe('test-uuid-1234');
    }
  });
});
