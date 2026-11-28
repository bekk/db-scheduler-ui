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
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';

// Marks history entries this app pushed to open the panel, so closing can pop the entry instead
// of pushing another one that the back button would reopen.
const OPENED_HERE = 'panelOpenedHere';

interface PanelParam {
  value: string | null;
  /** Opens the panel as a new history entry, so the back button closes it. */
  open: (value: string) => void;
  /** Shows something else in the open panel without adding a history entry. */
  replace: (value: string) => void;
  close: () => void;
}

/** A side panel whose content is kept in one query parameter. */
export const usePanelParam = (name: string): PanelParam => {
  const [searchParams, setSearchParams] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();
  const openedHere =
    (location.state as Record<string, unknown> | null)?.[OPENED_HERE] === true;

  const write = (value: string | null, push: boolean) => {
    const params = new URLSearchParams(searchParams);
    if (value) {
      params.set(name, value);
    } else {
      params.delete(name);
    }
    setSearchParams(params, {
      replace: !push,
      state: push
        ? { ...(location.state ?? {}), [OPENED_HERE]: true }
        : location.state,
    });
  };

  return {
    value: searchParams.get(name),
    open: (value) => write(value, true),
    replace: (value) => write(value, false),
    close: () => {
      // Callers can hold an old close, e.g. a delete that completes after the drawer was closed;
      // the live URL says whether the panel is still open.
      if (!new URLSearchParams(window.location.search).has(name)) {
        return;
      }
      if (openedHere) {
        navigate(-1);
      } else {
        // Arrived with the panel already open (reload or shared link): there is no entry to pop.
        write(null, false);
      }
    },
  };
};
