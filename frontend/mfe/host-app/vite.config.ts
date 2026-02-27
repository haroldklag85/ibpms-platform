import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import federation from '@originjs/vite-plugin-federation';

// https://vitejs.dev/config/
export default defineConfig(({ command }) => {
    const isProd = command === 'build';
    return {
        plugins: [
            vue(),
            federation({
                name: 'host_app',
                remotes: {
                    inbox_remote: isProd ? '/inbox-remote/assets/remoteEntry.js' : 'http://localhost:3001/assets/remoteEntry.js',
                    admin_remote: isProd ? '/admin-remote/assets/remoteEntry.js' : 'http://localhost:3002/assets/remoteEntry.js'
                },
                shared: ['vue', 'vue-router', 'pinia']
            })
        ],
        server: {
            port: 3000,
            host: '0.0.0.0', // Allows docker exposure
            proxy: {
                '/api': {
                    target: 'http://localhost:8080', // Default backend local fallback
                    changeOrigin: true
                }
            }
        },
        build: {
            target: 'esnext' // Requisito de Module Federation
        }
    };
});
server: {
    port: 3000,
        host: '0.0.0.0', // Allows docker exposure
            proxy: {
        '/api': {
            target: 'http://localhost:8080', // Default backend local fallback
                changeOrigin: true
        }
    }
},
build: {
    target: 'esnext' // Requisito de Module Federation
}
});
