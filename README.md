# TL;DR:

`mvn verify` (requires running Docker daemon) results in 1 test failure (`ExampleIT.example2()`) after upgrading to SB3.

# Setup

- Simple SB webapp with embedded h2 db, containerized (using `spring-boot-maven-plugin`) to enable container
  tests (`ExampleIT`)
- `Application` creates a `MY_ENTITY` record with `id` = `1` and `someValue` = `INITIAL` @ app
  startup (`@PostConstruct`)
- `MyService.update()`:
    - executes the following statement (using `java.sql.Statement.executeUpdate`):
      ```
      UPDATE MY_ENTITY 
      SET SOME_VALUE = 'UPDATED'
      WHERE ID = 1
      ```
    - waits 10 seconds before committing
- `ExampleIT` contains 2 tests that query `MY_ENTITY.SOME_VALUE` after `java.sql.Statement.executeUpdate()` but
  before `java.sql.Connection.commit()`. These tests expect to receive the value `INITIAL` as the queries are run
  with `Isolation.READ_COMMITTED`.
    - `example1()` works as expected
    - `example2()` fails after upgrading to SB3
        - this test queries `MY_ENTITY.SOME_VALUE` using `Isolation.READ_UNCOMMITTED` before querying this same value
          using `Isolation.READ_COMMITTED`