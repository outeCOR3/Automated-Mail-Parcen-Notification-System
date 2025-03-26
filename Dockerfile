# Use OpenJDK as base image
FROM openjdk:17 AS build

WORKDIR /app

# Copy project files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Ensure gradlew is executable
RUN bash -c 'chmod +x ./gradlew'

# Run Gradle build
RUN ./gradlew build --no-daemon
