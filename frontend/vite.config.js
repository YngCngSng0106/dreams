import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

export default defineConfig({
    plugins: [uni()],
    server: {
        port: 3000,
        host: '0.0.0.0',
        proxy: {
            '/api': {
                target: 'http://10.245.181.47:8080',
                changeOrigin: true,
                secure: false
            }
        }
    }
});
