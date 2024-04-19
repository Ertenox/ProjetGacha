FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/ApiGacha.jar /app
# Set the command to run the application
EXPOSE 8080/tcp
CMD ["java", "-jar", "ApiGacha.jar"]