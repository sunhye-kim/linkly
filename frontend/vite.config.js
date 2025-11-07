import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  // 빌드 결과물을 Spring Boot의 static 폴더로 출력
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },

  // 개발 서버 설정
  server: {
    port: 3000,
    // Spring Boot API로 프록시
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
