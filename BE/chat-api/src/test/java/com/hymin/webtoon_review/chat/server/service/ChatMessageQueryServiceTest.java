package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.repository.ChatMessageRepository;
import com.hymin.webtoon_review.chat.common.service.ChatRecentMessageCacheService;
import com.hymin.webtoon_review.chat.server.metrics.ChatMessageQueryMetrics;
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
    @Mock
    private ChatMessageCacheLockService chatMessageCacheLockService;
    @Mock
    private ChatMessageQueryMetrics chatMessageQueryMetrics;

    private ChatMessageQueryService service;

    @BeforeEach
    void setUp() {
        service = new ChatMessageQueryService(
            chatRecentMessageCacheService,
            chatMessageRepository,
            chatMessageCacheLockService,
            chatMessageQueryMetrics
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
        when(chatMessageCacheLockService.isEnabled()).thenReturn(false);
        when(chatMessageRepository
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 1L))
            .thenReturn(List.of(persistedMessage));

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 1L);

        assertThat(result)
            .extracting(ChatMessageResponse::getMessageId)
            .containsExactly("message-2");
        verify(chatRecentMessageCacheService).cacheAll(result);
        verify(chatMessageQueryMetrics).databaseLoad();
    }

    @Test
    void returnsEmptyListWhenCacheAndDatabaseHaveNoMessages() {
        when(chatRecentMessageCacheService.getMessagesAfter(1L, 10L))
            .thenReturn(List.of());
        when(chatMessageCacheLockService.isEnabled()).thenReturn(false);
        when(chatMessageRepository
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 10L))
            .thenReturn(List.of());

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 10L);

        assertThat(result).isEmpty();
        verify(chatRecentMessageCacheService).cacheAll(List.of());
    }

    @Test
    void rechecksCacheAfterAcquiringDistributedLock() {
        List<ChatMessageResponse> cachedMessages = List.of(response("message-2", 2L));
        when(chatRecentMessageCacheService.getMessagesAfter(1L, 1L))
            .thenReturn(List.of(), cachedMessages);
        when(chatMessageCacheLockService.isEnabled()).thenReturn(true);
        when(chatMessageCacheLockService.tryLock(1L)).thenReturn("lock-token");

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 1L);

        assertThat(result).isEqualTo(cachedMessages);
        verify(chatRecentMessageCacheService, times(2)).getMessagesAfter(1L, 1L);
        verify(chatMessageCacheLockService).unlock(1L, "lock-token");
        verify(chatMessageRepository, never())
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 1L);
        verify(chatMessageQueryMetrics, never()).databaseLoad();
    }

    @Test
    void queriesDatabaseOnceWhileHoldingDistributedLock() {
        ChatMessage persistedMessage = ChatMessage.builder()
            .id("message-2")
            .roomId(1L)
            .messageSequence(2L)
            .build();
        when(chatRecentMessageCacheService.getMessagesAfter(1L, 1L))
            .thenReturn(List.of());
        when(chatMessageCacheLockService.isEnabled()).thenReturn(true);
        when(chatMessageCacheLockService.tryLock(1L)).thenReturn("lock-token");
        when(chatMessageRepository
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(1L, 1L))
            .thenReturn(List.of(persistedMessage));

        List<ChatMessageResponse> result = service.getMessagesAfter(1L, 1L);

        assertThat(result).hasSize(1);
        verify(chatMessageQueryMetrics).databaseLoad();
        verify(chatRecentMessageCacheService).cacheAll(result);
        verify(chatMessageCacheLockService).unlock(1L, "lock-token");
    }

    private ChatMessageResponse response(String messageId, Long messageSequence) {
        return ChatMessageResponse.builder()
            .messageId(messageId)
            .roomId(1L)
            .messageSequence(messageSequence)
            .build();
    }
}
