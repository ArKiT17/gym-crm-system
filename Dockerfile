FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline

COPY src ./src
RUN mvn --batch-mode package -DskipTests

FROM eclipse-temurin:21-jre

RUN groupadd --system spring && useradd --system --gid spring spring
WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
