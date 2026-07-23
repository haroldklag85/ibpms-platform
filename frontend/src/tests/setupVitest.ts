import { vi } from 'vitest';
import { config } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import i18n from '@/i18n';

// Inicialización Global de Pinia para los Tests del Sprint 5 (Evitar caídas de montaje)
config.global.plugins = [
    i18n,
    createTestingPinia({
        createSpy: vi.fn,
        stubActions: false, // Permitir que las acciones originen cambios de estado
        initialState: {
            auth: {
                token: 'TEST-TOKEN-QA',
                user: { username: 'qa_worker', roles: ['ROLE_APPROVER'] },
                activeRole: 'ROLE_APPROVER',
                isHydrating: false
            }
        }
    })
];

config.global.stubs = {
    RecycleScroller: { 
        template: '<div><slot v-for="item in items" :key="item.id || Math.random()" :item="item"></slot></div>',
        props: ['items'] 
    }
};

// Interceptar imports fallidos de UI que rompen JSDOM antes que el Vite Bundler llame a los spec.ts
vi.mock('frappe-gantt/dist/frappe-gantt.css', () => ({}));

vi.mock('@guolao/vue-monaco-editor', async (importOriginal) => {
    const actual = await importOriginal<any>();
    return {
        ...actual,
        loader: {
            ...actual.loader,
            config: vi.fn(),
            init: vi.fn()
        }
    };
});

class MockEventSource {
    url: string;
    constructor(url: string) { this.url = url; }
    close() {}
    addEventListener() {}
    removeEventListener() {}
}
vi.stubGlobal('EventSource', MockEventSource);

vi.stubGlobal('fetch', vi.fn(() => Promise.resolve({
    ok: true,
    json: () => Promise.resolve({}),
    text: () => Promise.resolve('')
})));
