import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import path from 'path';

export default defineConfig({
    plugins: [react()],
    server: {
        host: true,
        allowedHosts: true,
        port: 3000,
        // Không dùng proxy: apiClient gọi thẳng http://<hostname>:443 để chạy được qua LAN
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
})
