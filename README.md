# Hedvig Gatekeeper

How to start the Hedvig Gatekeeper application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/gatekeeper-0.1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8334`

Health Check
---

To see your applications health enter url `http://localhost:8335/healthcheck` or `http://localhost:8334/health/ping`
