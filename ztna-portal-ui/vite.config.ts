import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        host: true,
        allowedHosts: true,
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
        },
    },
    resolve: {
        alias: {
            // Cấu hình này giúp Vite hiểu @/ là thư mục src/
            '@': path.resolve(__dirname, './src'),
        },
    },
})
