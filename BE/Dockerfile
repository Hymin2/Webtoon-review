FROM gradle:8.8-jdk17-alpine AS builder

WORKDIR /workspace

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY chat-api/build.gradle chat-api/build.gradle
COPY chat-connection/build.gradle chat-connection/build.gradle
RUN chmod +x gradlew

COPY src src
COPY chat-api/src chat-api/src
COPY chat-connection/src chat-connection/src
RUN ./gradlew bootJar :chat-connection:bootJar --no-daemon \
    && cp build/libs/webtoon-review-root.jar /workspace/root-app.jar \
    && cp chat-connection/build/libs/webtoon-review-chat-connection.jar /workspace/chat-connection-app.jar

FROM eclipse-temurin:17-jre-alpine AS runtime-base

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/logs \
    && chown -R app:app /app

WORKDIR /app

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM runtime-base AS root-runtime

COPY --from=builder --chown=app:app \
    /workspace/root-app.jar \
    /app/app.jar

FROM runtime-base AS chat-connection-runtime

COPY --from=builder --chown=app:app \
    /workspace/chat-connection-app.jar \
    /app/app.jar
