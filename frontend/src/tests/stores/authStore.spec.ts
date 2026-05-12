import { setActivePinia, createPinia } from 'pinia';
import { useAuthStore } from '@/stores/authStore';
import { beforeEach, describe, it, expect, vi } from 'vitest';

describe('Auth Store (Pinia)', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it('should initialize with empty state', () => {
        const store = useAuthStore();
        expect(store.token).toBeNull();
        expect(store.user).toBeNull();
    });

    it('login() should mutate state and save to localStorage', () => {
        const store = useAuthStore();
        const mockJwt = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjYXJsb3MuYWRtaW4iLCJyb2xlcyI6WyJpYnBtc19yb2xfUk9MRV9VU0VSIiwiaWJwbXNfcm9sX1JPTEVfQVBQUk9WRVIiXX0=.signature';

        store.login(mockJwt);

        expect(store.token).toBe(mockJwt);
        expect(store.user).toEqual({ username: 'carlos.admin', roles: ['ROLE_ROLE_USER', 'ROLE_ROLE_APPROVER'] });
        expect(localStorage.getItem('ibpms_token')).toBe(mockJwt);
    });

    it('logout() should clear state and remove from localStorage', () => {
        const store = useAuthStore();

        // Arrange
        store.login('dummy-token');
        expect(store.token).toBe('dummy-token');

        // Act
        store.logout();

        // Assert
        expect(store.token).toBeNull();
        expect(store.user).toBeNull();
        expect(localStorage.getItem('ibpms_token')).toBeNull();
    });
});
