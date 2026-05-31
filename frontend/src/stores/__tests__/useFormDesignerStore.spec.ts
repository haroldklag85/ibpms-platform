import { describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useFormDesignerStore } from '../useFormDesignerStore';

describe('useFormDesignerStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('AvailableStages_Computed_Removes_Duplicates', () => {
    const store = useFormDesignerStore();
    store.canvasFields = [
      { id: 'f1', type: 'text', stage: 'START_EVENT' },
      { id: 'f2', type: 'text', stage: 'INSPECTION' },
      { id: 'f3', type: 'text', stage: 'VALUATION' },
      { id: 'f4', type: 'text', stage: 'INSPECTION' },
      { id: 'f5', type: 'text', stage: 'ALL' }
    ];
    
    expect(store.availableStages).toEqual(['INSPECTION', 'VALUATION']);
  });

  it('MockPath_Returns_Array_For_FieldArray', () => {
    const store = useFormDesignerStore();
    store.canvasFields = [
      {
        id: 'grid1',
        camundaVariable: 'gridData',
        type: 'field_array',
        children: [
          { id: 'c1', camundaVariable: 'col1', type: 'text' },
          { id: 'c2', camundaVariable: 'col2', type: 'number' }
        ]
      }
    ];

    const fuzzerPayloadRef = { value: '' };
    store.generateMockPath('happy', fuzzerPayloadRef);

    const parsed = JSON.parse(fuzzerPayloadRef.value);
    expect(Array.isArray(parsed.gridData)).toBe(true);
    expect(parsed.gridData.length).toBe(1);
    expect(parsed.gridData[0].col1).toBe('Dummy Data');
    expect(parsed.gridData[0].col2).toBe(42);
  });
});
