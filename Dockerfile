# syntax=docker/dockerfile:1

FROM maven:3.9.12-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /workspace/target/release-sentinel-api-*.jar /app/release-sentinel-api.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

EXPOSE 10000

USER app

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/release-sentinel-api.jar"]
