# 🏋️ Use a minimal runtime image (Java 21)
FROM openjdk:21-jdk-slim AS runtime
WORKDIR /app

# Copy only the prebuilt application
COPY server/build/install/server/ ./

# Expose port
EXPOSE 8080

# Run the server
CMD ["./bin/server"]
