import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
    // FIX: sockjs-client es CommonJS y usa `global` (Node.js). Vite no lo shima
    // por defecto en ESM browser bundles → ReferenceError: global is not defined.
    define: {
        global: 'globalThis',
    },
    plugins: [vue() as any],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
            'frappe-gantt/dist/frappe-gantt.css': path.resolve(__dirname, './src/tests/dummy.css'),
        },
    },
    server: {
        port: 5173,
        open: false,
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8080', // Apuntando al APIM o Backend Spring Boot Local
                changeOrigin: true,
                secure: false,
                timeout: 120000, // ADR-014: Timeout amplio para arranque en frío de Spring Boot
            },
            // FIX: Proxificar el handshake HTTP de SockJS (/ws/workdesk/info, /ws/workdesk/***)
            // Sin este proxy, el navegador hace CORS al puerto 8080 directamente
            '/ws': {
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,
                secure: false,
                ws: true, // Habilitar upgrade WebSocket nativo en el proxy de Vite
            },
        },
    },
    optimizeDeps: {
        include: ['vue', 'vue-router', 'pinia', 'axios'],
    },
    test: {
        globals: true,
        environment: 'jsdom',
        css: true,
        setupFiles: ['./src/tests/setupVitest.ts'],
        include: ['src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
        exclude: ['**/node_modules/**', '**/dist/**', '**/*.e2e.spec.ts', 'src/tests/ct/**'],
        server: {
            deps: {
                inline: ['element-plus', 'vue-router']
            }
        }
    }
});

