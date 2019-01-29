FROM java:openjdk-8-jdk-alpine as thebuild

COPY . /build
WORKDIR /build

RUN ./mvnw clean install -DskipTests

FROM java:openjdk-8-jre-alpine

RUN apk add --no-cache --update curl
RUN curl -fsSL -o /usr/bin/dbmate https://github.com/amacneil/dbmate/releases/download/v1.4.1/dbmate-linux-musl-amd64
RUN chmod +x /usr/bin/dbmate
RUN apk del curl

RUN mkdir /app
COPY --from=thebuild /build/target/gatekeeper-0.1.0-SNAPSHOT.jar /app/gatekeeper-0.1.0-SNAPSHOT.jar
COPY --from=thebuild /build/config.yml /app/config.yml
COPY --from=thebuild /build/db /app/db

WORKDIR /app

ENTRYPOINT dbmate up && java -jar gatekeeper-0.1.0-SNAPSHOT.jar server config.yml
