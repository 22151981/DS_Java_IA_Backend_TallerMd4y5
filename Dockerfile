FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./pom.xml
COPY .mvn ./.mvn
COPY mvnw ./mvnw
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
