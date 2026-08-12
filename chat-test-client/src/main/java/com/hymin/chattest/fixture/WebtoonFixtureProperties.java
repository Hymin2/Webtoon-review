package com.hymin.chattest.fixture;

public record WebtoonFixtureProperties(
    String databaseUrl,
    String databaseUsername,
    String databasePassword,
    String webtoonName
) {

    public WebtoonFixtureProperties {
        requireText(databaseUrl, "CHAT_TEST_DB_URL");
        requireText(databaseUsername, "CHAT_TEST_DB_USERNAME");
        requireText(databasePassword, "CHAT_TEST_DB_PASSWORD");
        requireText(webtoonName, "CHAT_TEST_WEBTOON_NAME");
    }

    public static WebtoonFixtureProperties fromEnvironment() {
        return new WebtoonFixtureProperties(
            environmentOrDefault(
                "CHAT_TEST_DB_URL",
                "jdbc:mysql://localhost:13306/webtoon_review?serverTimezone=Asia/Seoul"
            ),
            environmentOrDefault("CHAT_TEST_DB_USERNAME", "webtoon"),
            environmentOrDefault("CHAT_TEST_DB_PASSWORD", "webtoon-local-password"),
            environmentOrDefault("CHAT_TEST_WEBTOON_NAME", "채팅 E2E 테스트 웹툰")
        );
    }

    public WebtoonFixtureProperties withWebtoonName(String newWebtoonName) {
        return new WebtoonFixtureProperties(
            databaseUrl,
            databaseUsername,
            databasePassword,
            newWebtoonName
        );
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static void requireText(String value, String environmentName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(environmentName + " 값이 필요합니다.");
        }
    }
}
