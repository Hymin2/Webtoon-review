FROM gradle:8.8-jdk17-alpine AS builder

WORKDIR /workspace

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon \
    && cp build/libs/*.jar /workspace/app.jar

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/logs \
    && chown -R app:app /app

WORKDIR /app

COPY --from=builder --chown=app:app \
    /workspace/app.jar \
    /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
