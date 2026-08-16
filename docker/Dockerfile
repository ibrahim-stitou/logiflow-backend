# syntax=docker/dockerfile:1

# --- Étape 1 : build Maven (cache des dépendances) ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests \
    && mv target/logiflow-backend.jar target/app.jar

# --- Étape 2 : runtime léger, utilisateur non-root ---
FROM eclipse-temurin:25-jre-noble AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system logiflow && useradd --system --gid logiflow --home /app logiflow
WORKDIR /app
COPY --from=build /workspace/target/app.jar app.jar
RUN chown -R logiflow:logiflow /app
USER logiflow

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
