import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://10.245.181.47:8080',
        changeOrigin: true
      }
    }
  }
})
