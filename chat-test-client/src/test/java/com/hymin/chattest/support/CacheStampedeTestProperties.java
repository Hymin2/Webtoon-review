package com.hymin.chattest.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public record CacheStampedeTestProperties(
    Path backendDirectory,
    Path environmentFile,
    String dockerImage,
    String redisHost,
    int redisPort,
    String redisPassword,
    String mongoHost,
    int mongoPort,
    String mongoDatabase,
    int requestCount,
    int fixtureMessageCount
) {

    public static CacheStampedeTestProperties fromEnvironment() {
        Path backendDirectory = resolveBackendDirectory();
        Path environmentFile = backendDirectory.resolve(".env");
        Map<String, String> fileEnvironment = readEnvironmentFile(environmentFile);

        return new CacheStampedeTestProperties(
            backendDirectory,
            environmentFile,
            value("CHAT_CACHE_STAMPEDE_IMAGE", "webtoon-review-backend:local"),
            value("CHAT_TEST_REDIS_HOST", "localhost"),
            integerValue("CHAT_TEST_REDIS_PORT", fileEnvironment.getOrDefault("REDIS_PORT", "6379")),
            value("CHAT_TEST_REDIS_PASSWORD", fileEnvironment.getOrDefault("REDIS_PASSWORD", "")),
            value("CHAT_TEST_MONGODB_HOST", "localhost"),
            integerValue("CHAT_TEST_MONGODB_PORT", fileEnvironment.getOrDefault("MONGODB_PORT", "27017")),
            value("CHAT_TEST_MONGODB_DATABASE", "test"),
            integerValue("CHAT_CACHE_STAMPEDE_REQUEST_COUNT", "100"),
            integerValue("CHAT_CACHE_STAMPEDE_MESSAGE_COUNT", "100")
        );
    }

    private static Path resolveBackendDirectory() {
        Path workingDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(workingDirectory.resolve("BE"))) {
            return workingDirectory.resolve("BE");
        }
        if (workingDirectory.getFileName().toString().equals("chat-test-client")) {
            return workingDirectory.getParent().resolve("BE");
        }
        throw new IllegalStateException("BE 디렉터리를 찾을 수 없습니다: " + workingDirectory);
    }

    private static Map<String, String> readEnvironmentFile(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalStateException("Docker 환경 파일이 없습니다: " + path);
        }
        Map<String, String> values = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                values.put(
                    trimmed.substring(0, separator).trim(),
                    stripQuotes(trimmed.substring(separator + 1).trim())
                );
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Docker 환경 파일을 읽지 못했습니다: " + path, exception);
        }
        return values;
    }

    private static int integerValue(String name, String defaultValue) {
        return Integer.parseInt(value(name, defaultValue));
    }

    private static String value(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
            && ((value.startsWith("\"") && value.endsWith("\""))
            || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
