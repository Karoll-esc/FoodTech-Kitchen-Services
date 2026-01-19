# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src ./src

# Build application
RUN ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]