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
        secure: false
      }
    }
  },
  test: {
    globals: true,
    environment: "jsdom",
    css: true,
    setupFiles: ["./src/tests/setupVitest.ts"],
    include: ["src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
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
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJDOlxcXFxVc2Vyc1xcXFxIYXJvbHRBbmRyXHUwMEU5c0dcdTAwRjNtZXpBZ3VcXFxcUHJveWVjdG9BbnRpZ3Jhdml0eVxcXFxpYnBtcy1wbGF0Zm9ybVxcXFxmcm9udGVuZFwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiQzpcXFxcVXNlcnNcXFxcSGFyb2x0QW5kclx1MDBFOXNHXHUwMEYzbWV6QWd1XFxcXFByb3llY3RvQW50aWdyYXZpdHlcXFxcaWJwbXMtcGxhdGZvcm1cXFxcZnJvbnRlbmRcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0M6L1VzZXJzL0hhcm9sdEFuZHIlQzMlQTlzRyVDMyVCM21lekFndS9Qcm95ZWN0b0FudGlncmF2aXR5L2licG1zLXBsYXRmb3JtL2Zyb250ZW5kL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZXN0L2NvbmZpZyc7XHJcbmltcG9ydCB2dWUgZnJvbSAnQHZpdGVqcy9wbHVnaW4tdnVlJztcclxuaW1wb3J0IHBhdGggZnJvbSAncGF0aCc7XHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gICAgcGx1Z2luczogW3Z1ZSgpIGFzIGFueV0sXHJcbiAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgYWxpYXM6IHtcclxuICAgICAgICAgICAgJ0AnOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAnLi9zcmMnKSxcclxuICAgICAgICAgICAgJ2ZyYXBwZS1nYW50dC9kaXN0L2ZyYXBwZS1nYW50dC5jc3MnOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAnLi9zcmMvdGVzdHMvZHVtbXkuY3NzJyksXHJcbiAgICAgICAgfSxcclxuICAgIH0sXHJcbiAgICBzZXJ2ZXI6IHtcclxuICAgICAgICBwb3J0OiA1MTczLFxyXG4gICAgICAgIG9wZW46IGZhbHNlLFxyXG4gICAgICAgIHByb3h5OiB7XHJcbiAgICAgICAgICAgICcvYXBpJzoge1xyXG4gICAgICAgICAgICAgICAgdGFyZ2V0OiAnaHR0cDovLzEyNy4wLjAuMTo4MDgwJywgLy8gQXB1bnRhbmRvIGFsIEFQSU0gbyBCYWNrZW5kIFNwcmluZyBCb290IExvY2FsXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXHJcbiAgICAgICAgICAgICAgICBzZWN1cmU6IGZhbHNlLFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgIH0sXHJcbiAgICB9LFxyXG4gICAgdGVzdDoge1xyXG4gICAgICAgIGdsb2JhbHM6IHRydWUsXHJcbiAgICAgICAgZW52aXJvbm1lbnQ6ICdqc2RvbScsXHJcbiAgICAgICAgY3NzOiB0cnVlLFxyXG4gICAgICAgIHNldHVwRmlsZXM6IFsnLi9zcmMvdGVzdHMvc2V0dXBWaXRlc3QudHMnXSxcclxuICAgICAgICBpbmNsdWRlOiBbJ3NyYy8qKi8qLnt0ZXN0LHNwZWN9LntqcyxtanMsY2pzLHRzLG10cyxjdHMsanN4LHRzeH0nXSxcclxuICAgICAgICBzZXJ2ZXI6IHtcclxuICAgICAgICAgICAgZGVwczoge1xyXG4gICAgICAgICAgICAgICAgaW5saW5lOiBbJ2VsZW1lbnQtcGx1cycsICd2dWUtcm91dGVyJ11cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxufSk7XHJcbiJdLAogICJtYXBwaW5ncyI6ICI7QUFBbWEsU0FBUyxvQkFBb0I7QUFDaGMsT0FBTyxTQUFTO0FBQ2hCLE9BQU8sVUFBVTtBQUZqQixJQUFNLG1DQUFtQztBQUt6QyxJQUFPLHNCQUFRLGFBQWE7QUFBQSxFQUN4QixTQUFTLENBQUMsSUFBSSxDQUFRO0FBQUEsRUFDdEIsU0FBUztBQUFBLElBQ0wsT0FBTztBQUFBLE1BQ0gsS0FBSyxLQUFLLFFBQVEsa0NBQVcsT0FBTztBQUFBLE1BQ3BDLHNDQUFzQyxLQUFLLFFBQVEsa0NBQVcsdUJBQXVCO0FBQUEsSUFDekY7QUFBQSxFQUNKO0FBQUEsRUFDQSxRQUFRO0FBQUEsSUFDSixNQUFNO0FBQUEsSUFDTixNQUFNO0FBQUEsSUFDTixPQUFPO0FBQUEsTUFDSCxRQUFRO0FBQUEsUUFDSixRQUFRO0FBQUE7QUFBQSxRQUNSLGNBQWM7QUFBQSxRQUNkLFFBQVE7QUFBQSxNQUNaO0FBQUEsSUFDSjtBQUFBLEVBQ0o7QUFBQSxFQUNBLE1BQU07QUFBQSxJQUNGLFNBQVM7QUFBQSxJQUNULGFBQWE7QUFBQSxJQUNiLEtBQUs7QUFBQSxJQUNMLFlBQVksQ0FBQyw0QkFBNEI7QUFBQSxJQUN6QyxTQUFTLENBQUMsc0RBQXNEO0FBQUEsSUFDaEUsUUFBUTtBQUFBLE1BQ0osTUFBTTtBQUFBLFFBQ0YsUUFBUSxDQUFDLGdCQUFnQixZQUFZO0FBQUEsTUFDekM7QUFBQSxJQUNKO0FBQUEsRUFDSjtBQUNKLENBQUM7IiwKICAibmFtZXMiOiBbXQp9Cg==
