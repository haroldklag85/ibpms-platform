import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import federation from '@originjs/vite-plugin-federation';

// https://vitejs.dev/config/
export default defineConfig({
    base: '/inbox-remote/',
    plugins: [
        vue(),
        federation({
            name: 'inbox_remote',
            filename: 'remoteEntry.js',
            exposes: {
                './InboxPage': './src/views/InboxPage.vue',
                './DashboardPage': './src/views/DashboardPage.vue',
                './TaskDetailPage': './src/views/TaskDetailPage.vue'
            },
            shared: ['vue', 'vue-router', 'pinia']
        })
    ],
    server: {
        port: 3001,
        host: '0.0.0.0', // Allows docker exposure
    },
    build: {
        target: 'esnext',
        minify: false,
        cssCodeSplit: false
    }
});
