package com.hymin.webtoon_review.chat.server.service;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.common.repository.ChatMessageRepository;
import com.hymin.webtoon_review.chat.common.service.ChatRecentMessageCacheService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("chat")
@RequiredArgsConstructor
public class ChatMessageQueryService {

    private final ChatRecentMessageCacheService chatRecentMessageCacheService;
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessageResponse> getMessagesAfter(Long roomId, Long messageSequence) {
        List<ChatMessageResponse> cachedMessages = chatRecentMessageCacheService
            .getMessagesAfter(roomId, messageSequence);
        if (!cachedMessages.isEmpty()) {
            return cachedMessages;
        }

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
