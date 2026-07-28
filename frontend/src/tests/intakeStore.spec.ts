import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useIntakeStore } from '../stores/intakeStore';
import axios from 'axios';

vi.mock('axios');
const mockedAxios = vi.mocked(axios, true);

describe('IntakeStore - TDD Zero-Trust Validation', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should fetch tasks successfully and map to state', async () => {
    const store = useIntakeStore();
    
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        tasks: [
          { id: 't-1', status: 'PENDING', payloadPreview: 'test emails' }
        ],
        currentPage: 0,
        totalPages: 1,
        totalElements: 1
      }
    });

    await store.fetchTasks();

    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/intake/triage/tasks', { params: { page: 0, size: 10 } });
    expect(store.tasks.length).toBe(1);
    expect(store.totalElements).toBe(1);
    expect(store.isLoading).toBe(false);
  });

  it('should approve task, invoke correct API and update grid optimistically', async () => {
    const store = useIntakeStore();
    store.tasks = [
      { id: 't-1', source: 'EMAIL_WEBHOOK', receivedAt: '', sender: '', subject: '', payloadPreview: '', slaDeadline: '', status: 'PENDING' }
    ];
    store.totalElements = 1;

    mockedAxios.post.mockResolvedValueOnce({});

    await store.approveTask('t-1', 'PROCESO_QUEJA');

    expect(mockedAxios.post).toHaveBeenCalledWith('/api/v1/intake/triage/tasks/t-1/approve', { processType: 'PROCESO_QUEJA' });
    expect(store.tasks.length).toBe(0);
    expect(store.totalElements).toBe(0);
  });

  it('should prevent rejection if reason is empty or missing (Zero-Trust Logic)', async () => {
    const store = useIntakeStore();
    
    await expect(store.rejectTask('t-1', '')).rejects.toThrow('Rejection reason is mandatory');
    await expect(store.rejectTask('t-1', '   ')).rejects.toThrow('Rejection reason is mandatory');
    
    expect(mockedAxios.post).not.toHaveBeenCalled();
    expect(store.error).toBe('Rejection reason is mandatory');
  });

  it('should reject task, invoke correct API and update grid optimistically when reason is valid', async () => {
    const store = useIntakeStore();
    store.tasks = [
      { id: 't-1', source: 'EMAIL_WEBHOOK', receivedAt: '', sender: '', subject: '', payloadPreview: '', slaDeadline: '', status: 'PENDING' }
    ];
    store.totalElements = 1;

    mockedAxios.post.mockResolvedValueOnce({});

    await store.rejectTask('t-1', 'Spam detected');

    expect(mockedAxios.post).toHaveBeenCalledWith('/api/v1/intake/triage/tasks/t-1/reject', { rejectionReason: 'Spam detected' });
    expect(store.tasks.length).toBe(0);
  });
});
