FROM maven:3.9.10-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/backend-api-0.0.1-SNAPSHOT.jar ./backend-api.jar
EXPOSE 8080
CMD ["java", "-jar", "backend-api.jar"]
