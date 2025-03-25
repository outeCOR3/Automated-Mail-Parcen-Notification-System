# Stage 1: Build with Gradle (caches dependencies)
FROM gradle:8-jdk17-alpine AS builder
WORKDIR /app

# 1. Copy only files needed for dependency resolution (optimizes caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY server/build.gradle.kts server/
COPY shared/build.gradle.kts shared/

# 2. Download dependencies (cached unless build files change)
RUN gradle --no-daemon dependencies

# 3. Copy source code
COPY server/src server/src
COPY shared/src shared/src

# 4. Build the server fat JAR (using Shadow plugin)
RUN gradle --no-daemon --build-cache :server:shadowJar

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy only the built JAR from the builder
COPY --from=builder /app/server/build/libs/server-*.jar /app/server.jar

# Healthcheck for Render (adjust path if needed)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/health || exit 1

EXPOSE 8080
CMD ["java", "-jar", "server.jar"]