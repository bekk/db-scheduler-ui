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
export {};

declare global {
    interface Window {
        /** Path to prepended to API calls and the router basename (optional). */
        CONTEXT_PATH?: string;
        /** Frontend SPA path used when DB_SCHEDULER_UI is not injected. */
        UI_PATH?: string;
        /** API path used when DB_SCHEDULER_UI is not injected. */
        API_PATH?: string;
        DB_SCHEDULER_UI?: {
            contextPath: string;
            routePath?: string;
            uiBasePath: string;
            apiBasePath: string;
        };
    }
}
