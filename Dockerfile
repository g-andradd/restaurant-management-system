# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the POM first so dependency resolution is a separate, cached layer.
# Only invalidated when pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy sources after dependencies are cached.
COPY src ./src
RUN mvn -B -DskipTests package

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /build/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
