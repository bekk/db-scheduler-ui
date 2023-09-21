export enum FilterBy {
    All = 'All',
    Failed = 'Failed',
    Running = 'Running',
    Scheduled = 'Scheduled',
    Succeeded = 'Succeeded',
  }
  
export interface PaginationParams {
pageNumber: number;
limit: number;
}

export enum SortBy {
Default = 'Default',
Name = 'Name',
}