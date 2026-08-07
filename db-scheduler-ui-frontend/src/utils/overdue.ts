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
import { isBefore } from 'date-fns';

/** Dark amber: the warning token is a background colour and unreadable as text. */
export const overdueColor = '#725200';

/**
 * Due in the past and nobody picked it up.
 *
 * One definition, because an overview row and the panel it opens describe the same execution —
 * if they disagree about "overdue", one of them is lying.
 */
export const isOverdue = (
  executionTime: string | null,
  running: boolean,
): boolean =>
  !running && !!executionTime && isBefore(new Date(executionTime), new Date());
