FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
COPY pom.xml /app
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk
WORKDIR /app
COPY --from=build /app/target/invocation.jar /app
EXPOSE 8081/tcp
CMD ["java", "-jar", "invocation.jar"]
