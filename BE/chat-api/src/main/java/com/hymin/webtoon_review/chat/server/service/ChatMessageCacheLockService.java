package com.hymin.webtoon_review.chat.server.service;

import com.hymin.webtoon_review.global.constant.RedisKeys;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@Profile("chat")
public class ChatMessageCacheLockService {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5L);
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT =
        new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;

    public ChatMessageCacheLockService(
        StringRedisTemplate redisTemplate,
        @Value("${chat.message-query.distributed-lock-enabled:true}") boolean enabled
    ) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String tryLock(Long roomId) {
        String lockToken = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
            lockKey(roomId),
            lockToken,
            LOCK_TTL
        );
        return Boolean.TRUE.equals(acquired) ? lockToken : null;
    }

    public void unlock(Long roomId, String lockToken) {
        redisTemplate.execute(
            RELEASE_LOCK_SCRIPT,
            List.of(lockKey(roomId)),
            lockToken
        );
    }

    private String lockKey(Long roomId) {
        return RedisKeys.CHAT_ROOM_PREFIX
            + roomId
            + RedisKeys.CHAT_ROOM_MESSAGE_CACHE_LOCK_POSTFIX;
    }
}
