# syntax=docker/dockerfile:1

# ---- Build stage ----
# Jammy (not alpine) so this builds on both amd64 CI runners and arm64 dev machines.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copy just the wrapper/build files first so dependency resolution is cached
# independently of source changes.
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
