import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import Workdesk from '@/views/Workdesk.vue';
import { useWorkdeskStore } from '@/stores/useWorkdeskStore';

describe('Pantalla 1: Hybrid Workdesk (US-001)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('Debe renderizar ⚡ BPMN y 📋 KANBAN según el sourceSystem', async () => {
    const store = useWorkdeskStore();
    
    // Inyectamos Mock Data al State Global
    store.items = [
      {
        unifiedId: "BPMN-1",
        sourceSystem: "BPMN",
        originalTaskId: "123",
        title: "Tarea Camunda",
        slaExpirationDate: new Date(Date.now() + 86400000).toISOString(),
        status: "NORMAL",
        assignee: "userA"
      },
      {
        unifiedId: "KANBAN-1",
        sourceSystem: "KANBAN",
        originalTaskId: "456",
        title: "Tarea Agile",
        slaExpirationDate: new Date(Date.now() + 86400000).toISOString(),
        status: "NORMAL",
        assignee: "userB"
      }
    ];

    const wrapper = mount(Workdesk);

    // Esperamos a que el render se pinte
    await wrapper.vm.$nextTick();
    const html = wrapper.html();

    expect(html).toContain('⚡');
    expect(html).toContain('BPMN');
    expect(html).toContain('📋');
    expect(html).toContain('KANBAN');
  });

  it('Debe renderizar el Semáforo SLA (bg-red-100) si la fecha expiró', async () => {
    const store = useWorkdeskStore();
    
    // Hace 1 hora
    const expiredDate = new Date(Date.now() - 3600000).toISOString();

    store.items = [
      {
        unifiedId: "BPMN-EXPIRED",
        sourceSystem: "BPMN",
        originalTaskId: "999",
        title: "Incidente SLA Vencido",
        slaExpirationDate: expiredDate,
        status: "URGENT",
        assignee: "agentX"
      }
    ];

    const wrapper = mount(Workdesk);
    await wrapper.vm.$nextTick();

    const taskCards = wrapper.findAll('td .bg-red-100');
    expect(taskCards.length).toBe(1); // Deberia haber un texto mapeado con este color tailwind en la celda

    const html = wrapper.html();
    expect(html).toContain('Vencido hace 1 hrs'); // Text format validator
  });

});
