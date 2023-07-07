# Use Java 17 as the base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml ./

# Download the project dependencies
RUN apt-get update && apt-get install -y maven

# Force build
ENV CACHE_BUST=8

# Copy the application source code to the container
COPY src ./src

# Build the application using Maven
RUN mvn clean package -DskipTests

EXPOSE 8080

# Set the command to run the application
CMD ["java", "-jar", "target/flink-control-api.jar"]
