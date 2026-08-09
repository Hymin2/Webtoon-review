package com.hymin.chattest.support;

import java.net.URI;
import java.time.Duration;

public record ChatTestProperties(
    URI httpUrl,
    URI webSocketUrl,
    String username,
    String password,
    String clientId,
    String deviceType,
    long roomId,
    Duration timeout
) {

    public ChatTestProperties {
        requireText(username, "CHAT_TEST_USERNAME");
        requireText(password, "CHAT_TEST_PASSWORD");
        requireText(clientId, "CHAT_TEST_CLIENT_ID");

        if (clientId.contains("_")) {
            throw new IllegalArgumentException("CHAT_TEST_CLIENT_ID에는 밑줄(_)을 사용할 수 없습니다.");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout은 0보다 커야 합니다.");
        }
        if (roomId <= 0) {
            throw new IllegalArgumentException("CHAT_TEST_ROOM_ID는 0보다 커야 합니다.");
        }
    }

    public static ChatTestProperties fromEnvironment() {
        return new ChatTestProperties(
            URI.create(environmentOrDefault("CHAT_HTTP_URL", "http://localhost:18080")),
            URI.create(environmentOrDefault("CHAT_WS_URL", "ws://localhost:18080/stomp-chat")),
            System.getenv("CHAT_TEST_USERNAME"),
            System.getenv("CHAT_TEST_PASSWORD"),
            environmentOrDefault("CHAT_TEST_CLIENT_ID", "chat-test-client"),
            environmentOrDefault("CHAT_TEST_DEVICE_TYPE", "CHAT_TEST"),
            Long.parseLong(System.getenv("CHAT_TEST_ROOM_ID")),
            Duration.ofSeconds(Long.parseLong(
                environmentOrDefault("CHAT_TEST_TIMEOUT_SECONDS", "10")))
        );
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static void requireText(String value, String environmentName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(environmentName + " 환경변수가 필요합니다.");
        }
    }
}
