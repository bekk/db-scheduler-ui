# Changelog

## Unreleased

### Breaking changes

- **Execution-log writer is now built in.** The `io.rocketbase.extension:db-scheduler-log-spring-boot-starter` dependency is no longer required; `db-scheduler-ui-starter` (Spring Boot 3) and `db-scheduler-ui-spring-boot-4-starter` (Spring Boot 4) now auto-configure the writer themselves.
- **Property prefix renamed**: `db-scheduler-log.*` → `db-scheduler-ui.log.*`.
  - Applications that still set `db-scheduler-log.enabled=true` will fail at startup with a migration message. Remove the rocketbase starter from your dependencies and rename the properties.
- **Package rename**: classes previously imported from `io.rocketbase.extension.*` now live under `no.bekk.dbscheduler.ui.log.*`:
  - `io.rocketbase.extension.*` → `no.bekk.dbscheduler.ui.log.*`

### Migration

1. Remove the dependency:
   ```xml
   <!-- delete -->
   <dependency>
     <groupId>io.rocketbase.extension</groupId>
     <artifactId>db-scheduler-log-spring-boot-starter</artifactId>
     <version>0.7.0</version>
   </dependency>
   ```
2. Rename properties (`application.properties` / `application.yml`):
   ```diff
   - db-scheduler-log.enabled=<...>
   - db-scheduler-log.table-name=<...>
   + db-scheduler-ui.log.enabled=<...>
   + db-scheduler-ui.log.table-name=<...>
   ```
3. Update imports of any custom `LogRepository` / `IdProvider` implementations to the `no.bekk.dbscheduler.ui.log.*` package.
