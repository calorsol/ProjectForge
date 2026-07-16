import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: { proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } } },
  build: {
    rollupOptions: {
      output: {
        // 拆分大依赖，独立缓存、并行下载；富文本编辑器单独成块，工作台首屏无需加载
        manualChunks: {
          'element-plus': ['element-plus'],
          'wangeditor': ['@wangeditor/editor', '@wangeditor/editor-for-vue']
        }
      }
    }
  },
  test: { environment: 'jsdom' }
})
