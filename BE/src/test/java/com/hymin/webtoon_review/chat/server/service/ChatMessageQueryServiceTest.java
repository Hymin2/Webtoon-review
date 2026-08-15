package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.repository.ChatMessageRepository;
import com.hymin.webtoon_review.chat.common.service.ChatRecentMessageCacheService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMessageQueryServiceTest {

    @Mock
    private ChatRecentMessageCacheService chatRecentMessageCacheService;
    @Mock
    private ChatMessageRepository chatMessageRepository;

    private ChatMessageQueryService service;

    @BeforeEach
    void setUp() {
        service = new ChatMessageQueryService(
            chatRecentMessageCacheService,
            chatMessageRepository
        );
    }

    @Test
    void returnsCachedMessagesWithoutQueryingDatabase() {
        List<ChatMessageResponse> cachedMessages = List.of(
            response("message-2", 2L),
            response("message-3", 3L)
        );
        when(chatRecentMessageCacheService.getMessagesAfter(1L, 1L))
            .thenReturn(cachedMessages);

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 1L);

        assertThat(result).isEqualTo(cachedMessages);
        verify(chatMessageRepository, never())
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 1L);
    }

    @Test
    void queriesDatabaseAndCachesMessagesWhenCacheIsEmpty() {
        ChatMessage persistedMessage = ChatMessage.builder()
            .id("message-2")
            .roomId(1L)
            .messageSequence(2L)
            .clientMessageId("client-message-2")
            .roomMemberId("room-member-id")
            .sender("sender")
            .build();
        when(chatRecentMessageCacheService.getMessagesAfter(1L, 1L))
            .thenReturn(List.of());
        when(chatMessageRepository
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 1L))
            .thenReturn(List.of(persistedMessage));

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 1L);

        assertThat(result)
            .extracting(ChatMessageResponse::getMessageId)
            .containsExactly("message-2");
        verify(chatRecentMessageCacheService).cacheAll(result);
    }

    @Test
    void returnsEmptyListWhenCacheAndDatabaseHaveNoMessages() {
        when(chatRecentMessageCacheService.getMessagesAfter(1L, 10L))
            .thenReturn(List.of());
        when(chatMessageRepository
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 10L))
            .thenReturn(List.of());

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 10L);

        assertThat(result).isEmpty();
        verify(chatRecentMessageCacheService).cacheAll(List.of());
    }

    private ChatMessageResponse response(String messageId, Long messageSequence) {
        return ChatMessageResponse.builder()
            .messageId(messageId)
            .roomId(1L)
            .messageSequence(messageSequence)
            .build();
    }
}
