// vite.config.ts
import { defineConfig } from "file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/frontend/node_modules/vitest/dist/config.js";
import vue from "file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/frontend/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import path from "path";
var __vite_injected_original_dirname = "C:\\Users\\HaroltAndr\xE9sG\xF3mezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend";
var vite_config_default = defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__vite_injected_original_dirname, "./src"),
      "frappe-gantt/dist/frappe-gantt.css": path.resolve(__vite_injected_original_dirname, "./src/tests/dummy.css")
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
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJDOlxcXFxVc2Vyc1xcXFxIYXJvbHRBbmRyXHUwMEU5c0dcdTAwRjNtZXpBZ3VcXFxcUHJveWVjdG9BbnRpZ3Jhdml0eVxcXFxpYnBtcy1wbGF0Zm9ybVxcXFxmcm9udGVuZFwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiQzpcXFxcVXNlcnNcXFxcSGFyb2x0QW5kclx1MDBFOXNHXHUwMEYzbWV6QWd1XFxcXFByb3llY3RvQW50aWdyYXZpdHlcXFxcaWJwbXMtcGxhdGZvcm1cXFxcZnJvbnRlbmRcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0M6L1VzZXJzL0hhcm9sdEFuZHIlQzMlQTlzRyVDMyVCM21lekFndS9Qcm95ZWN0b0FudGlncmF2aXR5L2licG1zLXBsYXRmb3JtL2Zyb250ZW5kL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZXN0L2NvbmZpZyc7XHJcbmltcG9ydCB2dWUgZnJvbSAnQHZpdGVqcy9wbHVnaW4tdnVlJztcclxuaW1wb3J0IHBhdGggZnJvbSAncGF0aCc7XHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gICAgcGx1Z2luczogW3Z1ZSgpIGFzIGFueV0sXHJcbiAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgYWxpYXM6IHtcclxuICAgICAgICAgICAgJ0AnOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAnLi9zcmMnKSxcclxuICAgICAgICAgICAgJ2ZyYXBwZS1nYW50dC9kaXN0L2ZyYXBwZS1nYW50dC5jc3MnOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAnLi9zcmMvdGVzdHMvZHVtbXkuY3NzJyksXHJcbiAgICAgICAgfSxcclxuICAgIH0sXHJcbiAgICBzZXJ2ZXI6IHtcclxuICAgICAgICBwb3J0OiA1MTczLFxyXG4gICAgICAgIG9wZW46IGZhbHNlLFxyXG4gICAgICAgIHByb3h5OiB7XHJcbiAgICAgICAgICAgICcvYXBpJzoge1xyXG4gICAgICAgICAgICAgICAgdGFyZ2V0OiAnaHR0cDovLzEyNy4wLjAuMTo4MDgwJywgLy8gQXB1bnRhbmRvIGFsIEFQSU0gbyBCYWNrZW5kIFNwcmluZyBCb290IExvY2FsXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXHJcbiAgICAgICAgICAgICAgICBzZWN1cmU6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgdGltZW91dDogMTIwMDAwLCAvLyBBRFItMDE0OiBUaW1lb3V0IGFtcGxpbyBwYXJhIGFycmFucXVlIGVuIGZyXHUwMEVEbyBkZSBTcHJpbmcgQm9vdFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgIH0sXHJcbiAgICB9LFxyXG4gICAgb3B0aW1pemVEZXBzOiB7XHJcbiAgICAgICAgaW5jbHVkZTogWyd2dWUnLCAndnVlLXJvdXRlcicsICdwaW5pYScsICdheGlvcyddLFxyXG4gICAgfSxcclxuICAgIHRlc3Q6IHtcclxuICAgICAgICBnbG9iYWxzOiB0cnVlLFxyXG4gICAgICAgIGVudmlyb25tZW50OiAnanNkb20nLFxyXG4gICAgICAgIGNzczogdHJ1ZSxcclxuICAgICAgICBzZXR1cEZpbGVzOiBbJy4vc3JjL3Rlc3RzL3NldHVwVml0ZXN0LnRzJ10sXHJcbiAgICAgICAgaW5jbHVkZTogWydzcmMvKiovKi57dGVzdCxzcGVjfS57anMsbWpzLGNqcyx0cyxtdHMsY3RzLGpzeCx0c3h9J10sXHJcbiAgICAgICAgZXhjbHVkZTogWycqKi9ub2RlX21vZHVsZXMvKionLCAnKiovZGlzdC8qKicsICcqKi8qLmUyZS5zcGVjLnRzJywgJ3NyYy90ZXN0cy9jdC8qKiddLFxyXG4gICAgICAgIHNlcnZlcjoge1xyXG4gICAgICAgICAgICBkZXBzOiB7XHJcbiAgICAgICAgICAgICAgICBpbmxpbmU6IFsnZWxlbWVudC1wbHVzJywgJ3Z1ZS1yb3V0ZXInXVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59KTtcclxuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUFtYSxTQUFTLG9CQUFvQjtBQUNoYyxPQUFPLFNBQVM7QUFDaEIsT0FBTyxVQUFVO0FBRmpCLElBQU0sbUNBQW1DO0FBS3pDLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQ3hCLFNBQVMsQ0FBQyxJQUFJLENBQVE7QUFBQSxFQUN0QixTQUFTO0FBQUEsSUFDTCxPQUFPO0FBQUEsTUFDSCxLQUFLLEtBQUssUUFBUSxrQ0FBVyxPQUFPO0FBQUEsTUFDcEMsc0NBQXNDLEtBQUssUUFBUSxrQ0FBVyx1QkFBdUI7QUFBQSxJQUN6RjtBQUFBLEVBQ0o7QUFBQSxFQUNBLFFBQVE7QUFBQSxJQUNKLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE9BQU87QUFBQSxNQUNILFFBQVE7QUFBQSxRQUNKLFFBQVE7QUFBQTtBQUFBLFFBQ1IsY0FBYztBQUFBLFFBQ2QsUUFBUTtBQUFBLFFBQ1IsU0FBUztBQUFBO0FBQUEsTUFDYjtBQUFBLElBQ0o7QUFBQSxFQUNKO0FBQUEsRUFDQSxjQUFjO0FBQUEsSUFDVixTQUFTLENBQUMsT0FBTyxjQUFjLFNBQVMsT0FBTztBQUFBLEVBQ25EO0FBQUEsRUFDQSxNQUFNO0FBQUEsSUFDRixTQUFTO0FBQUEsSUFDVCxhQUFhO0FBQUEsSUFDYixLQUFLO0FBQUEsSUFDTCxZQUFZLENBQUMsNEJBQTRCO0FBQUEsSUFDekMsU0FBUyxDQUFDLHNEQUFzRDtBQUFBLElBQ2hFLFNBQVMsQ0FBQyxzQkFBc0IsY0FBYyxvQkFBb0IsaUJBQWlCO0FBQUEsSUFDbkYsUUFBUTtBQUFBLE1BQ0osTUFBTTtBQUFBLFFBQ0YsUUFBUSxDQUFDLGdCQUFnQixZQUFZO0FBQUEsTUFDekM7QUFBQSxJQUNKO0FBQUEsRUFDSjtBQUNKLENBQUM7IiwKICAibmFtZXMiOiBbXQp9Cg==
