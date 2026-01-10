import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import Inspector from 'unplugin-vue-dev-locator/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import traeBadgePlugin from 'vite-plugin-trae-solo-badge'

export default defineConfig({
  build: {
    sourcemap: 'hidden',
  },
  server: {
    port: 5173,
    proxy: {
      // 1. 代理 API 接口请求
      '/api': {
        // 🔴 关键修改：确认你的后端端口！通常是 8080，不是 8085。
        // 如果你确定后端跑在 8085，那就不用改这行。但如果是默认启动，请改成 8080。
        target: 'http://localhost:8085', 
        changeOrigin: true,
        // 🔴 保持注释状态：不要打开 rewrite！
        // 因为你的后端 UserController 已经加了 @RequestMapping("/api")
        // rewrite: (path) => path.replace(/^\/api/, '') 
      },
      // 2. 代理静态资源
      '/images': {
        target: 'http://localhost:8085', // 🔴 修正：不要加 /images 后缀
        changeOrigin: true,
      }
    }
  },
  plugins: [
    vue(),
    Inspector(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    traeBadgePlugin({
      variant: 'dark',
      position: 'bottom-right',
      prodOnly: true,
      clickable: true,
      clickUrl: 'https://www.trae.ai/solo?showJoin=1',
      autoTheme: true,
      autoThemeTarget: '#app',
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), 
    },
  },
})