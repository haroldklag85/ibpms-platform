import { vi } from 'vitest';

// Interceptar imports fallidos de UI que rompen JSDOM antes que el Vite Bundler llame a los spec.ts
vi.mock('frappe-gantt/dist/frappe-gantt.css', () => ({}));

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
