FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY config ./config
COPY frontend ./frontend
COPY src ./src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd --system spring && useradd --system spring -g spring

COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/logs/archive \
    && chown -R spring:spring /app

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
