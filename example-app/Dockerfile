FROM eclipse-temurin:17-jre
WORKDIR /app
# Expects the Spring Boot jar to be built first (./mvnw install -DskipTests).
COPY target/example-app-*.jar /app/app.jar
EXPOSE 8081
CMD ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
