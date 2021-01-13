FROM amazoncorretto:11 as thebuild

COPY . /build
WORKDIR /build

RUN ./mvnw clean install -DskipTests

FROM amazoncorretto:11
RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

RUN mkdir /app
COPY --from=thebuild /build/target/gatekeeper-0.1.0-SNAPSHOT.jar /app/gatekeeper-0.1.0-SNAPSHOT.jar
COPY --from=thebuild /build/config.yml /app/config.yml

WORKDIR /app

ENTRYPOINT \
    java -jar gatekeeper-0.1.0-SNAPSHOT.jar db migrate config.yml && \
    java -javaagent:/dd-java-agent.jar -jar gatekeeper-0.1.0-SNAPSHOT.jar server config.yml
