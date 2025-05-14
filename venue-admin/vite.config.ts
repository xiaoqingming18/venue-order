import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // 配置开发服务器
  server: {
    // 启动端口
    port: 3000,
    // 自动打开浏览器
    open: true,
    // 代理配置，解决跨域问题
    proxy: {
      // 代理所有 /api 开头的请求
      '/api': {
        // 目标地址，指向后端服务器
        target: 'http://localhost:8081',
        // 允许跨域
        changeOrigin: true,
        // 不重写路径，因为后端接口也是以/api开头
        rewrite: (path) => path,
      },
    },
  },
})
