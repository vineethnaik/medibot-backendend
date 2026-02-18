# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache dumb-init
COPY --from=build /app/target/*.jar app.jar

# Use production profile by default in container
ENV SPRING_PROFILES_ACTIVE=production

EXPOSE 8081
ENTRYPOINT ["dumb-init", "--", "java", "-jar", "app.jar"]
