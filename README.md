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
* **View only Tasks that are Scheduled, Running or Failed.**
* **View the history of all Tasks**
* **Delete Tasks** 
* **Polling** every 2 seconds.

<img alt="Screenshot" src="Screenshot.png" width=700/>

## Table of contents

* [Getting started](#getting-started)
* [Prerequisites](#prerequisites)
* [How it works](#how-it-works)
* [Configuration](#configuration)

### Prerequisites

* An existing Spring Boot application, with [db-scheduler](https://github.com/kagkarlsson/db-scheduler)
* Minimum db-scheduler version 12.5
* Minimum Java 11 and SpringBoot 2.7
* Optional (if you want task history): db-scheduler-log version 0.7.0


## Getting started

Read the [db-scheduler](https://github.com/kagkarlsson/db-scheduler) readme and follow the getting started guide.
You do not need to add db-scheduler as a dependency, it

1. Add the spring boot starter maven dependency
```xml
<dependency>
    <groupId>no.bekk.db-scheduler-ui</groupId>
    <artifactId>db-scheduler-ui-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Task history

```xml
<dependency>
    <groupId>io.rocketbase.extension</groupId>
    <artifactId>db-scheduler-log-spring-boot-starter</artifactId>
    <version>0.7.0</version>
</dependency>
```


## How it works

We currently require that you use spring boot to run your application.
Rest controllers providing DB-Scheduler tasks will be injected when you add db-scheduler-ui-starter as a dependency. 
The frontend is a built React app that can be reached by going to `<your-app-url>/db-scheduler`


Db-scheduler-UI adds a library that have a frontend application to show all Task in db-scheduler. 
The db-scheduler-ui backend is connected to the schedule-client. The backend fetches all executions, and 
they are sorted and filtered in the backend before it is displayed in the frontend app. 
As the backend connects to scheduler-client it is possible to run, re-run and delete task form the database in the application.

The URL connects to db-scheduler/**


## Configuration
None


## Contributing

Feel free to create pull requests if there are features or improvements you want to add. 
PR's need to be approved by one of the maintainers. To publish a new version, create a release in Github and tag it with a SemVer version.
A new release will then be released to maven central by a github action using JReleaser.
