import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import DynamicForm from '@/components/forms/DynamicForm.vue';
import { z } from 'zod';

describe('DynamicForm.vue', () => {
    beforeEach(() => {
        localStorage.clear();
        vi.useFakeTimers();
    });

    const mockSchema = {
        title: 'Test Form',
        description: 'Test description',
        fields: [
            { key: 'username', label: 'User Name', type: 'string', defaultValue: 'defaultUser' },
            { key: 'age', label: 'Age', type: 'number' },
        ]
    };

    it('renders the form correctly based on the schema', () => {
        const wrapper = mount(DynamicForm as any, {
            props: { schema: mockSchema }
        });

        // The title should be rendered
        expect(wrapper.text()).toContain('Test Form');
        expect(wrapper.text()).toContain('Test description');

        // Buttons should be rendered
        expect(wrapper.text()).toContain('Cancelar');
        expect(wrapper.text()).toContain('Completar Tarea');
    });

    it('emits a cancel event on Cancelar button click', async () => {
        const wrapper = mount(DynamicForm as any, {
            props: { schema: mockSchema }
        });

        const buttons = wrapper.findAll('button');
        const cancelBtn = buttons.find(b => b.text().includes('Cancelar'));
        await cancelBtn!.trigger('click');

        expect(wrapper.emitted()).toHaveProperty('cancel');
    });

    it('initializes with default values and submits them properly', async () => {
        const wrapper = mount(DynamicForm as any, {
            props: { schema: mockSchema }
        });

        const form = wrapper.find('form');
        await form.trigger('submit.prevent');

        // Fast forward the setTimeout
        vi.runAllTimers();

        expect(wrapper.emitted()).toHaveProperty('submit');
        const submitEvent = wrapper.emitted('submit') as any[];

        // Verify default value injection
        expect(submitEvent[0][0]).toEqual({
            username: 'defaultUser',
            age: null
        });
    });

    it('should validate zod schema prior to submit and prevent emission if invalid', async () => {
        const strictZodSchema = z.object({
            username: z.string().min(5),
            age: z.number().min(18)
        });

        const wrapper = mount(DynamicForm as any, {
            props: {
                schema: mockSchema,
                zodSchema: strictZodSchema
            }
        });

        const form = wrapper.find('form');

        // Act: Submission goes through with faulty default inputs (age is null)
        await form.trigger('submit.prevent');
        vi.runAllTimers();

        // Assert
        expect(wrapper.emitted('submit')).toBeUndefined();
    });
});
