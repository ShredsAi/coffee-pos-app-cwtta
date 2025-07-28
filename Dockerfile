FROM openjdk:17-jdk-slim AS build

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Create app directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/product-catalog-shred-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Add health check using a simple HTTP endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/products?page=0&size=1 || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]