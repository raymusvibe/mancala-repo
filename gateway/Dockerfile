FROM maven:3-adoptopenjdk-11
WORKDIR /tmp
RUN rm -rf src
RUN rm -rf target/gateway-0.0.1-SNAPSHOT.jar
RUN rm -rf app.jar
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src src
RUN mvn clean
RUN mvn package -Dmaven.test.skip=true spring-boot:repackage

FROM adoptopenjdk:11-jre
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]