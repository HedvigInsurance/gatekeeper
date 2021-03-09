##### Dependencies stage #####
FROM maven:3.6.3-amazoncorretto-11 AS dependencies
WORKDIR /usr/app

# Set up the user running the tests (needed for embedded postgres)
RUN yum -y install python3 \
    python3-pip \
    shadow-utils \
    util-linux
RUN adduser gatekeeper

# Resolve dependencies and cache them
COPY pom.xml .
RUN mvn dependency:go-offline -s /usr/share/maven/ref/settings-docker.xml
# This is the maven repo in /usr/share/maven/ref/settings-docker.xml
# has to be readable by 'gatekeeper'
RUN chown -R gatekeeper /usr/share/maven/ref/repository


##### Build stage #####
FROM dependencies AS build
COPY src/main src/main
RUN mvn clean package -s /usr/share/maven/ref/settings-docker.xml


##### Test stage #####
FROM build AS test
COPY src/test src/test
RUN chown -R gatekeeper .

# Tests must be run as custom user because of EmbeddedPostgres
RUN su gatekeeper -c 'mvn test -s /usr/share/maven/ref/settings-docker.xml'


##### Assemble stage #####
FROM amazoncorretto:11 AS assemble

# Fetch the datadog agent
RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

COPY --from=build /usr/app/target/gatekeeper-0.1.0-SNAPSHOT.jar .
COPY config.yml .

ENTRYPOINT \
    java -jar gatekeeper-0.1.0-SNAPSHOT.jar db migrate config.yml && \
    java -javaagent:/dd-java-agent.jar -jar gatekeeper-0.1.0-SNAPSHOT.jar server config.yml
