// @Traceability: US-003 - CA-81, CA-82
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { mount, flushPromises } from '@vue/test-utils';
import FormRenderer from '@/components/forms/FormRenderer.vue';
import apiClient from '@/services/apiClient';

// Mock apiClient to prevent actual HTTP calls during testing
vi.mock('@/services/apiClient', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: {} }),
    post: vi.fn().mockResolvedValue({ data: {} }),
    put: vi.fn().mockResolvedValue({ data: {} })
  }
}));

describe('[🕵️ QA - E2E] FormRenderer CA-81 & CA-82', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
    vi.restoreAllMocks();

    // Mock navigator.sendBeacon if it doesn't exist in the testing environment
    if (typeof navigator.sendBeacon === 'undefined') {
      Object.defineProperty(navigator, 'sendBeacon', {
        value: vi.fn().mockReturnValue(true),
        writable: true,
        configurable: true
      });
    } else {
      vi.spyOn(navigator, 'sendBeacon').mockImplementation(() => true);
    }
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // 1. CA-81: Anchoring Form Version
  it('CA-81: should render form structure strictly based on the schema version snapshot from the BFF context', async () => {
    const schemaV1 = [
      {
        id: 'FIELD_V1',
        type: 'text',
        label: 'V1 Field',
        camundaVariable: 'v1Field',
        required: true
      }
    ];

    const formData = {
      v1Field: 'Initial Value'
    };

    const wrapper = mount(FormRenderer, {
      props: {
        schema: schemaV1,
        modelValue: formData
      }
    });

    await flushPromises();

    // Query inside shadow root of shadow DOM host container
    const host = wrapper.find({ ref: 'hostRef' }).element as HTMLElement;
    const shadowRoot = host.shadowRoot;
    expect(shadowRoot).not.toBeNull();

    // V1 field should render
    const fieldWrapper = shadowRoot!.querySelector('#field-wrapper-FIELD_V1');
    expect(fieldWrapper).not.toBeNull();
    expect(fieldWrapper!.textContent).toContain('V1 Field');

    // Newer V2 fields should not be rendered or present in the DOM
    const v2FieldWrapper = shadowRoot!.querySelector('#field-wrapper-FIELD_V2');
    expect(v2FieldWrapper).toBeNull();
  });

  // 2. CA-82: Auto-save Inputs to Local Storage
  it('CA-82: should autosave inputs to local storage on value changes targeting key draft_task_${taskId}', async () => {
    const schema = [
      {
        id: 'FIELD_NAME',
        type: 'text',
        label: 'Name',
        camundaVariable: 'customerName',
        required: false
      }
    ];

    const formData = {
      customerName: ''
    };

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: formData,
        taskId: 'task-123-autosave'
      }
    });

    await flushPromises();

    // Simulate input value change in formData
    formData.customerName = 'Jane Doe';
    await wrapper.setProps({ modelValue: { ...formData } });
    await flushPromises();

    // Verify draft is stored in local storage under key 'draft_task_task-123-autosave'
    const storedDraft = localStorage.getItem('draft_task_task-123-autosave');
    expect(storedDraft).not.toBeNull();
    const parsedDraft = JSON.parse(storedDraft!);
    expect(parsedDraft.customerName).toBe('Jane Doe');
  });

  // 3. CA-82: Ghost Data Cleanup on Visibility Change
  it('CA-82: should clean up hidden fields from form data to prevent submitting invalid/inactive inputs', async () => {
    const schema = [
      {
        id: 'FIELD_HAS_DISCOUNT',
        type: 'checkbox',
        label: 'Has Discount?',
        camundaVariable: 'hasDiscount',
        required: false
      },
      {
        id: 'FIELD_DISCOUNT_CODE',
        type: 'text',
        label: 'Discount Code',
        camundaVariable: 'discountCode',
        required: false,
        visibilityCondition: 'data.hasDiscount == true'
      }
    ];

    const formData = {
      hasDiscount: true,
      discountCode: 'PROMO50'
    };

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: formData
      }
    });

    await flushPromises();

    // Initially discountCode is visible and present
    expect(formData.discountCode).toBe('PROMO50');

    // Change hasDiscount to false to make discountCode field invisible
    formData.hasDiscount = false;
    await wrapper.setProps({ modelValue: { ...formData } });
    
    // We trigger dynamic validation and nextTick to let the watcher react
    await wrapper.vm.$nextTick();
    await flushPromises();

    // discountCode should have been deleted from formData (ghost data cleanup)
    const emitted = wrapper.emitted('update:modelValue');
    const updatedData = emitted ? emitted[emitted.length - 1][0] : wrapper.props('modelValue');
    expect(updatedData.discountCode).toBeUndefined();
  });

  // 4. CA-82: S3 Orphan Cleanup via navigator.sendBeacon
  it('CA-82: should clean up temporary S3 uploaded file orphans using sendBeacon on unmount if not completed', async () => {
    const schema = [
      {
        id: 'FIELD_FILE',
        type: 'text',
        label: 'Upload File',
        camundaVariable: 'fileUpload'
      }
    ];

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: {}
      }
    });

    await flushPromises();

    // Mark some files as uploaded
    wrapper.vm.markFileUploaded('uuid-document-1');
    wrapper.vm.markFileUploaded('uuid-document-2');

    // Unmount without submitting
    wrapper.unmount();

    // navigator.sendBeacon should be called to clean up the orphans
    expect(navigator.sendBeacon).toHaveBeenCalledOnce();
    expect(navigator.sendBeacon).toHaveBeenCalledWith(
      '/api/v1/documents/cleanup',
      expect.stringContaining('["uuid-document-1","uuid-document-2"]')
    );
  });

  it('CA-82: should NOT run sendBeacon orphan cleanup on unmount if the form has been completed/submitted', async () => {
    const schema = [];
    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: {}
      }
    });

    await flushPromises();

    wrapper.vm.markFileUploaded('uuid-completed-doc');

    // Simulate completion/submission
    wrapper.vm.notifySubmit();

    // Unmount
    wrapper.unmount();

    // navigator.sendBeacon should NOT be called
    expect(navigator.sendBeacon).not.toHaveBeenCalled();
  });

  // 5. CA-82: Smart Buttons Error Handling (No alert)
  it('CA-82: should catch submission errors and render a friendly UI error alert banner without using window.alert', async () => {
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
    
    // We expect the FormRenderer or custom submit button handler to display an error banner
    // when a submission API call fails.
    const schema = [
      {
        id: 'SUBMIT_BTN',
        type: 'button_submit',
        label: 'Submit Form'
      }
    ];

    const wrapper = mount(FormRenderer, {
      props: {
        schema,
        modelValue: {}
      }
    });

    await flushPromises();

    // We configure the onSubmit API call to reject with an error
    vi.mocked(apiClient.post).mockRejectedValue(new Error('Network error or Camunda crash'));

    // Trigger submit / try submitting. In a real scenario, this might be triggered by clicking a button.
    // Let's call the submission method or simulate button click if it renders one, or mock submission trigger.
    const host = wrapper.find({ ref: 'hostRef' }).element as HTMLElement;
    const shadowRoot = host.shadowRoot;
    expect(shadowRoot).not.toBeNull();

    // Let's check if the submission error shows an error banner
    // This is expected to FAIL in the RED phase.
    
    // Simulating submitting the form:
    // If the component has a method to submit, we call it. If it expects a form submit, we trigger it.
    // Let's trigger a submission failure
    if (wrapper.vm.submitForm) {
      await wrapper.vm.submitForm();
    } else {
      // Force test failure by asserting that a warning banner is present in shadow DOM, which won't be yet.
      const errorBanner = shadowRoot!.querySelector('.error-banner, .alert-danger, [role="alert"]');
      expect(errorBanner).not.toBeNull();
    }

    // Expect window.alert to NEVER be called
    expect(alertSpy).not.toHaveBeenCalled();
  });
});
