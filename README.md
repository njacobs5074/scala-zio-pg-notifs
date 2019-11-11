# scala-zio-pg-notifs

Simple Scala project that combines using [ZIO](https://zio.dev) and Postgres notifications. Inspired by 
[System design hack: Postgres is a great pub/sub & job server](https://layerci.com/blog/postgres-is-the-answer/) written
by Colin Chartier.

## Build & Test
Project uses [sbt](https://scala-sbt.org), so the usual:
```bash
sbt compile
sbt test
```