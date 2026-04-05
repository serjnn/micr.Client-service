FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/ClientService-0.0.1-SNAPSHOT.jar /app/client-service.jar
EXPOSE 7015
ENTRYPOINT ["java", "-jar", "/app/client-service.jar"]
