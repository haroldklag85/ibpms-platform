import { mount, flushPromises } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import ConnectorBuilder from '@/views/admin/Integration/ConnectorBuilder.vue';
import apiClient from '@/services/apiClient';

// Mock the apiClient module
vi.mock('@/services/apiClient', () => {
  return {
    default: {
      post: vi.fn(),
      get: vi.fn()
    }
  };
});

describe('ConnectorBuilder.vue Regression Test Suite (Finding 3)', () => {
  let mockPost: any;

  beforeEach(() => {
    vi.clearAllMocks();
    mockPost = vi.mocked(apiClient.post);
  });

  it('Upon mounting, apiSecret has masked value and type is password', async () => {
    const wrapper = mount(ConnectorBuilder, {
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              auth: {
                user: { username: 'qa_worker', roles: ['ROLE_APPROVER'] },
                activeRole: 'ROLE_APPROVER'
              }
            }
          })
        ],
        stubs: {
          VueMonacoEditor: true,
          RouterLink: true
        }
      }
    });

    const input = wrapper.find('input[placeholder="••••••••••••••••"]');
    expect(input.exists()).toBe(true);
    expect(input.attributes('type')).toBe('password');

    // Verify it is bound to the value '••••••••••••••••'
    const inputEl = input.element as HTMLInputElement;
    expect(inputEl.value).toBe('••••••••••••••••');
  });

  it('Clicking "👁️ Monitorear" sends audit event and displays the real secret', async () => {
    mockPost.mockResolvedValueOnce({ data: { success: true } });

    const wrapper = mount(ConnectorBuilder, {
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              auth: {
                user: { username: 'qa_worker', roles: ['ROLE_APPROVER'] },
                activeRole: 'ROLE_APPROVER'
              }
            }
          })
        ],
        stubs: {
          VueMonacoEditor: true,
          RouterLink: true
        }
      }
    });

    // Locate the "👁️ Monitorear" button
    const revealBtn = wrapper.find('button.absolute');
    expect(revealBtn.exists()).toBe(true);
    expect(revealBtn.text()).toContain('👁️ Monitorear');

    // Trigger the click event
    await revealBtn.trigger('click');

    // Flush all unresolved promises (like apiClient.post)
    await flushPromises();

    // Verify the input has changed type to "text" and contains the real secret
    const input = wrapper.find('input[placeholder="••••••••••••••••"]');
    expect(input.exists()).toBe(true);
    expect(input.attributes('type')).toBe('text');

    const inputEl = input.element as HTMLInputElement;
    expect(inputEl.value).toBe('ibpms_sk_live_9f8g7h6j...');
  });
});
