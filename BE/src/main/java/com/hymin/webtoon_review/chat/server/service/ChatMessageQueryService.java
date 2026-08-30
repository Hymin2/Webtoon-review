package com.hymin.webtoon_review.chat.server.service;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.common.repository.ChatMessageRepository;
import com.hymin.webtoon_review.chat.common.service.ChatRecentMessageCacheService;
import com.hymin.webtoon_review.chat.server.metrics.ChatMessageQueryMetrics;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("chat")
@RequiredArgsConstructor
public class ChatMessageQueryService {

    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(5L);
    private static final Duration CACHE_RETRY_INTERVAL = Duration.ofMillis(10L);

    private final ChatRecentMessageCacheService chatRecentMessageCacheService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageCacheLockService chatMessageCacheLockService;
    private final ChatMessageQueryMetrics chatMessageQueryMetrics;

    public List<ChatMessageResponse> getMessagesAfter(Long roomId, Long messageSequence) {
        List<ChatMessageResponse> cachedMessages = chatRecentMessageCacheService
            .getMessagesAfter(roomId, messageSequence);
        if (!cachedMessages.isEmpty()) {
            return cachedMessages;
        }

        if (!chatMessageCacheLockService.isEnabled()) {
            return loadFromDatabase(roomId, messageSequence);
        }

        long waitDeadline = System.nanoTime() + LOCK_WAIT_TIMEOUT.toNanos();
        while (System.nanoTime() < waitDeadline) {
            String lockToken = chatMessageCacheLockService.tryLock(roomId);
            if (lockToken != null) {
                return loadWithLock(roomId, messageSequence, lockToken);
            }

            LockSupport.parkNanos(CACHE_RETRY_INTERVAL.toNanos());
            cachedMessages = chatRecentMessageCacheService
                .getMessagesAfter(roomId, messageSequence);
            if (!cachedMessages.isEmpty()) {
                return cachedMessages;
            }
        }

        return loadFromDatabase(roomId, messageSequence);
    }

    private List<ChatMessageResponse> loadWithLock(
        Long roomId,
        Long messageSequence,
        String lockToken
    ) {
        try {
            List<ChatMessageResponse> cachedMessages = chatRecentMessageCacheService
                .getMessagesAfter(roomId, messageSequence);
            if (!cachedMessages.isEmpty()) {
                return cachedMessages;
            }
            return loadFromDatabase(roomId, messageSequence);
        } finally {
            chatMessageCacheLockService.unlock(roomId, lockToken);
        }
    }

    private List<ChatMessageResponse> loadFromDatabase(
        Long roomId,
        Long messageSequence
    ) {
        chatMessageQueryMetrics.databaseLoad();

        List<ChatMessageResponse> persistedMessages = chatMessageRepository
            .findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(
                roomId,
                messageSequence
            )
            .stream()
            .map(ChatMapper::toChatMessageResponse)
            .toList();

        chatRecentMessageCacheService.cacheAll(persistedMessages);
        return persistedMessages;
    }
}
