FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache tzdata \
    && addgroup -S appgroup \
    && adduser -S -G appgroup appuser

ENV TZ=Europe/Moscow
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]