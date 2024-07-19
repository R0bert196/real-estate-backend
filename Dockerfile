# Stage 1: Build the JAR file
FROM maven:3.8.5-openjdk-17 as builder

WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ ./src/
RUN mvn clean package -DskipTests=true


# Stage 2: Create the final image
FROM openjdk:17-jdk-slim as prod
RUN mkdir /app

# Create a directory for the images uploads and set as a volume
RUN mkdir /app/uploads
VOLUME /app/uploads

# Copy the built JAR from the previous stage
COPY --from=builder /app/target/*.jar /app/app.jar

WORKDIR /app

# Expose the application's port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app.jar"]