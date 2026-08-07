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
import { useSearchParams } from 'react-router-dom';
import {
  OVERVIEW_FILTER_KEYS,
  OverviewFilterKey,
} from 'src/utils/overviewFilters';

// Filters live in the URL so a refresh, back/forward or a shared link restores the view.
const FILTER_PARAM = 'filter';

interface OverviewFilterState {
  activeFilters: OverviewFilterKey[];
  toggleFilter: (key: OverviewFilterKey) => void;
  clearFilters: () => void;
}

/**
 * Owns the whole round-trip between the active filters and the query string, so callers
 * only state what they want and never touch the wire format.
 */
export const useOverviewFilters = (): OverviewFilterState => {
  const [searchParams, setSearchParams] = useSearchParams();
  // Unknown values are dropped: the query string is user-editable.
  const selected = searchParams.get(FILTER_PARAM)?.split(',') ?? [];
  const activeFilters = OVERVIEW_FILTER_KEYS.filter((key) =>
    selected.includes(key),
  );

  const write = (next: OverviewFilterKey[]) => {
    const params = new URLSearchParams(searchParams);
    const ordered = OVERVIEW_FILTER_KEYS.filter((key) => next.includes(key));
    if (ordered.length > 0) {
      params.set(FILTER_PARAM, ordered.join(','));
    } else {
      params.delete(FILTER_PARAM);
    }
    // Replace, not push: toggling a filter is a change of view, not a step worth putting
    // in the back button.
    setSearchParams(params, { replace: true });
  };

  return {
    activeFilters,
    toggleFilter: (key) =>
      write(
        activeFilters.includes(key)
          ? activeFilters.filter((active) => active !== key)
          : [...activeFilters, key],
      ),
    clearFilters: () => write([]),
  };
};
