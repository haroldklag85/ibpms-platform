import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAgileStore } from '../stores/agileStore';
import { ItemType, ItemStatus } from '../types/agile';
import axios from 'axios';

vi.mock('axios');
const mockedAxios = vi.mocked(axios, true);

describe('AgileStore - TDD Zero-Trust Validation (Standalone)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should prevent tag creation if label is too short (Zod validation)', async () => {
    const store = useAgileStore();
    
    // Inject mock task
    store.backlogItems = [{
       id: 'item-1', title: 'Test Task', type: ItemType.STORY, status: ItemStatus.TO_DO, tags: [], assignees: []
    }];

    // Attempting to create a tag with empty string
    await expect(store.createAndAssignTag('item-1', 'A')).rejects.toThrow('Tag validation failed');
    
    expect(mockedAxios.post).not.toHaveBeenCalled();
    expect(store.backlogItems[0].tags.length).toBe(0);
  });

  it('should successfully create and assign tag', async () => {
    const store = useAgileStore();
    
    store.backlogItems = [{
       id: 'item-1', title: 'Test Task', type: ItemType.STORY, status: ItemStatus.TO_DO, tags: [], assignees: []
    }];

    mockedAxios.post.mockResolvedValueOnce({
      data: { tag: { id: 'tag-123', label: 'Backend', color: '#000000' } }
    });

    await store.createAndAssignTag('item-1', 'Backend', '#000000');

    expect(mockedAxios.post).toHaveBeenCalledWith(
      '/api/v1/agile/items/item-1/tags', 
      expect.objectContaining({ label: 'Backend', color: '#000000' })
    );

    expect(store.backlogItems[0].tags.length).toBe(1);
    expect(store.backlogItems[0].tags[0].label).toBe('Backend');
  });

  it('should optimisticly move items across sprints', async () => {
    const store = useAgileStore();
    
    store.backlogItems = [{
       id: 'item-1', title: 'Test Task', type: ItemType.STORY, status: ItemStatus.TO_DO, tags: [], assignees: [], sprintId: null
    }];

    mockedAxios.put.mockResolvedValueOnce({});

    await store.moveItemToSprint('item-1', 'sprint-100');

    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/agile/items/item-1/sprint', { sprintId: 'sprint-100' });
    expect(store.backlogItems[0].sprintId).toBe('sprint-100');
  });

  it('should handle massive backlog size and Smart Archive toggle gracefully (CA-12 & CA-8)', () => {
    const store = useAgileStore();
    const massivePayload = [];
    
    // Inject 10,000 items
    for(let i=0; i<10000; i++) {
       massivePayload.push({
          id: `item-${i}`,
          title: `Massive Scalability Item ${i}`,
          type: ItemType.BUG,
          status: i % 2 === 0 ? ItemStatus.DONE : ItemStatus.TO_DO,
          tags: [],
          assignees: []
       });
    }
    store.backlogItems = massivePayload as any;

    expect(store.filteredBacklogItems.length).toBe(5000);

    // Toggle Smart Archive (Hides DONE items, which are 50%)
    store.isArchiveSimulated = true;
    
    expect(store.filteredBacklogItems.length).toBe(5000);
    expect(store.filteredBacklogItems.every(i => i.status !== ItemStatus.DONE)).toBe(true);
  });
});
