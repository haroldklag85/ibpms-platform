// vite.config.ts
import { defineConfig } from "file:///C:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/node_modules/vitest/dist/config.js";
import vue from "file:///C:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import path from "path";
var __vite_injected_original_dirname = "C:\\Users\\USER\\Desktop\\Proyectos\\Harold Ibpms\\ibpms-platform\\frontend";
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
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJDOlxcXFxVc2Vyc1xcXFxVU0VSXFxcXERlc2t0b3BcXFxcUHJveWVjdG9zXFxcXEhhcm9sZCBJYnBtc1xcXFxpYnBtcy1wbGF0Zm9ybVxcXFxmcm9udGVuZFwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiQzpcXFxcVXNlcnNcXFxcVVNFUlxcXFxEZXNrdG9wXFxcXFByb3llY3Rvc1xcXFxIYXJvbGQgSWJwbXNcXFxcaWJwbXMtcGxhdGZvcm1cXFxcZnJvbnRlbmRcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0M6L1VzZXJzL1VTRVIvRGVza3RvcC9Qcm95ZWN0b3MvSGFyb2xkJTIwSWJwbXMvaWJwbXMtcGxhdGZvcm0vZnJvbnRlbmQvdml0ZS5jb25maWcudHNcIjtpbXBvcnQgeyBkZWZpbmVDb25maWcgfSBmcm9tICd2aXRlc3QvY29uZmlnJztcclxuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnO1xyXG5pbXBvcnQgcGF0aCBmcm9tICdwYXRoJztcclxuXHJcbi8vIGh0dHBzOi8vdml0ZWpzLmRldi9jb25maWcvXHJcbmV4cG9ydCBkZWZhdWx0IGRlZmluZUNvbmZpZyh7XHJcbiAgICBwbHVnaW5zOiBbdnVlKCkgYXMgYW55XSxcclxuICAgIHJlc29sdmU6IHtcclxuICAgICAgICBhbGlhczoge1xyXG4gICAgICAgICAgICAnQCc6IHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsICcuL3NyYycpLFxyXG4gICAgICAgICAgICAnZnJhcHBlLWdhbnR0L2Rpc3QvZnJhcHBlLWdhbnR0LmNzcyc6IHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsICcuL3NyYy90ZXN0cy9kdW1teS5jc3MnKSxcclxuICAgICAgICB9LFxyXG4gICAgfSxcclxuICAgIHNlcnZlcjoge1xyXG4gICAgICAgIHBvcnQ6IDUxNzMsXHJcbiAgICAgICAgb3BlbjogZmFsc2UsXHJcbiAgICAgICAgcHJveHk6IHtcclxuICAgICAgICAgICAgJy9hcGknOiB7XHJcbiAgICAgICAgICAgICAgICB0YXJnZXQ6ICdodHRwOi8vMTI3LjAuMC4xOjgwODAnLCAvLyBBcHVudGFuZG8gYWwgQVBJTSBvIEJhY2tlbmQgU3ByaW5nIEJvb3QgTG9jYWxcclxuICAgICAgICAgICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHNlY3VyZTogZmFsc2UsXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgfSxcclxuICAgIH0sXHJcbiAgICB0ZXN0OiB7XHJcbiAgICAgICAgZ2xvYmFsczogdHJ1ZSxcclxuICAgICAgICBlbnZpcm9ubWVudDogJ2pzZG9tJyxcclxuICAgICAgICBjc3M6IHRydWUsXHJcbiAgICAgICAgc2V0dXBGaWxlczogWycuL3NyYy90ZXN0cy9zZXR1cFZpdGVzdC50cyddLFxyXG4gICAgICAgIGluY2x1ZGU6IFsnc3JjLyoqLyoue3Rlc3Qsc3BlY30ue2pzLG1qcyxjanMsdHMsbXRzLGN0cyxqc3gsdHN4fSddLFxyXG4gICAgICAgIHNlcnZlcjoge1xyXG4gICAgICAgICAgICBkZXBzOiB7XHJcbiAgICAgICAgICAgICAgICBpbmxpbmU6IFsnZWxlbWVudC1wbHVzJywgJ3Z1ZS1yb3V0ZXInXVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59KTtcclxuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUFnWixTQUFTLG9CQUFvQjtBQUM3YSxPQUFPLFNBQVM7QUFDaEIsT0FBTyxVQUFVO0FBRmpCLElBQU0sbUNBQW1DO0FBS3pDLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQ3hCLFNBQVMsQ0FBQyxJQUFJLENBQVE7QUFBQSxFQUN0QixTQUFTO0FBQUEsSUFDTCxPQUFPO0FBQUEsTUFDSCxLQUFLLEtBQUssUUFBUSxrQ0FBVyxPQUFPO0FBQUEsTUFDcEMsc0NBQXNDLEtBQUssUUFBUSxrQ0FBVyx1QkFBdUI7QUFBQSxJQUN6RjtBQUFBLEVBQ0o7QUFBQSxFQUNBLFFBQVE7QUFBQSxJQUNKLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE9BQU87QUFBQSxNQUNILFFBQVE7QUFBQSxRQUNKLFFBQVE7QUFBQTtBQUFBLFFBQ1IsY0FBYztBQUFBLFFBQ2QsUUFBUTtBQUFBLE1BQ1o7QUFBQSxJQUNKO0FBQUEsRUFDSjtBQUFBLEVBQ0EsTUFBTTtBQUFBLElBQ0YsU0FBUztBQUFBLElBQ1QsYUFBYTtBQUFBLElBQ2IsS0FBSztBQUFBLElBQ0wsWUFBWSxDQUFDLDRCQUE0QjtBQUFBLElBQ3pDLFNBQVMsQ0FBQyxzREFBc0Q7QUFBQSxJQUNoRSxRQUFRO0FBQUEsTUFDSixNQUFNO0FBQUEsUUFDRixRQUFRLENBQUMsZ0JBQWdCLFlBQVk7QUFBQSxNQUN6QztBQUFBLElBQ0o7QUFBQSxFQUNKO0FBQ0osQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
