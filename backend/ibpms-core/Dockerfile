# Multi-stage Dockerfile for iBPMS backend service

# ---------- Builder Stage ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose application port (default 8080)
EXPOSE 8080

# Entry point
ENTRYPOINT ["java","-jar","/app/app.jar"]
