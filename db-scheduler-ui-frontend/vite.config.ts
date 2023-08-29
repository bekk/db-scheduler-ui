import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import eslintPlugin from 'vite-plugin-eslint';

export default defineConfig({
  server: { port: 51373 },
  plugins: [react(), eslintPlugin()],
  resolve: {
    alias: {
      src: '/src',
    },
  },
  build: {
    outDir: '../db-scheduler-ui-backend/src/main/resources/static',
  }
});
