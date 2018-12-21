# Hedvig Gatekeeper

How to start the Hedvig Gatekeeper application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/gatekeeper-0.1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8334`

Health Check
---

To see your applications health enter url `http://localhost:8335/healthcheck` or `http://localhost:8334/health/ping`

Database Migrations
---

We use [dbmate](https://github.com/amacneil/dbmate) for database migrations, please refer to the docs for how to use it. Here's a TLDR:
  - Install dbmate via homebrew or download the binary
  - Migrations live under `./db/migrations/*.sql`
  - To run pending migrations, use `DATBASE_URL=postgres://user:password@127.0.0.1:5432/database?sslmode=disable dbamte up`
  - To rollback one migration, use `DATABASE_URL=... dbmate down`
  - To create a new migration template use `dbmate new <insert migration_name>`
  - dbmate (and the app) support .env
