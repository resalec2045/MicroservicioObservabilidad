FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY orchestrator-service/pom.xml orchestrator-service/pom.xml
COPY orchestrator-service/src orchestrator-service/src
RUN mvn -q -DskipTests -pl orchestrator-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/orchestrator-service/target/orchestrator-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
