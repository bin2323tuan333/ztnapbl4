import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import path from 'path';

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
            '@': path.resolve(__dirname, './src'),
        },
    },
})
