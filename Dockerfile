FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build --chown=appuser:appuser /workspace/target/college-management-api.jar /app/app.jar

USER appuser
EXPOSE 10000

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
