import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue() as any],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
            'frappe-gantt/dist/frappe-gantt.css': path.resolve(__dirname, './src/tests/dummy.css'),
        },
    },
    server: {
        port: 3000,
        open: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8080', // Apuntando al APIM o Backend Spring Boot Local
                changeOrigin: true,
                secure: false,
            },
        },
    },
    test: {
        globals: true,
        environment: 'jsdom',
        css: true,
        setupFiles: ['./src/tests/setupVitest.ts'],
        include: ['src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
        server: {
            deps: {
                inline: ['element-plus', 'vue-router']
            }
        }
    }
});
