package com.hymin.webtoon_review.chat.server.service;

import com.hymin.webtoon_review.global.constant.RedisKeys;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("chat")
@RequiredArgsConstructor
public class ChatServerMessageIdService {

    private static final Duration SERVER_MESSAGE_ID_TTL = Duration.ofHours(2L);

    private final StringRedisTemplate redisTemplate;

    public String getOrCreate(Long roomId, Long userId, String clientMessageId) {
        String key = getKey(roomId, userId, clientMessageId);
        String existingMessageId = redisTemplate.opsForValue().get(key);
        if (existingMessageId != null) {
            return existingMessageId;
        }

        String generatedMessageId = UUID.randomUUID().toString();
        Boolean registered = redisTemplate.opsForValue().setIfAbsent(
            key,
            generatedMessageId,
            SERVER_MESSAGE_ID_TTL
        );
        if (Boolean.TRUE.equals(registered)) {
            return generatedMessageId;
        }

        existingMessageId = redisTemplate.opsForValue().get(key);
        if (existingMessageId != null) {
            return existingMessageId;
        }

        throw new IllegalStateException("서버 메시지 ID를 Redis에 등록하지 못했습니다.");
    }

    private String getKey(Long roomId, Long userId, String clientMessageId) {
        return RedisKeys.CHAT_MESSAGE_SERVER_ID_PREFIX
            + roomId + ":"
            + userId + ":"
            + clientMessageId;
    }
}
