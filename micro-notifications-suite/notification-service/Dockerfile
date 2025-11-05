FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY orchestrator-service/pom.xml orchestrator-service/pom.xml
COPY notification-service/src notification-service/src
RUN mvn -q -DskipTests -pl notification-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
