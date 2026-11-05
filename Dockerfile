# ---------- Stage 1: Build JAR with Maven ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and build fat jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: Minimal Runtime Image ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Copy compiled jar
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Xmx384m -Dserver.port=${PORT:-8080} -jar app.jar"]
