# Hedvig Gatekeeper

How to start the Hedvig Gatekeeper application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/gatekeeper-0.1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8040`

Health Check
---

To see your applications health enter url `http://localhost:8041/healthcheck` or `http://localhost:8040/health/ping`

Database Migrations
---

We use Liquibase for database migrations, please refer to the docs for how to use it. Here's a TLDR:
  - Migrations live under `./resources/migrations/`
  - Create target `mvn clean install -DskipTests`
  - To run pending migrations, use `java -jar target/gatekeeper-0.1.0-SNAPSHOT.jar db migrate config.yml`
