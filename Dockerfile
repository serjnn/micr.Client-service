# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and resolve dependencies
COPY pom.xml .
RUN mvn dependency:resolve-plugins -B && \
    mvn dependency:resolve -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B && ls -lh target/

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/client-service.jar /app/client-service.jar

EXPOSE 7015

ENTRYPOINT ["java", "-jar", "/app/client-service.jar"]
