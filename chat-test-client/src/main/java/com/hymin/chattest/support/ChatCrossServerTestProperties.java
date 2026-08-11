package com.hymin.chattest.support;

import java.net.URI;

public record ChatCrossServerTestProperties(
    ChatTestProperties userA,
    ChatTestProperties userB
) {

    public static ChatCrossServerTestProperties fromEnvironment() {
        ChatTestProperties common = ChatTestProperties.fromEnvironment();

        return new ChatCrossServerTestProperties(
            new ChatTestProperties(
                common.httpUrl(),
                URI.create(environmentOrDefault(
                    "CHAT_TEST_CHAT_1_WS_URL", "ws://localhost:18081/stomp-chat")),
                common.username(),
                common.password(),
                "cross-server-client-a",
                common.deviceType(),
                common.timeout()
            ),
            new ChatTestProperties(
                common.httpUrl(),
                URI.create(environmentOrDefault(
                    "CHAT_TEST_CHAT_2_WS_URL", "ws://localhost:18082/stomp-chat")),
                requiredEnvironment("CHAT_TEST_SECOND_USERNAME"),
                requiredEnvironment("CHAT_TEST_SECOND_PASSWORD"),
                "cross-server-client-b",
                common.deviceType(),
                common.timeout()
            )
        );
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }
}
