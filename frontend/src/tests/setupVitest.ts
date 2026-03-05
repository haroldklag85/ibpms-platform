import { vi } from 'vitest';

// Interceptar imports fallidos de UI que rompen JSDOM antes que el Vite Bundler llame a los spec.ts
vi.mock('frappe-gantt/dist/frappe-gantt.css', () => ({}));
