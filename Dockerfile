# Build i kombinuar: frontend (Vite/Vue) + backend (Quarkus)
# Përdoret nga Render me Root Directory=. (rrënja e repos), Dockerfile Path=Dockerfile

# --- Faza 1: build frontend ---
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY package.json package-lock.json* ./
RUN npm install
COPY index.html vite.config.* ./
COPY src ./src
COPY public ./public
RUN npm run build

# --- Faza 2: build backend, duke përfshirë dist-in e frontend-it si static resources ---
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY backend/pom.xml .
COPY backend/src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/META-INF/resources
RUN mvn -B -DskipTests package

# --- Faza 3: runtime ---
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest
ENV LANGUAGE='en_US:en'
COPY --chown=185 --from=backend-build /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=backend-build /workspace/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=backend-build /workspace/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=backend-build /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
