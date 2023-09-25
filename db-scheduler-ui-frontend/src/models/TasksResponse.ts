import { Task } from "src/models/Task";
import { Log } from "./Log";


export interface InfiniteScrollResponse<ItemType> {
    items: ItemType[];
    numberOfItems: number;
    numberOfPages: number;
  }
  
  export type TasksResponse = InfiniteScrollResponse<Task>;

  export type LogResponse = InfiniteScrollResponse<Log>;
  