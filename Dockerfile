# syntax=docker/dockerfile:1

# ---------- Стадия 1: сборка jar с помощью Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Сначала только pom.xml — слой с зависимостями кэшируется между сборками
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Стадия 2: образ для запуска (только JRE + jar) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

VOLUME /tmp
COPY --from=build /workspace/target/lab4.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
