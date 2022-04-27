FROM adoptopenjdk:11-jdk as build
WORKDIR /tmp/eureka/
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw clean package -Dmaven.test.skip=true spring-boot:repackage

FROM adoptopenjdk:11-jre
COPY --from=build /tmp/eureka/target/eureka-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]