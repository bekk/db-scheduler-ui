import { FilterBy, SortBy } from "./QueryParams";

export interface TaskRequestParams {
    filter: FilterBy;
    pageNumber?: number;
    limit?: number;
    size?: number;
    sorting?: SortBy;
    asc?: boolean;
    searchTerm?: string;
    startTime?: Date;
    endTime?: Date;
    refresh?: boolean;
  }

export interface TaskDetailsRequestParams extends TaskRequestParams {
    taskName?: string;
    taskId?: string;
}