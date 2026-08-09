package com.hymin.webtoon_review.global.manager;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Profile({"chat-worker", "chat-persister"})
@RequiredArgsConstructor
public class RedisStreamGroupManager {

    private final RedisConnectionFactory redisConnectionFactory;

    public void createStreamAndGroup(String streamKey, String groupName) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.execute(
                "XGROUP",
                bytes("CREATE"),
                bytes(streamKey),
                bytes(groupName),
                bytes("0"),
                bytes("MKSTREAM")
            );
        } catch (RuntimeException exception) {
            if (!isGroupAlreadyExists(exception)) {
                throw exception;
            }
        }
    }

    private boolean isGroupAlreadyExists(Throwable throwable) {
        Throwable cause = throwable;

        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP")) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
