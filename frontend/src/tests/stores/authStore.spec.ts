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
        // Base64 for {"sub":"carlos.admin", "roles":["ROLE_USER", "ROLE_APPROVER"]}
        const mockJwt = 'header.eyJzdWIiOiJjYXJsb3MuYWRtaW4iLCAicm9sZXMiOlsiUk9MRV9VU0VSIiwgIlJPTEVfQVBQUk9WRVIiXX0=.signature';

        store.login(mockJwt);

        expect(store.token).toBe(mockJwt);
        expect(store.user).toEqual({ username: 'carlos.admin', roles: ['ROLE_USER', 'ROLE_APPROVER'] });
        expect(localStorage.getItem('ibpms_token')).toBe(mockJwt);
    });

    it('logout() should clear state and remove from localStorage', () => {
        const store = useAuthStore();

        // Arrange
        const mockJwt = 'header.eyJzdWIiOiJjYXJsb3MuYWRtaW4iLCAicm9sZXMiOlsiUk9MRV9VU0VSIiwgIlJPTEVfQVBQUk9WRVIiXX0=.signature';
        store.login(mockJwt);
        expect(store.token).toBe(mockJwt);

        // Act
        store.logout();

        // Assert
        expect(store.token).toBeNull();
        expect(store.user).toBeNull();
        expect(localStorage.getItem('ibpms_token')).toBeNull();
    });

    describe('hasWritePermission', () => {
        it('should return true for normal users', () => {
            const store = useAuthStore();
            store.user = { username: 'carlos', roles: ['ROLE_USER', 'ROLE_APPROVER'] };
            expect(store.hasWritePermission).toBe(true);
        });

        it('should return false for READONLY role', () => {
            const store = useAuthStore();
            store.user = { username: 'carlos', roles: ['ROLE_READONLY'] };
            expect(store.hasWritePermission).toBe(false);
        });

        it('should return false for AUDITOR role', () => {
            const store = useAuthStore();
            store.user = { username: 'carlos', roles: ['ROLE_AUDITOR'] };
            expect(store.hasWritePermission).toBe(false);
        });

        it('should return true if user has mixed roles including a write role', () => {
            const store = useAuthStore();
            store.user = { username: 'carlos', roles: ['ROLE_AUDITOR', 'ROLE_ADMIN'] };
            expect(store.hasWritePermission).toBe(true);
        });

        it('should return false if user has no roles', () => {
            const store = useAuthStore();
            store.user = { username: 'carlos', roles: [] };
            expect(store.hasWritePermission).toBe(false);
        });
        
        it('should return false if user is null', () => {
            const store = useAuthStore();
            store.user = null;
            expect(store.hasWritePermission).toBe(false);
        });
    });
});
