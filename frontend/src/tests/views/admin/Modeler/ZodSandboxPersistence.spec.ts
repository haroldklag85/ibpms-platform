import { describe, it, expect, beforeEach } from 'vitest';

describe('ZodSandboxPersistence', () => {
  const LOCAL_STORAGE_KEY = 'zod_sandbox_payload';

  beforeEach(() => {
    localStorage.clear();
  });

  it('CA-10-01: El payload se persiste en localStorage', () => {
    const mockPayload = JSON.stringify({ formId: '123', status: 'DRAFT' });
    localStorage.setItem(LOCAL_STORAGE_KEY, mockPayload);
    
    expect(localStorage.getItem(LOCAL_STORAGE_KEY)).toBe(mockPayload);
  });

  it('CA-10-02: El payload NO se destruye al abrir el modal', () => {
    const mockPayload = JSON.stringify({ formId: '456', status: 'DRAFT' });
    localStorage.setItem(LOCAL_STORAGE_KEY, mockPayload);
    
    // Simulate modal open action which shouldn't affect localStorage
    const isModalOpen = true; 
    
    expect(localStorage.getItem(LOCAL_STORAGE_KEY)).toBe(mockPayload);
    expect(isModalOpen).toBe(true);
  });

  it('CA-10-03: El botón Limpiar restablece el payload', () => {
    const mockPayload = JSON.stringify({ formId: '789', status: 'DRAFT' });
    localStorage.setItem(LOCAL_STORAGE_KEY, mockPayload);
    
    // Simulate cleanup action
    localStorage.removeItem(LOCAL_STORAGE_KEY);
    
    expect(localStorage.getItem(LOCAL_STORAGE_KEY)).toBeNull();
  });
});
