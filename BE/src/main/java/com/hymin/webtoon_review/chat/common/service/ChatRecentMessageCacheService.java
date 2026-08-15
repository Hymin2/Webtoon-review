package com.hymin.webtoon_review.chat.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@Profile({"chat", "chat-worker"})
@RequiredArgsConstructor
public class ChatRecentMessageCacheService {

    private static final Duration CACHE_TTL = Duration.ofHours(2L);
    private static final long MAX_CACHE_SIZE = 300L;
    private static final DefaultRedisScript<Long> CACHE_MESSAGE_SCRIPT =
        new DefaultRedisScript<>("""
            redis.call('HSET', KEYS[2], ARGV[1], ARGV[2])
            redis.call('ZADD', KEYS[1], ARGV[3], ARGV[1])

            local maxSize = tonumber(ARGV[4])
            local trimStop = -(maxSize + 1)
            local evictedIds = redis.call('ZRANGE', KEYS[1], 0, trimStop)
            if #evictedIds > 0 then
                redis.call('ZREMRANGEBYRANK', KEYS[1], 0, trimStop)
                redis.call('HDEL', KEYS[2], unpack(evictedIds))
            end

            redis.call('EXPIRE', KEYS[1], ARGV[5])
            redis.call('EXPIRE', KEYS[2], ARGV[5])
            return #evictedIds
            """, Long.class);

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public void cache(ChatMessageResponse message) {
        validateCacheable(message);

        redisTemplate.execute(
            CACHE_MESSAGE_SCRIPT,
            List.of(
                getRecentMessagesKey(message.getRoomId()),
                getRecentMessageContentsKey(message.getRoomId())
            ),
            message.getMessageId(),
            serialize(message),
            String.valueOf(message.getMessageSequence()),
            String.valueOf(MAX_CACHE_SIZE),
            String.valueOf(CACHE_TTL.toSeconds())
        );
    }

    public void cacheAll(List<ChatMessageResponse> messages) {
        int firstCachedIndex = Math.max(0, messages.size() - (int) MAX_CACHE_SIZE);
        messages.subList(firstCachedIndex, messages.size()).forEach(this::cache);
    }

    public List<ChatMessageResponse> getMessagesAfter(Long roomId, Long messageSequence) {
        String recentMessagesKey = getRecentMessagesKey(roomId);
        Set<String> messageIds = redisTemplate.opsForZSet().rangeByScore(
            recentMessagesKey,
            messageSequence.doubleValue(),
            Double.POSITIVE_INFINITY
        );
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> orderedMessageIds = new ArrayList<>(messageIds);
        List<String> serializedMessages = redisTemplate.<String, String>opsForHash().multiGet(
            getRecentMessageContentsKey(roomId),
            new ArrayList<>(orderedMessageIds)
        );
        if (serializedMessages == null
            || serializedMessages.size() != orderedMessageIds.size()
            || serializedMessages.stream().anyMatch(value -> value == null)) {
            redisTemplate.delete(List.of(
                recentMessagesKey,
                getRecentMessageContentsKey(roomId)
            ));
            return Collections.emptyList();
        }

        return serializedMessages.stream()
            .map(this::deserialize)
            .filter(message -> message.getMessageSequence() != null)
            .filter(message -> message.getMessageSequence() > messageSequence)
            .toList();
    }

    private void validateCacheable(ChatMessageResponse message) {
        if (message.getMessageId() == null) {
            throw new IllegalStateException("서버 메시지 ID가 없는 메시지는 캐싱할 수 없습니다.");
        }
        if (message.getMessageSequence() == null) {
            throw new IllegalStateException("메시지 순번이 없는 메시지는 캐싱할 수 없습니다.");
        }
    }

    private String serialize(ChatMessageResponse message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ChatMessageResponse deserialize(String value) {
        try {
            return objectMapper.readValue(value, ChatMessageResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRecentMessagesKey(Long roomId) {
        return RedisKeys.CHAT_ROOM_PREFIX
            + roomId
            + RedisKeys.CHAT_ROOM_RECENT_MESSAGES_POSTFIX;
    }

    private String getRecentMessageContentsKey(Long roomId) {
        return RedisKeys.CHAT_ROOM_PREFIX
            + roomId
            + RedisKeys.CHAT_ROOM_RECENT_MESSAGE_CONTENTS_POSTFIX;
    }
}
