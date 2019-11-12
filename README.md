# scala-zio-pg-notifs

Simple Scala project that combines using [ZIO](https://zio.dev) and Postgres notifications. Inspired by 
[System design hack: Postgres is a great pub/sub & job server](https://layerci.com/blog/postgres-is-the-answer/) written
by Colin Chartier.

## Database
This project requires an actual database. I used PostgreSQL 11.5 but the features used herein has been available
since at least version 9.1.

NB: Run your database server on localhost on port 5432 (this is the default PostgreSQL port)

### Database Configuration 
You should only need to do this once. Changes to the table schema are beyond the scope of this simple project.

1. Create a database called `zio_pg_notifs`
2. Copy & run the script in the `resources/db/jobs.sql` into the `zio_pg_notifs` database.

## Build & Test
Project uses [sbt](https://scala-sbt.org), so the usual commands will compile and run the tests.
```bash
sbt compile
sbt test
```

## Code

### Listen/Notify
The code to demonstrate the use of PostgreSQL notifications is in `wrappers.pgnotifs.PostgresIO`. More documentation
on the JDBC interface to LISTEN/NOTIFY facility can be found [here](https://jdbc.postgresql.org/documentation/head/listennotify.html)
The SQL server level documentation on this facility is [here](https://www.postgresql.org/docs/11/sql-notify.html) 

### Jobs Table
The code that provides some basic CRUD operations for the `jobs` table is in the file `src/main/scala/model/jobs.scala`
I've written a simple entity class and then an object `JobsRepository` that provides those operations.

### Tests
The code that exercises the above facilities can be found in the `test/scala/tests` directory. There are 2 test suites:

- `tests.ModelTest` - Exercises the basic CRUD functionality. Basically just a sanity check for that.
- `tests.NotificationsTest` - This exercises the listen/notify functionality but also uses the `JobRepository` to create
records in the database. 