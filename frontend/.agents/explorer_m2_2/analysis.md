# Analysis — Frontend Test Suite Issues (Milestone 2)

This report details the investigation of the frontend test suite failures for Milestone 2 in `ibpms-platform/frontend` and provides the exact code modifications required to make the tests pass.

---

## Executive Summary
Four test specifications (`RoleSelectorDropdown.spec.ts`, `WorkdeskTabs.spec.ts`, `axiosInterceptor.spec.ts`, and `PmoSettings.spec.ts`) fail due to configuration mismatches, missing mocks, incorrect Pinia state initialization, and an underlying ReferenceError in the `PmoSettings.vue` component. Applying target-specific mocks, correcting store ID keys, and defining the Pinia store client resolves all failures.

---

## Summary of Findings

| File | Primary Issue | Impact | Root Cause & Resolution |
|---|---|---|---|
| **RoleSelectorDropdown.spec.ts** | Missing mocks for `vue-router` and `apiClient`. | Test crashes; fails assertion. | Component calls `useRouter()` and `apiClient.post()`. Resolves by adding local `vi.mock` at the top. |
| **WorkdeskTabs.spec.ts** | Incorrect key (`workdeskStore`) in `initialState`. | Test fails class inclusion assertion. | The store's registered ID is `workdesk`, not `workdeskStore`. Resolves by correcting the key in `initialState`. |
| **axiosInterceptor.spec.ts** | axios mock does not track interceptor registration. | Test crashes with `TypeError: Cannot read properties of undefined (reading '0')`. | `apiClient.interceptors.response.handlers` is undefined because `response.use` returns nothing. Resolves by enriching the mock to push handlers into arrays. |
| **PmoSettings.spec.ts** / **PmoSettings.vue** | Pinia not initialized in tests; `integrationStore` is undefined in the view. | Component fails to mount; `ReferenceError` occurs. | 1) The spec lacks Pinia initialization in `beforeEach` and `mount`. <br>2) `PmoSettings.vue` imports `useIntegrationStore` but never instantiates `integrationStore`. Resolves by updating both. |

---

## Evidence Chain and Proposed Fixes

### 1. RoleSelectorDropdown.spec.ts
- **Observation:** Running `npx vitest run src/tests/components/shell/RoleSelectorDropdown.spec.ts` fails with:
  - `Modo Desconectado. La aplicación se ha congelado por falta de Red.` (thrown by the real `apiClient.ts` interceptor on Network Error).
  - `TypeError: Cannot read properties of undefined (reading 'currentRoute')` (thrown when accessing `router.currentRoute.value` since `useRouter()` returns `undefined`).
  - `AssertionError: expected "spy" to be called with arguments: [ 'ROLE_OPERADOR' ]`.
- **Reasoning:** The test does not stub network requests and routing behavior. The component relies on `apiClient` telemetry post calls and `vue-router` push/meta queries.
- **Proposed Code Changes:**
  Add the following mocks at the top of `src/tests/components/shell/RoleSelectorDropdown.spec.ts` (directly after imports):

```typescript
// Proposed mocks in RoleSelectorDropdown.spec.ts
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    currentRoute: {
      value: {
        meta: {}
      }
    }
  })
}));

vi.mock('@/services/apiClient', () => ({
  default: {
    post: vi.fn().mockResolvedValue({})
  }
}));
```

---

### 2. WorkdeskTabs.spec.ts
- **Observation:** Running `npx vitest run src/tests/components/workdesk/WorkdeskTabs.spec.ts` fails with:
  - `AssertionError: expected [ 'py-3', 'px-6', …(14) ] to include 'border-blue-600'`.
- **Reasoning:** Pinia store state was not initialized correctly because the key used was `workdeskStore`. The store in `src/stores/useWorkdeskStore.ts` is registered as `'workdesk'`. Hence, the component defaulted to `'POOL'` (from the store's default state) and the tab `'Mi Bandeja'` (active when `'PERSONAL'`) did not receive the `'border-blue-600'` class.
- **Proposed Code Changes:**
  In `src/tests/components/workdesk/WorkdeskTabs.spec.ts`, update `initialState` inside `beforeEach` to map to `workdesk` instead of `workdeskStore`:

```typescript
// Before
    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                workdeskStore: { activeView: 'PERSONAL' }
            }
        });
    });

// After
    beforeEach(() => {
        pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                workdesk: { activeView: 'PERSONAL' }
            }
        });
    });
```

---

### 3. axiosInterceptor.spec.ts
- **Observation:** Running `npx vitest run src/tests/services/axiosInterceptor.spec.ts` fails with:
  - `TypeError: Cannot read properties of undefined (reading '0')` when evaluating `(apiClient.interceptors.response as any).handlers[0].rejected`.
- **Reasoning:** The local mock of `axios` stubs `response.use` as a basic `vi.fn()` mock that does nothing. Consequently, `handlers` array is empty or undefined, which crashes the test.
- **Proposed Code Changes:**
  Enrich the `vi.mock('axios')` block in `src/tests/services/axiosInterceptor.spec.ts` to accumulate the callbacks:

```typescript
// Before
vi.mock('axios', async (importOriginal) => {
    const actual = await importOriginal<typeof import('axios')>();
    const mockAxiosInstance = {
        interceptors: {
            request: { use: vi.fn() },
            response: { use: vi.fn() }
        },
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn()
    };
    return {
        ...actual,
        default: {
            create: vi.fn(() => mockAxiosInstance)
        }
    };
});

// After
vi.mock('axios', async (importOriginal) => {
    const actual = await importOriginal<typeof import('axios')>();
    const requestHandlers: any[] = [];
    const responseHandlers: any[] = [];
    
    const mockAxiosInstance = {
        interceptors: {
            request: {
                use: vi.fn((fulfilled, rejected) => {
                    requestHandlers.push({ fulfilled, rejected });
                }),
                handlers: requestHandlers
            },
            response: {
                use: vi.fn((fulfilled, rejected) => {
                    responseHandlers.push({ fulfilled, rejected });
                }),
                handlers: responseHandlers
            }
        },
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn()
    };
    return {
        ...actual,
        default: {
            create: vi.fn(() => mockAxiosInstance)
        }
    };
});
```

---

### 4. PmoSettings.spec.ts and PmoSettings.vue
- **Observation:** Running `npx vitest run src/tests/views/PmoSettings.spec.ts` fails all three tests with:
  - `ReferenceError: integrationStore is not defined` inside `PmoSettings.vue`.
  - Component fails to mount/update, meaning calls to spied functions on `apiClient` are never reached.
- **Reasoning:**
  1. The component `PmoSettings.vue` imports `useIntegrationStore` but fails to instantiate it with `const integrationStore = useIntegrationStore();`.
  2. The test file `PmoSettings.spec.ts` does not initialize Pinia (`setActivePinia`) or pass it as a global plugin to the `mount` method, causing it to crash when components use Pinia stores.
- **Proposed Code Changes:**

**In `src/views/admin/PMO/PmoSettings.vue`:**
Declare `integrationStore` in the setup script:

```typescript
// Before (Line 172-175)
<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, onMounted } from 'vue';

// After
<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, onMounted } from 'vue';

const integrationStore = useIntegrationStore();
```

**In `src/tests/views/PmoSettings.spec.ts`:**
1. Import `createPinia` and `setActivePinia` from `'pinia'`.
2. Call `setActivePinia(createPinia())` in `beforeEach`.
3. Pass `createPinia()` inside `global.plugins` to all `mount` calls.

```typescript
// Imports & Hook updates
import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia'; // Added
import PmoSettings from '@/views/admin/PMO/PmoSettings.vue';
import apiClient from '@/services/apiClient';
import { nextTick } from 'vue';

describe('PmoSettings.vue', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    setActivePinia(createPinia()); // Added
  });
...

// Inside each of the three "it" test cases:
// Before:
const wrapper = mount(PmoSettings);

// After:
const wrapper = mount(PmoSettings, {
  global: {
    plugins: [createPinia()]
  }
});
```

---

## Conclusion
By registering stubs for `vue-router`/`apiClient` in `RoleSelectorDropdown.spec.ts`, correcting the key mapping in `WorkdeskTabs.spec.ts`, tracking interceptors in `axiosInterceptor.spec.ts`, and initializing/defining Pinia store references in both `PmoSettings.spec.ts` and `PmoSettings.vue`, the entire frontend test suite for Milestone 2 will run successfully without altering validation assertions.
