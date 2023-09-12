import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import eslintPlugin from 'vite-plugin-eslint';

const BASE_URL: string = process.env.NODE_ENV === 'production' ? 'db-scheduler-ui' : '/db-scheduler';


export default defineConfig({
  base: BASE_URL,
  server: {
    port: 51373,
  },
  plugins: [react(), eslintPlugin()],
  resolve: {
    alias: {
      src: '/src',
    },
  },
});
