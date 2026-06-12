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

/**
 * Normalizes the default export of a CommonJS dependency.
 *
 * Some CJS packages expose their value on `module.exports.default`. Depending on the bundler's
 * interop behaviour, the default import can resolve to the module namespace rather than that inner
 * value (e.g. react-datepicker v4 under Vite 8). This returns the inner `default` when present and
 * falls back to the import itself when a bundler already hands back the value directly.
 */
export const interopDefault = <T>(mod: T): T => (mod as { default?: T }).default ?? mod;
