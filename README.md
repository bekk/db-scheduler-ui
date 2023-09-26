# DB Scheduler UI
![build status](https://github.com/bekk/db-scheduler-ui/workflows/Build/badge.svg)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)


Demo version.\
A UI extension of [db-scheduler](https://github.com/kagkarlsson/db-scheduler)


## Features

* **Connects to db-scheduler** to show a UI for your executions.
* **Uses SpringBoot** to launch a UI
* **Re-run or Run** your task directly from the User Interface
* **Sort task** on Task Name or Execution Time
* **Search for Task** on name and task id 
* **View only Tasks that are Scheduled, Running or Failed.**
* **Delete Tasks** 
* **Polling** on Refetch button
* **View a log of all Tasks**
 
DB-scheduler-UI is an extention of DB scheduler. It is built to give a visulasation of the work done by Db scheduler. The UI can also display history if you are using the extention db-scheduler-log, https://github.com/rocketbase-io/db-scheduler-log. 

In the UI you can Run scheduled Task now or Re-run tasks that have Failed, scheduler.reschedule(taskInstance, Instant.now());
You can delete task form the scheuler, scheduler.cancel(taskInstance);
The UI can display data contained in the task, or you can chose to turn this of. 
The UI will show what task have faild how many times and when the next execution time will bee. 
If you have manny task whit the same name, thes will bee shon as a group that can be extende to see all inctances. 

If you have the log extrention the UI will display a History tab that show all task that hav run, bothe succsesfull and failed. They can be sorted on time, filterd on failed/succeeded, or searched on task name, task instance and time intevall.
Failed task will when presed show the stacktrace of the exseption. 

<img alt="Screenshot" src="Screenshot.png" width=700/>

## Table of contents

* [Getting started](#getting-started)
* [Prerequisites](#prerequisites)
* [How it works](#how-it-works)
* [Configuration](#configuration)

### Prerequisites

* An existing Spring Boot application, with [db-scheduler](https://github.com/kagkarlsson/db-scheduler)
* Minimum Java 11 and SpringBoot 2.7
* db-scheduler-log version 0.7.0
* Minimum db-scheduler version 12.5

## Getting started

1. Add maven dependency
```xml
<dependency>
    <groupId>no.bekk.db-scheduler-ui</groupId>
    <artifactId>db-scheduler-ui-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```
```xml
<dependency>
    <groupId>io.rocketbase.extension</groupId>
    <artifactId>db-scheduler-log-spring-boot-starter</artifactId>
    <version>0.7.0</version>
</dependency>
```


## How it works

Db-scheduler-UI adds a library that have a frontend application to show all Task in db-scheduler. 
The db-scheduler-ui backend is connected to the schedule-client. The backend fetches all executions, and 
they are sorted and filtered in the backend before it is displayed in the frontend app. 
As the backend connects to scheduler-client it is possible to run, re-run and delete task form the database in the application.

The URL connects to db-scheduler/**


## Configuration
None
