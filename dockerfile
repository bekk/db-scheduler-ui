# Use the official OpenJDK base image
FROM openjdk:11-jdk-slim

# Set the working directory inside the container
WORKDIR /app

COPY ./*.jar /app/app.jar

EXPOSE 8081

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]