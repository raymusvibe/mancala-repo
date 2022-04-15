FROM maven:3-adoptopenjdk-11
WORKDIR /tmp
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src src
RUN mvn clean package -Dmaven.test.skip=true spring-boot:repackage

FROM adoptopenjdk:11-jre
COPY target/eureka-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]