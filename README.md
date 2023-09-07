# DB Scheduler UI / JobJuggler
Demo version.\
A UI extension of [db-scheduler](https://github.com/kagkarlsson/db-scheduler)


## Features

* **Connects to db-scheduler** to show a UI for your executions.
* **Uses SpringBoot** to launch a UI
* **Re-run or Run** your task directly from the User Interface
* **Sort task** on Task Name or Execution Time
* **View only Tasks that are Scheduled, Running or Failed.**
* **Delete Tasks** 
* **Polling** every 2 seconds.

![Screenshot](Screenshot.png)

## Table of contents

* [Getting started](#getting-started)
* [Prerequisites](#prerequisites)
* [How it works](#how-it-works)
* [Example](#example)
* [Configuration](#configuration)

### Prerequisites

* An existing Spring Boot application, with [db-scheduler](https://github.com/kagkarlsson/db-scheduler)
* Minimum Java 11 and SpringBoot 2.7
* db-scheduler version????

## Getting started

1. Add maven dependency
```xml
<dependency>
    <groupId>com.github.bekk</groupId>
    <artifactId>db-scheduler-ui-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```


## How it works

Legger til et ???
Som bruker db-scheduler som 

Frontend 

URL går til jobjuggler/** (db-scheduler/*)


## Configuration
None