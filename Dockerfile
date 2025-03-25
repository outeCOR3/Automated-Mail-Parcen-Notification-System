FROM debian:latest

# Install Java, Bash, and necessary tools
RUN apt-get update && apt-get install -y openjdk-17-jdk bash unzip dos2unix

WORKDIR /app

# Copy necessary files
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle.kts /app/
COPY settings.gradle.kts /app/
COPY gradle.properties /app/
COPY server /app/server

# Ensure gradlew is executable
RUN chmod +x /app/gradlew

# Convert gradlew to Unix format (Fixes Windows CRLF issue)
RUN dos2unix /app/gradlew

# Debugging: Check if gradlew works
RUN /bin/bash -c "/app/gradlew --version"
EXPOSE 8080
CMD ["/bin/sh"]
