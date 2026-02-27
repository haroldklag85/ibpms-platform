import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import federation from '@originjs/vite-plugin-federation';

// https://vitejs.dev/config/
export default defineConfig({
    base: '/admin-remote/',
    plugins: [
        vue(),
        federation({
            name: 'admin_remote',
            filename: 'remoteEntry.js',
            exposes: {
                './ProcessDesignerPage': './src/views/ProcessDesignerPage.vue'
            },
            shared: ['vue', 'vue-router', 'pinia']
        })
    ],
    server: {
        port: 3002,
        host: '0.0.0.0', // Allows docker exposure
    },
    build: {
        target: 'esnext',
        minify: false,
        cssCodeSplit: false
    }
});
