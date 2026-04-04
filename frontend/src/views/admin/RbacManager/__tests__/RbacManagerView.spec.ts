import { describe, it, expect, vi, beforeEach } from 'vitest';
import { shallowMount } from '@vue/test-utils';
import RbacManagerView from '../RbacManagerView.vue';
// Removido import no existente
import { useRbacStore } from '@/stores/rbacStore';

const fetchRolesMock = vi.fn();

// Mock the store
vi.mock('@/stores/rbacStore', () => {
  return {
    useRbacStore: vi.fn(() => ({
      fetchRoles: fetchRolesMock,
      roles: [],
      isLoading: false
    }))
  };
});

describe('RbacManagerView.vue', () => {
    
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders standard header elements', () => {
    const wrapper = shallowMount(RbacManagerView);
    const headerTitle = wrapper.find('h1');
    expect(headerTitle.exists()).toBe(true);
    expect(headerTitle.text()).toContain('Gobierno de Identidad y Accesos (RBAC)');
  });

  it('calls fetchRoles on mount', () => {
    const wrapper = shallowMount(RbacManagerView);
    expect(fetchRolesMock).toHaveBeenCalledTimes(1);
  });

  it('contains RbacTabs and SecurityAuditLog components', () => {
    const wrapper = shallowMount(RbacManagerView);
    // Because we used shallowMount, custom components are stubbed.
    // They will appear as <rbac-tabs-stub> or similar depending on the tag resolution
    expect(wrapper.findComponent({ name: 'RbacTabs' }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: 'SecurityAuditLog' }).exists()).toBe(true);
  });
});
