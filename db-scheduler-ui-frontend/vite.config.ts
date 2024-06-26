/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import eslintPlugin from 'vite-plugin-eslint';

const BASE_URL: string =
  process.env.NODE_ENV === 'production' ? '/db-scheduler' : '/';

export default defineConfig({
  base: BASE_URL,
  server: {
    port: 51373,
    proxy: {
      '/db-scheduler-api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
  build: {
    target: 'es2022',
  },
  plugins: [react(), eslintPlugin()],
  resolve: {
    alias: {
      src: '/src',
    },
  },
});
