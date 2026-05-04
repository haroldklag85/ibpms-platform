import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import RoleSelectorDropdown from '@/components/shell/RoleSelectorDropdown.vue';
import { useAuthStore } from '@/stores/authStore';

describe('RoleSelectorDropdown.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('NO renderiza si el usuario tiene 1 o 0 roles', () => {
    const wrapper = mount(RoleSelectorDropdown, {
      global: {
        stubs: { transition: false },
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { 
                user: { username: 'test', roles: ['ROLE_OPERADOR'] },
                activeRole: 'ROLE_OPERADOR' 
              }
            }
          })
        ]
      }
    });

    const selector = wrapper.find('[data-testid="role-selector"]');
    expect(selector.exists()).toBe(false);
  });

  it('Abre el dropdown al hacer click con 2+ roles', async () => {
    const wrapper = mount(RoleSelectorDropdown, {
      global: {
        stubs: { transition: false },
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { 
                user: { username: 'test', roles: ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'] },
                activeRole: 'ROLE_SUPER_ADMIN' 
              }
            }
          })
        ]
      }
    });

    const authStore = useAuthStore();
    // @ts-ignore
    authStore.roles = ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'];

    const selector = wrapper.find('[data-testid="role-selector"]');
    expect(selector.exists()).toBe(true);

    const button = wrapper.find('button');
    expect(button.text()).toContain('Super admin');

    // Inicialmente cerrado
    let menu = wrapper.find('.absolute.right-0');
    expect(menu.exists()).toBe(false);

    // Click abre dropdown
    await button.trigger('click');
    menu = wrapper.find('.absolute.right-0');
    expect(menu.exists()).toBe(true);
    expect(menu.text()).toContain('Super admin');
    expect(menu.text()).toContain('Operador');
  });

  it('Llama a switchRole y cierra el dropdown al seleccionar un rol diferente', async () => {
    const wrapper = mount(RoleSelectorDropdown, {
      global: {
        stubs: { transition: false },
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { 
                user: { username: 'test', roles: ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'] },
                activeRole: 'ROLE_SUPER_ADMIN' 
              }
            }
          })
        ]
      }
    });

    const authStore = useAuthStore();
    // @ts-ignore
    authStore.roles = ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'];
    
    // Abre
    await wrapper.find('button').trigger('click');
    
    // Lista de roles en el dropdown
    const roleButtons = wrapper.findAll('.absolute.right-0 button');
    expect(roleButtons.length).toBe(2);

    // Click en el segundo rol (Operador)
    await roleButtons[1].trigger('click');

    expect(authStore.switchRole).toHaveBeenCalledWith('ROLE_OPERADOR');
    
    // Esperar a que la transición termine
    await new Promise(r => setTimeout(r, 100));

    // Dropdown cerrado
    const menu = wrapper.find('.absolute.right-0');
    expect(menu.exists()).toBe(false);
  });
});
