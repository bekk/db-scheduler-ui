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

const normalizePath = (path: string): string => {
  const trimmedPath = path.trim();
  const pathWithLeadingSlash = trimmedPath.startsWith('/')
    ? trimmedPath
    : `/${trimmedPath}`;
  const normalized = pathWithLeadingSlash.replace(/\/+/g, '/').replace(/\/+$/, '');
  return normalized || '/';
};

const joinPaths = (...paths: Array<string | undefined>): string =>
  normalizePath(paths.filter((path): path is string => path !== undefined).join('/'));

const isAbsoluteUrl = (url: string): boolean =>
  /^[a-z][a-z\d+\-.]*:\/\//i.test(url);

const trimTrailingSlash = (url: string): string => url.replace(/\/+$/, '');

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL as
  | string
  | undefined;

export const getUiBasePath = (): string =>
  normalizePath(
    window.DB_SCHEDULER_UI?.uiBasePath ??
      joinPaths(window.CONTEXT_PATH, window.UI_PATH ?? import.meta.env.BASE_URL),
  );

export const getApiBaseUrl = (): string => {
  if (configuredApiBaseUrl) {
    return trimTrailingSlash(configuredApiBaseUrl);
  }

  const configuredApiBasePath = window.DB_SCHEDULER_UI?.apiBasePath;
  if (configuredApiBasePath !== undefined) {
    if (isAbsoluteUrl(configuredApiBasePath)) {
      return trimTrailingSlash(configuredApiBasePath);
    }

    const apiBasePath = normalizePath(configuredApiBasePath);
    return `${window.location.origin}${apiBasePath === '/' ? '' : apiBasePath}`;
  }

  if (window.API_PATH !== undefined && isAbsoluteUrl(window.API_PATH)) {
    return trimTrailingSlash(window.API_PATH);
  }

  const apiBasePath = normalizePath(
    joinPaths(window.CONTEXT_PATH, window.API_PATH ?? '/db-scheduler-api'),
  );
  return `${window.location.origin}${apiBasePath === '/' ? '' : apiBasePath}`;
};

export const redirectToUi = () => {
  document.location.href = getUiBasePath();
};
