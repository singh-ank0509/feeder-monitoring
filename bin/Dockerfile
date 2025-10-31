# ---- Stage 1: Build the application ----
FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source and build
COPY src /app/src
RUN mvn clean package -DskipTests

# ---- Stage 2: Runtime image ----
# FROM openjdk:17-slim
FROM gcr.io/distroless/java17-debian12

WORKDIR /app

# Copy only the JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9090
CMD ["-jar", "app.jar"]