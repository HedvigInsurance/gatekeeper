FROM openjdk:11.0-jdk-stretch as thebuild

COPY . /build
WORKDIR /build

RUN ./mvnw clean install -DskipTests

FROM openjdk:11.0-jre-slim-stretch
RUN mkdir /app
COPY --from=thebuild /build/target/gatekeeper-0.1.0-SNAPSHOT.jar /app/gatekeeper-0.1.0-SNAPSHOT.jar
COPY --from=thebuild /build/config.yml /app/config.yml

WORKDIR /app

ENTRYPOINT java -jar gatekeeper-0.1.0-SNAPSHOT.jar server config.yml
