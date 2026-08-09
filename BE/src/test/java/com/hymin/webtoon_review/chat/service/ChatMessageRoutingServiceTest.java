package com.hymin.webtoon_review.chat.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.metrics.ChatMessageMetrics;
import com.hymin.webtoon_review.chat.route.ChatWorkerLocalHashRing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

@ExtendWith(MockitoExtension.class)
class ChatMessageRoutingServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ChatWorkerLocalHashRing chatWorkerLocalHashRing;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private StreamOperations<String, Object, Object> streamOperations;
    @Mock
    private ChatMessageMetrics chatMessageMetrics;

    private ChatMessageRoutingService chatMessageRoutingService;
    private ChatMessageDto chatMessage;

    @BeforeEach
    void setUp() {
        chatMessageRoutingService = new ChatMessageRoutingService(
                objectMapper,
                chatWorkerLocalHashRing,
                redisTemplate,
                chatMessageMetrics
        );
        chatMessage = ChatMessageDto.builder()
                .roomId(1L)
                .messageUUID("message-uuid")
                .build();
    }

    @Test
    void recordsSuccessAfterPublishingToRedisStream() throws Exception {
        when(chatWorkerLocalHashRing.getTargetServerStreamKey(1L)).thenReturn("chat-worker-stream");
        when(objectMapper.writeValueAsString(chatMessage)).thenReturn("{}");
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        chatMessageRoutingService.route(chatMessage);

        verify(chatMessageMetrics).receivedSuccess();
        verify(chatMessageMetrics, never()).receivedFailure();
    }

    @Test
    void recordsFailureWhenMessageCannotBeSerialized() throws Exception {
        when(chatWorkerLocalHashRing.getTargetServerStreamKey(1L)).thenReturn("chat-worker-stream");
        when(objectMapper.writeValueAsString(chatMessage)).thenThrow(
                new JsonProcessingException("직렬화 실패") {
                }
        );

        assertThatThrownBy(() -> chatMessageRoutingService.route(chatMessage))
                .isInstanceOf(RuntimeException.class);

        verify(chatMessageMetrics).receivedFailure();
        verify(chatMessageMetrics, never()).receivedSuccess();
    }
}
