# Multi-stage production Dockerfile for MobilityShield API
# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

# Copy Maven wrapper and POM from backend directory
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw

# Copy source code
COPY backend/src/ src/

# Build production JAR (enabling release compatibility for cloud base images)
RUN ./mvnw clean package -DskipTests -Djava.version=21

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Non-root security user
RUN groupadd -r mobilityshield && useradd -r -g mobilityshield mobilityshield
RUN mkdir -p /secrets && chown -R mobilityshield:mobilityshield /secrets

# Copy compiled JAR
COPY --from=build /workspace/app/target/mobilityshield-api-*.jar app.jar
RUN chown -R mobilityshield:mobilityshield /app

USER mobilityshield

# Environment defaults
ENV PORT=8080 \
    FIREBASE_PROJECT_ID=mobilityshield-e60b3 \
    FIREBASE_DATABASE_ID=default \
    GOOGLE_APPLICATION_CREDENTIALS=/secrets/firebase-service-account.json \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
