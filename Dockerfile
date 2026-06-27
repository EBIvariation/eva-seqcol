# Build stage - Build without embedding credentials
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /app

# Copy pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.gitcommitid.skip=true

# Runtime stage
FROM eclipse-temurin:8-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r seqcol && useradd -r -g seqcol seqcol

# Create config directory for runtime application.properties mount
RUN mkdir -p /app/config && chown -R seqcol:seqcol /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy service-info.json
COPY --from=build /app/src/main/resources/static/service-info.json /app/service-info.json

# Set ownership
RUN chown -R seqcol:seqcol /app

USER seqcol

EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/eva/webservices/seqcol/health || exit 1

# JVM options for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
