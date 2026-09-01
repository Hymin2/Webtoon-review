package com.hymin.webtoon_review.chat.worker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.service.ChatRecentMessageCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMessageSequenceGeneratorTest {

    @Mock
    private ChatRecentMessageCacheService chatRecentMessageCacheService;

    private ChatMessageSequenceGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ChatMessageSequenceGenerator(chatRecentMessageCacheService);
    }

    @Test
    void returnsSequenceGeneratedByAtomicCacheOperation() {
        ChatMessageResponse message = message();
        when(chatRecentMessageCacheService.generateSequenceAndCache(message)).thenReturn(11L);

        Long result = generator.generateAndCache(message);

        assertThat(result).isEqualTo(11L);
    }

    private ChatMessageResponse message() {
        return ChatMessageResponse.builder()
            .messageId("message-1")
            .roomId(1L)
            .build();
    }
}
