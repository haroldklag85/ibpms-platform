// @Traceability: US-003 - CA-78
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import FormDesigner from '@/views/admin/Modeler/FormDesigner.vue';
import { useFormDesignerStore } from '@/stores/useFormDesignerStore';
import { ZodBuilder } from '@/views/admin/Modeler/ZodBuilder';

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    query: { id: 'test-id', formKey: 'test-process-key' },
    params: {}
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}));

vi.mock('@/services/apiClient', () => ({
  default: {
    getBpmnVariables: vi.fn().mockResolvedValue({ data: [] }),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}));

describe('US-003 - CA-78: Factoría Reactiva Zod On-The-Fly y Renderizado Bidireccional', () => {
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
    vi.clearAllMocks();
    const pinia = createPinia();
    setActivePinia(pinia);
  });

  describe('1. ZOD tab computedCode returns proper Zod schema text from canvasFields', () => {
    it('should generate proper Zod schema text, including required state, minLength and maxLength', () => {
      const store = useFormDesignerStore();
      store.activeCodeTab = 'ZOD';
      store.canvasFields = [
        {
          id: 'FIELD_USER',
          camundaVariable: 'username',
          type: 'text',
          label: 'Username',
          required: true,
          minLength: 5,
          maxLength: 20
        },
        {
          id: 'FIELD_AGE',
          camundaVariable: 'age',
          type: 'number',
          label: 'Age',
          required: false
        }
      ];

      const code = store.computedCode;
      expect(code).toContain('import { z } from \'zod\'');
      expect(code).toContain('export const taskSchema = z.object({');
      
      // Verification of basic schema generation (should include the variables)
      expect(code).toContain('username:');
      expect(code).toContain('age:');
      
      // Test for CA-78 requirement: including min/max validation rules (e.g. z.string().min(5))
      expect(code).toContain('z.string().min(5');
      expect(code).toContain('.max(20');
      expect(code).not.toContain('username: z.string().optional()');
      
      // Verification of number type optionals
      expect(code).toContain('age: z.number().optional()');
    });
  });

  describe('2. Monaco Bidirectional Sync (Setting computedCode updates canvasFields reactive metadata)', () => {
    it('should update reactive metadata when editing the Zod schema text', () => {
      const store = useFormDesignerStore();
      store.activeCodeTab = 'ZOD';
      store.canvasFields = [
        {
          id: 'FIELD_USER',
          camundaVariable: 'username',
          type: 'text',
          label: 'Username',
          required: true,
          minLength: 5,
          maxLength: 20
        }
      ];

      // Simulate user editing schema in Monaco: setting minLength=8, maxLength=18
      const newZodCode = `import { z } from 'zod';
export const taskSchema = z.object({
  username: z.string().min(8).max(18), // [GLOBAL]
})`;

      store.computedCode = newZodCode;

      // Verify that store's canvasFields metadata is dynamically updated via bidirectional sync
      expect(store.canvasFields[0].required).toBe(true);
      expect(store.canvasFields[0].minLength).toBe(8);
      expect(store.canvasFields[0].maxLength).toBe(18);

      // Now simulate user setting it to optional and removing min limit
      const optionalZodCode = `import { z } from 'zod';
export const taskSchema = z.object({
  username: z.string().optional(), // [GLOBAL]
})`;

      store.computedCode = optionalZodCode;

      // Verify required is set to false
      expect(store.canvasFields[0].required).toBe(false);
    });

    it('should sync bidirectionally through the component wrapper instance', async () => {
      const pinia = createPinia();
      const store = useFormDesignerStore(pinia);
      store.activeCodeTab = 'ZOD';
      store.canvasFields = [
        {
          id: 'FIELD_USER',
          camundaVariable: 'username',
          type: 'text',
          label: 'Username',
          required: true,
          minLength: 5
        }
      ];

      const wrapper = mount(FormDesigner, mountOptions(pinia));
      await wrapper.vm.$nextTick();

      // Check initially exposed value
      expect(wrapper.vm.computedCode).toContain('username:');

      // Edit Monaco editor value via wrapper.vm
      wrapper.vm.computedCode = `import { z } from 'zod';
export const taskSchema = z.object({
  username: z.string().min(12), // [GLOBAL]
})`;

      await wrapper.vm.$nextTick();

      // Verify field updates
      expect(store.canvasFields[0].minLength).toBe(12);
      expect(store.canvasFields[0].required).toBe(true);
    });
  });

  describe('3. ZodBuilder.buildSchema(canvasFields) correctly creates RAM schema and validates payloads', () => {
    it('should build a schema dynamically and perform successful and failed validations', () => {
      const canvasFields: any[] = [
        {
          id: 'FIELD_USER',
          camundaVariable: 'username',
          type: 'text',
          label: 'Username',
          required: true,
          minLength: 5,
          maxLength: 10
        },
        {
          id: 'FIELD_AGE',
          camundaVariable: 'age',
          type: 'number',
          label: 'Age',
          required: true
        },
        {
          id: 'FIELD_VIP',
          camundaVariable: 'isVip',
          type: 'checkbox',
          label: 'Is VIP',
          required: false
        }
      ];

      // Compile Zod schema dynamically in-memory (Zod Factory in RAM)
      const schema = ZodBuilder.buildSchema(canvasFields);

      // Verify that it is a valid Zod schema by validating payloads
      
      // Happy path validation
      const validPayload = {
        username: 'alexis',
        age: 30,
        isVip: true
      };
      const resultValid = schema.safeParse(validPayload);
      expect(resultValid.success).toBe(true);

      // Fail path: username too short
      const tooShortPayload = {
        username: 'alex',
        age: 30,
        isVip: false
      };
      const resultTooShort = schema.safeParse(tooShortPayload);
      expect(resultTooShort.success).toBe(false);
      if (!resultTooShort.success) {
        expect(resultTooShort.error.errors[0].path).toContain('username');
        expect(resultTooShort.error.errors[0].message).toContain('Mínimo 5 caracteres');
      }

      // Fail path: username too long
      const tooLongPayload = {
        username: 'alexander_the_great',
        age: 30
      };
      const resultTooLong = schema.safeParse(tooLongPayload);
      expect(resultTooLong.success).toBe(false);
      if (!resultTooLong.success) {
        expect(resultTooLong.error.errors[0].path).toContain('username');
        expect(resultTooLong.error.errors[0].message).toContain('Máximo 10 caracteres');
      }

      // Fail path: age is not a number
      const invalidAgePayload = {
        username: 'alexis',
        age: 'thirty'
      };
      const resultInvalidAge = schema.safeParse(invalidAgePayload);
      expect(resultInvalidAge.success).toBe(false);
      if (!resultInvalidAge.success) {
        expect(resultInvalidAge.error.errors[0].path).toContain('age');
      }

      // Fail path: missing required fields
      const missingFieldsPayload = {
        isVip: true
      };
      const resultMissing = schema.safeParse(missingFieldsPayload);
      expect(resultMissing.success).toBe(false);
      if (!resultMissing.success) {
        const paths = resultMissing.error.errors.map(e => e.path[0]);
        expect(paths).toContain('username');
        expect(paths).toContain('age');
      }
    });
  });

  describe('4. Grid (field_array) Monaco-to-Visual Bidirectional Sync', () => {
    // @Traceability: US-003 - CA-78
    it('should parse grid min/max rows and nested input metadata from Zod schema updates', () => {
      const store = useFormDesignerStore();
      store.activeCodeTab = 'ZOD';
      store.canvasFields = [
        {
          id: 'FIELD_EMPLOYEES',
          camundaVariable: 'employees',
          type: 'field_array',
          label: 'Employees List',
          required: true,
          minRows: 2,
          maxRows: 5,
          children: [
            {
              id: 'FIELD_NAME',
              camundaVariable: 'name',
              type: 'text',
              label: 'Employee Name',
              required: true,
              minLength: 3,
              maxLength: 30
            }
          ]
        }
      ];

      // Simulate Monaco editor update modifying the grid rows and nested input min/max/optional constraints
      const newZodCode = `import { z } from 'zod';
export const taskSchema = z.object({
  employees: z.array(z.object({
    name: z.string().min(6).max(25).optional(), // [GLOBAL]
  })).min(3, "Mínimo 3 filas").max(8, "Máximo 8 filas"), // [GRILLA CA-41]
});`;

      store.computedCode = newZodCode;

      // Verify Monaco-to-Visual sync of field_array (grids) parses .min(N) to minRows and .max(M) to maxRows in canvasFields
      expect(store.canvasFields[0].minRows).toBe(3);
      expect(store.canvasFields[0].maxRows).toBe(8);

      // Verify Monaco-to-Visual sync of nested inputs inside the grid
      const nestedFieldName = store.canvasFields[0].children[0];
      expect(nestedFieldName.required).toBe(false);
      expect(nestedFieldName.minLength).toBe(6);
      expect(nestedFieldName.maxLength).toBe(25);
    });
  });
});

