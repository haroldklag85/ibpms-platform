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
    port: 3e3,
    open: true,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
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
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJDOlxcXFxVc2Vyc1xcXFxIYXJvbHRBbmRyXHUwMEU5c0dcdTAwRjNtZXpBZ3VcXFxcUHJveWVjdG9BbnRpZ3Jhdml0eVxcXFxpYnBtcy1wbGF0Zm9ybVxcXFxmcm9udGVuZFwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiQzpcXFxcVXNlcnNcXFxcSGFyb2x0QW5kclx1MDBFOXNHXHUwMEYzbWV6QWd1XFxcXFByb3llY3RvQW50aWdyYXZpdHlcXFxcaWJwbXMtcGxhdGZvcm1cXFxcZnJvbnRlbmRcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0M6L1VzZXJzL0hhcm9sdEFuZHIlQzMlQTlzRyVDMyVCM21lekFndS9Qcm95ZWN0b0FudGlncmF2aXR5L2licG1zLXBsYXRmb3JtL2Zyb250ZW5kL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZXN0L2NvbmZpZyc7XHJcbmltcG9ydCB2dWUgZnJvbSAnQHZpdGVqcy9wbHVnaW4tdnVlJztcclxuaW1wb3J0IHBhdGggZnJvbSAncGF0aCc7XHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gICAgcGx1Z2luczogW3Z1ZSgpIGFzIGFueV0sXHJcbiAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgYWxpYXM6IHtcclxuICAgICAgICAgICAgJ0AnOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAnLi9zcmMnKSxcclxuICAgICAgICAgICAgJ2ZyYXBwZS1nYW50dC9kaXN0L2ZyYXBwZS1nYW50dC5jc3MnOiBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAnLi9zcmMvdGVzdHMvZHVtbXkuY3NzJyksXHJcbiAgICAgICAgfSxcclxuICAgIH0sXHJcbiAgICBzZXJ2ZXI6IHtcclxuICAgICAgICBwb3J0OiAzMDAwLFxyXG4gICAgICAgIG9wZW46IHRydWUsXHJcbiAgICAgICAgcHJveHk6IHtcclxuICAgICAgICAgICAgJy9hcGknOiB7XHJcbiAgICAgICAgICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODAnLCAvLyBBcHVudGFuZG8gYWwgQVBJTSBvIEJhY2tlbmQgU3ByaW5nIEJvb3QgTG9jYWxcclxuICAgICAgICAgICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHNlY3VyZTogZmFsc2UsXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgfSxcclxuICAgIH0sXHJcbiAgICB0ZXN0OiB7XHJcbiAgICAgICAgZ2xvYmFsczogdHJ1ZSxcclxuICAgICAgICBlbnZpcm9ubWVudDogJ2pzZG9tJyxcclxuICAgICAgICBjc3M6IHRydWUsXHJcbiAgICAgICAgc2V0dXBGaWxlczogWycuL3NyYy90ZXN0cy9zZXR1cFZpdGVzdC50cyddLFxyXG4gICAgICAgIGluY2x1ZGU6IFsnc3JjLyoqLyoue3Rlc3Qsc3BlY30ue2pzLG1qcyxjanMsdHMsbXRzLGN0cyxqc3gsdHN4fSddLFxyXG4gICAgICAgIHNlcnZlcjoge1xyXG4gICAgICAgICAgICBkZXBzOiB7XHJcbiAgICAgICAgICAgICAgICBpbmxpbmU6IFsnZWxlbWVudC1wbHVzJywgJ3Z1ZS1yb3V0ZXInXVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59KTtcclxuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUFtYSxTQUFTLG9CQUFvQjtBQUNoYyxPQUFPLFNBQVM7QUFDaEIsT0FBTyxVQUFVO0FBRmpCLElBQU0sbUNBQW1DO0FBS3pDLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQ3hCLFNBQVMsQ0FBQyxJQUFJLENBQVE7QUFBQSxFQUN0QixTQUFTO0FBQUEsSUFDTCxPQUFPO0FBQUEsTUFDSCxLQUFLLEtBQUssUUFBUSxrQ0FBVyxPQUFPO0FBQUEsTUFDcEMsc0NBQXNDLEtBQUssUUFBUSxrQ0FBVyx1QkFBdUI7QUFBQSxJQUN6RjtBQUFBLEVBQ0o7QUFBQSxFQUNBLFFBQVE7QUFBQSxJQUNKLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE9BQU87QUFBQSxNQUNILFFBQVE7QUFBQSxRQUNKLFFBQVE7QUFBQTtBQUFBLFFBQ1IsY0FBYztBQUFBLFFBQ2QsUUFBUTtBQUFBLE1BQ1o7QUFBQSxJQUNKO0FBQUEsRUFDSjtBQUFBLEVBQ0EsTUFBTTtBQUFBLElBQ0YsU0FBUztBQUFBLElBQ1QsYUFBYTtBQUFBLElBQ2IsS0FBSztBQUFBLElBQ0wsWUFBWSxDQUFDLDRCQUE0QjtBQUFBLElBQ3pDLFNBQVMsQ0FBQyxzREFBc0Q7QUFBQSxJQUNoRSxRQUFRO0FBQUEsTUFDSixNQUFNO0FBQUEsUUFDRixRQUFRLENBQUMsZ0JBQWdCLFlBQVk7QUFBQSxNQUN6QztBQUFBLElBQ0o7QUFBQSxFQUNKO0FBQ0osQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
