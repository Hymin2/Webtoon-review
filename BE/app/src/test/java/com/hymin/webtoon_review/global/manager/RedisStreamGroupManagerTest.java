package com.hymin.webtoon_review.global.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@ExtendWith(MockitoExtension.class)
class RedisStreamGroupManagerTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @InjectMocks
    private RedisStreamGroupManager redisStreamGroupManager;

    @BeforeEach
    void setUp() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
    }

    @Test
    void Stream과_Consumer_Group을_생성한다() {
        redisStreamGroupManager.createStreamAndGroup("stream-key", "group-name");

        verify(redisConnection).execute(eq("XGROUP"), any(byte[][].class));
    }

    @Test
    void Consumer_Group이_이미_존재하면_정상_처리한다() {
        when(redisConnection.execute(eq("XGROUP"), any(byte[][].class)))
            .thenThrow(new RuntimeException("BUSYGROUP Consumer Group name already exists"));

        assertDoesNotThrow(() ->
            redisStreamGroupManager.createStreamAndGroup("stream-key", "group-name")
        );
    }

    @Test
    void Consumer_Group_중복이_아닌_Redis_오류는_전파한다() {
        when(redisConnection.execute(eq("XGROUP"), any(byte[][].class)))
            .thenThrow(new RuntimeException("Redis connection failed"));

        assertThrows(RuntimeException.class, () ->
            redisStreamGroupManager.createStreamAndGroup("stream-key", "group-name")
        );
    }
}
