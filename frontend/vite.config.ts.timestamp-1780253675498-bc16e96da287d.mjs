// vite.config.ts
import { defineConfig } from "file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/frontend/node_modules/vitest/dist/config.js";
import vue from "file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/frontend/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import { fileURLToPath, URL } from "node:url";
var __vite_injected_original_import_meta_url = "file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/frontend/vite.config.ts";
var vite_config_default = defineConfig({
  // FIX: sockjs-client es CommonJS y usa `global` (Node.js). Vite no lo shima
  // por defecto en ESM browser bundles → ReferenceError: global is not defined.
  define: {
    global: "globalThis"
  },
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", __vite_injected_original_import_meta_url)),
      "frappe-gantt/dist/frappe-gantt.css": fileURLToPath(new URL("./src/tests/dummy.css", __vite_injected_original_import_meta_url))
    }
  },
  server: {
    port: 5173,
    open: false,
    proxy: {
      "/api": {
        target: "http://127.0.0.1:8080",
        // Apuntando al APIM o Backend Spring Boot Local
        changeOrigin: true,
        secure: false,
        timeout: 12e4
        // ADR-014: Timeout amplio para arranque en frío de Spring Boot
      },
      // FIX: Proxificar el handshake HTTP de SockJS (/ws/workdesk/info, /ws/workdesk/***)
      // Sin este proxy, el navegador hace CORS al puerto 8080 directamente
      "/ws": {
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
        secure: false,
        ws: true
        // Habilitar upgrade WebSocket nativo en el proxy de Vite
      }
    }
  },
  optimizeDeps: {
    include: ["vue", "vue-router", "pinia", "axios"]
  },
  test: {
    globals: true,
    environment: "jsdom",
    css: true,
    setupFiles: ["./src/tests/setupVitest.ts"],
    include: ["src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
    exclude: ["**/node_modules/**", "**/dist/**", "**/*.e2e.spec.ts", "src/tests/ct/**"],
    server: {
      deps: {
        inline: ["element-plus", "vue-router"]
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJDOlxcXFxVc2Vyc1xcXFxIYXJvbHRBbmRyXHUwMEU5c0dcdTAwRjNtZXpBZ3VcXFxcUHJveWVjdG9BbnRpZ3Jhdml0eVxcXFxpYnBtcy1wbGF0Zm9ybVxcXFxmcm9udGVuZFwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiQzpcXFxcVXNlcnNcXFxcSGFyb2x0QW5kclx1MDBFOXNHXHUwMEYzbWV6QWd1XFxcXFByb3llY3RvQW50aWdyYXZpdHlcXFxcaWJwbXMtcGxhdGZvcm1cXFxcZnJvbnRlbmRcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0M6L1VzZXJzL0hhcm9sdEFuZHIlQzMlQTlzRyVDMyVCM21lekFndS9Qcm95ZWN0b0FudGlncmF2aXR5L2licG1zLXBsYXRmb3JtL2Zyb250ZW5kL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZXN0L2NvbmZpZyc7XHJcbmltcG9ydCB2dWUgZnJvbSAnQHZpdGVqcy9wbHVnaW4tdnVlJztcclxuaW1wb3J0IHsgZmlsZVVSTFRvUGF0aCwgVVJMIH0gZnJvbSAnbm9kZTp1cmwnO1xyXG5cclxuLy8gaHR0cHM6Ly92aXRlanMuZGV2L2NvbmZpZy9cclxuZXhwb3J0IGRlZmF1bHQgZGVmaW5lQ29uZmlnKHtcclxuICAgIC8vIEZJWDogc29ja2pzLWNsaWVudCBlcyBDb21tb25KUyB5IHVzYSBgZ2xvYmFsYCAoTm9kZS5qcykuIFZpdGUgbm8gbG8gc2hpbWFcclxuICAgIC8vIHBvciBkZWZlY3RvIGVuIEVTTSBicm93c2VyIGJ1bmRsZXMgXHUyMTkyIFJlZmVyZW5jZUVycm9yOiBnbG9iYWwgaXMgbm90IGRlZmluZWQuXHJcbiAgICBkZWZpbmU6IHtcclxuICAgICAgICBnbG9iYWw6ICdnbG9iYWxUaGlzJyxcclxuICAgIH0sXHJcbiAgICBwbHVnaW5zOiBbdnVlKCkgYXMgYW55XSxcclxuICAgIHJlc29sdmU6IHtcclxuICAgICAgICBhbGlhczoge1xyXG4gICAgICAgICAgICAnQCc6IGZpbGVVUkxUb1BhdGgobmV3IFVSTCgnLi9zcmMnLCBpbXBvcnQubWV0YS51cmwpKSxcclxuICAgICAgICAgICAgJ2ZyYXBwZS1nYW50dC9kaXN0L2ZyYXBwZS1nYW50dC5jc3MnOiBmaWxlVVJMVG9QYXRoKG5ldyBVUkwoJy4vc3JjL3Rlc3RzL2R1bW15LmNzcycsIGltcG9ydC5tZXRhLnVybCkpLFxyXG4gICAgICAgIH0sXHJcbiAgICB9LFxyXG4gICAgc2VydmVyOiB7XHJcbiAgICAgICAgcG9ydDogNTE3MyxcclxuICAgICAgICBvcGVuOiBmYWxzZSxcclxuICAgICAgICBwcm94eToge1xyXG4gICAgICAgICAgICAnL2FwaSc6IHtcclxuICAgICAgICAgICAgICAgIHRhcmdldDogJ2h0dHA6Ly8xMjcuMC4wLjE6ODA4MCcsIC8vIEFwdW50YW5kbyBhbCBBUElNIG8gQmFja2VuZCBTcHJpbmcgQm9vdCBMb2NhbFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlT3JpZ2luOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgc2VjdXJlOiBmYWxzZSxcclxuICAgICAgICAgICAgICAgIHRpbWVvdXQ6IDEyMDAwMCwgLy8gQURSLTAxNDogVGltZW91dCBhbXBsaW8gcGFyYSBhcnJhbnF1ZSBlbiBmclx1MDBFRG8gZGUgU3ByaW5nIEJvb3RcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgLy8gRklYOiBQcm94aWZpY2FyIGVsIGhhbmRzaGFrZSBIVFRQIGRlIFNvY2tKUyAoL3dzL3dvcmtkZXNrL2luZm8sIC93cy93b3JrZGVzay8qKiopXHJcbiAgICAgICAgICAgIC8vIFNpbiBlc3RlIHByb3h5LCBlbCBuYXZlZ2Fkb3IgaGFjZSBDT1JTIGFsIHB1ZXJ0byA4MDgwIGRpcmVjdGFtZW50ZVxyXG4gICAgICAgICAgICAnL3dzJzoge1xyXG4gICAgICAgICAgICAgICAgdGFyZ2V0OiAnaHR0cDovLzEyNy4wLjAuMTo4MDgwJyxcclxuICAgICAgICAgICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHNlY3VyZTogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICB3czogdHJ1ZSwgLy8gSGFiaWxpdGFyIHVwZ3JhZGUgV2ViU29ja2V0IG5hdGl2byBlbiBlbCBwcm94eSBkZSBWaXRlXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgfSxcclxuICAgIH0sXHJcbiAgICBvcHRpbWl6ZURlcHM6IHtcclxuICAgICAgICBpbmNsdWRlOiBbJ3Z1ZScsICd2dWUtcm91dGVyJywgJ3BpbmlhJywgJ2F4aW9zJ10sXHJcbiAgICB9LFxyXG4gICAgdGVzdDoge1xyXG4gICAgICAgIGdsb2JhbHM6IHRydWUsXHJcbiAgICAgICAgZW52aXJvbm1lbnQ6ICdqc2RvbScsXHJcbiAgICAgICAgY3NzOiB0cnVlLFxyXG4gICAgICAgIHNldHVwRmlsZXM6IFsnLi9zcmMvdGVzdHMvc2V0dXBWaXRlc3QudHMnXSxcclxuICAgICAgICBpbmNsdWRlOiBbJ3NyYy8qKi8qLnt0ZXN0LHNwZWN9LntqcyxtanMsY2pzLHRzLG10cyxjdHMsanN4LHRzeH0nXSxcclxuICAgICAgICBleGNsdWRlOiBbJyoqL25vZGVfbW9kdWxlcy8qKicsICcqKi9kaXN0LyoqJywgJyoqLyouZTJlLnNwZWMudHMnLCAnc3JjL3Rlc3RzL2N0LyoqJ10sXHJcbiAgICAgICAgc2VydmVyOiB7XHJcbiAgICAgICAgICAgIGRlcHM6IHtcclxuICAgICAgICAgICAgICAgIGlubGluZTogWydlbGVtZW50LXBsdXMnLCAndnVlLXJvdXRlciddXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcbn0pO1xyXG5cclxuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUFtYSxTQUFTLG9CQUFvQjtBQUNoYyxPQUFPLFNBQVM7QUFDaEIsU0FBUyxlQUFlLFdBQVc7QUFGa08sSUFBTSwyQ0FBMkM7QUFLdFQsSUFBTyxzQkFBUSxhQUFhO0FBQUE7QUFBQTtBQUFBLEVBR3hCLFFBQVE7QUFBQSxJQUNKLFFBQVE7QUFBQSxFQUNaO0FBQUEsRUFDQSxTQUFTLENBQUMsSUFBSSxDQUFRO0FBQUEsRUFDdEIsU0FBUztBQUFBLElBQ0wsT0FBTztBQUFBLE1BQ0gsS0FBSyxjQUFjLElBQUksSUFBSSxTQUFTLHdDQUFlLENBQUM7QUFBQSxNQUNwRCxzQ0FBc0MsY0FBYyxJQUFJLElBQUkseUJBQXlCLHdDQUFlLENBQUM7QUFBQSxJQUN6RztBQUFBLEVBQ0o7QUFBQSxFQUNBLFFBQVE7QUFBQSxJQUNKLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE9BQU87QUFBQSxNQUNILFFBQVE7QUFBQSxRQUNKLFFBQVE7QUFBQTtBQUFBLFFBQ1IsY0FBYztBQUFBLFFBQ2QsUUFBUTtBQUFBLFFBQ1IsU0FBUztBQUFBO0FBQUEsTUFDYjtBQUFBO0FBQUE7QUFBQSxNQUdBLE9BQU87QUFBQSxRQUNILFFBQVE7QUFBQSxRQUNSLGNBQWM7QUFBQSxRQUNkLFFBQVE7QUFBQSxRQUNSLElBQUk7QUFBQTtBQUFBLE1BQ1I7QUFBQSxJQUNKO0FBQUEsRUFDSjtBQUFBLEVBQ0EsY0FBYztBQUFBLElBQ1YsU0FBUyxDQUFDLE9BQU8sY0FBYyxTQUFTLE9BQU87QUFBQSxFQUNuRDtBQUFBLEVBQ0EsTUFBTTtBQUFBLElBQ0YsU0FBUztBQUFBLElBQ1QsYUFBYTtBQUFBLElBQ2IsS0FBSztBQUFBLElBQ0wsWUFBWSxDQUFDLDRCQUE0QjtBQUFBLElBQ3pDLFNBQVMsQ0FBQyxzREFBc0Q7QUFBQSxJQUNoRSxTQUFTLENBQUMsc0JBQXNCLGNBQWMsb0JBQW9CLGlCQUFpQjtBQUFBLElBQ25GLFFBQVE7QUFBQSxNQUNKLE1BQU07QUFBQSxRQUNGLFFBQVEsQ0FBQyxnQkFBZ0IsWUFBWTtBQUFBLE1BQ3pDO0FBQUEsSUFDSjtBQUFBLEVBQ0o7QUFDSixDQUFDOyIsCiAgIm5hbWVzIjogW10KfQo=
