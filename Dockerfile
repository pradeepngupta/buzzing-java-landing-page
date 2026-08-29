# Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml ./
COPY src ./src

RUN chmod +x mvnw \
    && ./mvnw --batch-mode -DskipTests clean package

# Run stage
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
