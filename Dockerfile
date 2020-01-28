FROM java:openjdk-8-jdk-alpine as thebuild

COPY . /build
WORKDIR /build

RUN ./mvnw clean install -DskipTests

FROM java:openjdk-8-jre-alpine

RUN mkdir /app
COPY --from=thebuild /build/target/gatekeeper-0.1.0-SNAPSHOT.jar /app/gatekeeper-0.1.0-SNAPSHOT.jar
COPY --from=thebuild /build/config.yml /app/config.yml

WORKDIR /app

ENTRYPOINT \
    java -jar gatekeeper-0.1.0-SNAPSHOT.jar db migrate config.yml && \
    java -jar gatekeeper-0.1.0-SNAPSHOT.jar server config.yml
