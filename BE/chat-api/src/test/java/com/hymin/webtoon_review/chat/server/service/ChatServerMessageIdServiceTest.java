package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ChatServerMessageIdServiceTest {

    private static final String KEY = "chat:message:server-id:1:2:client-message-id";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ChatServerMessageIdService service;

    @BeforeEach
    void setUp() {
        service = new ChatServerMessageIdService(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void reusesExistingServerMessageId() {
        when(valueOperations.get(KEY)).thenReturn("existing-message-id");

        String result = service.getOrCreate(1L, 2L, "client-message-id");

        assertThat(result).isEqualTo("existing-message-id");
        verify(valueOperations, never()).setIfAbsent(
            org.mockito.ArgumentMatchers.eq(KEY),
            anyString(),
            org.mockito.ArgumentMatchers.eq(Duration.ofHours(2L))
        );
    }

    @Test
    void generatesAndStoresServerMessageIdWithTtl() {
        when(valueOperations.get(KEY)).thenReturn(null);
        when(valueOperations.setIfAbsent(
            org.mockito.ArgumentMatchers.eq(KEY),
            anyString(),
            org.mockito.ArgumentMatchers.eq(Duration.ofHours(2L))
        )).thenReturn(true);

        String result = service.getOrCreate(1L, 2L, "client-message-id");

        assertThat(UUID.fromString(result)).isNotNull();
        verify(valueOperations).setIfAbsent(KEY, result, Duration.ofHours(2L));
    }

    @Test
    void usesMessageIdRegisteredByConcurrentRequest() {
        when(valueOperations.get(KEY)).thenReturn(null, "winner-message-id");
        when(valueOperations.setIfAbsent(
            org.mockito.ArgumentMatchers.eq(KEY),
            anyString(),
            org.mockito.ArgumentMatchers.eq(Duration.ofHours(2L))
        )).thenReturn(false);

        String result = service.getOrCreate(1L, 2L, "client-message-id");

        assertThat(result).isEqualTo("winner-message-id");
    }
}
