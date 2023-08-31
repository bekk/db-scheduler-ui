# Use the official OpenJDK base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Try using /home/runner/work/db-scheduler-ui/db-scheduler-ui/db-scheduler-ui-backend/target/db-scheduler-ui-backend-0.0.1-SNAPSHOT.jar?
COPY ./*.jar /app/app.jar

# Expose port 8080 for the application
EXPOSE 8081

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]
