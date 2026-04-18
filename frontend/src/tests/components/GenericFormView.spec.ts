import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';

// Como el componente aún no ha sido implementado, aplicamos TDD estricto.
// Importaremos el componente esperado. Cuando el test corra, fallará por archivo no encontrado
// o por fallas de aserción.
import GenericFormView from '../../../views/admin/BPMN/GenericFormView.vue';

describe('GenericFormView.vue (US-039 CA-4 al CA-8)', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.clearAllMocks();
  });

  // ==========================================
  // CA-4: Validación del Cuerpo Editable
  // ==========================================
  it('CA-4: Debería renderizar exactamente 3 campos editables (Observaciones, Management Result, Adjuntos)', () => {
    const wrapper = mount(GenericFormView, {
      props: {
        taskId: 't-123',
        prefillData: {}
      }
    });

    const editableFields = wrapper.findAll('.generic-form-editable-field');
    expect(editableFields.length).toBe(3); // Fail-fast: Currently 0 because component is missing
  });

  it('CA-4: Botón [Enviar] deshabilitado si textarea vacía o < 5 caracteres', async () => {
    const wrapper = mount(GenericFormView, {
      props: { taskId: 't-123', prefillData: {} }
    });

    const submitBtn = wrapper.find('[data-testid="btn-submit-generic"]');
    expect(submitBtn.attributes('disabled')).toBeDefined();

    const textarea = wrapper.find('[data-testid="textarea-observations"]');
    await textarea.setValue('ok'); // 2 chars
    expect(submitBtn.attributes('disabled')).toBeDefined();

    await textarea.setValue('Todo correcto'); // Valid
    expect(submitBtn.attributes('disabled')).toBeUndefined();
  });

  // ==========================================
  // CA-5: Whitelist Regex por Proceso
  // ==========================================
  it('CA-5: Debería mostrar mensaje de "No hay metadatos" si prefillData está vacío', () => {
    const wrapper = mount(GenericFormView, {
      props: { taskId: 't-123', prefillData: {} }
    });

    expect(wrapper.text()).toContain('No hay metadatos disponibles');
  });

  it('CA-5: Debería renderizar las variables extraídas en modo Solo Lectura', () => {
    const prefill = { "Case_ID": "12345", "Client_Name": "Harolt" };
    const wrapper = mount(GenericFormView, {
      props: { taskId: 't-123', prefillData: prefill }
    });

    const inputs = wrapper.findAll('.generic-prefill-input');
    expect(inputs.length).toBe(2);
    expect(inputs[0].attributes('readonly')).toBeDefined();
  });

  // ==========================================
  // CA-6: Roles VIP Pre-Flight (Frontend View)
  // ==========================================
  it('CA-6: Debería adornar con ícono VIP si el rol es restringido', () => {
    const prefill = { "isVipRestricted": true };
    const wrapper = mount(GenericFormView, {
      props: { taskId: 't-123', prefillData: prefill }
    });

    const vipIcon = wrapper.find('[data-testid="icon-vip-star"]');
    expect(vipIcon.exists()).toBe(true);
  });

  // ==========================================
  // CA-7: Persistencia y Auto-Guardado (Drafts)
  // ==========================================
  it('CA-7: Debería disparar auto-guardado a los 10 segundos de inactividad', async () => {
    const putDraftSpy = vi.fn();
    const wrapper = mount(GenericFormView, {
      props: { taskId: 't-123', prefillData: {} },
      global: {
        mocks: {
          $api: { put: putDraftSpy }
        }
      }
    });

    const textarea = wrapper.find('[data-testid="textarea-observations"]');
    await textarea.setValue('Borrador inicial');

    // Mover el tiempo 10 segundos adelante
    vi.advanceTimersByTime(10000);

    expect(putDraftSpy).toHaveBeenCalledWith('/api/v1/drafts/t-123', expect.any(Object));
  });

  // ==========================================
  // CA-8: Botones de Pánico
  // ==========================================
  it('CA-8: Click al botón [Cancelar] debería desplegar la Modal Justificación', async () => {
    const wrapper = mount(GenericFormView, {
      props: { taskId: 't-123', prefillData: {} }
    });

    const cancelBtn = wrapper.find('[data-testid="btn-panic-cancel"]');
    await cancelBtn.trigger('click');

    const modal = wrapper.find('[data-testid="modal-panic-justification"]');
    expect(modal.exists()).toBe(true);
  });
});
