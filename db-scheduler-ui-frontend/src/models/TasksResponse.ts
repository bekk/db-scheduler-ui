import { Task } from "src/models/Task";

export type TasksResponse = {
    tasks: Task[];
    numberOfTasks: number;
    numberOfPages: number;
}
