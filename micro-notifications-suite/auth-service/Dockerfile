# Build stage (usa Maven con JDK 21)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copiamos primero los POMs para aprovechar cache
COPY pom.xml ./pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY orchestrator-service/pom.xml orchestrator-service/pom.xml

# Ahora el código SOLO del módulo que vamos a construir
COPY auth-service/src auth-service/src

# Construimos "auth-service" y sus dependencias (-am) desde la raíz
RUN mvn -q -DskipTests -pl auth-service -am package

# Runtime stage (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
