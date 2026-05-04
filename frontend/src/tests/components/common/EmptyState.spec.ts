import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import EmptyState from '@/components/common/EmptyState.vue';

describe('EmptyState.vue', () => {
  it('renderiza con variant y props correctos', () => {
    const wrapper = mount(EmptyState, {
      props: {
        variant: 'no-tasks',
        title: 'Sin tareas',
        subtitle: 'No hay nada por hacer',
        actionLabel: 'Recargar'
      }
    });

    expect(wrapper.find('[data-testid="empty-state-icon"] span').text()).toBe('inventory_2');
    expect(wrapper.find('[data-testid="empty-state-title"]').text()).toBe('Sin tareas');
    expect(wrapper.find('[data-testid="empty-state-subtitle"]').text()).toBe('No hay nada por hacer');
    expect(wrapper.find('[data-testid="empty-state-action"]').text()).toBe('Recargar');
  });

  it('emite el evento action al hacer clic en el botón', async () => {
    const wrapper = mount(EmptyState, {
      props: {
        variant: 'no-results',
        actionLabel: 'Buscar de nuevo'
      }
    });

    await wrapper.find('[data-testid="empty-state-action"]').trigger('click');
    expect(wrapper.emitted('action')).toBeTruthy();
  });
});
