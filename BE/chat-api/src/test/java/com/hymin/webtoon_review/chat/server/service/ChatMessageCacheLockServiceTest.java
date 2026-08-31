package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class ChatMessageCacheLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ChatMessageCacheLockService service;

    @BeforeEach
    void setUp() {
        service = new ChatMessageCacheLockService(redisTemplate, true);
    }

    @Test
    void returnsTokenWhenLockIsAcquired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
            eq("chat:room:1:message-cache:lock"),
            any(String.class),
            eq(Duration.ofSeconds(5L))
        )).thenReturn(true);

        String lockToken = service.tryLock(1L);

        assertThat(lockToken).isNotBlank();
    }

    @Test
    void returnsNullWhenLockIsAlreadyHeld() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
            eq("chat:room:1:message-cache:lock"),
            any(String.class),
            eq(Duration.ofSeconds(5L))
        )).thenReturn(false);

        assertThat(service.tryLock(1L)).isNull();
    }

    @Test
    void releasesOnlyLockOwnedByToken() {
        service.unlock(1L, "lock-token");

        verify(redisTemplate).execute(
            any(RedisScript.class),
            eq(List.of("chat:room:1:message-cache:lock")),
            eq("lock-token")
        );
    }
}
