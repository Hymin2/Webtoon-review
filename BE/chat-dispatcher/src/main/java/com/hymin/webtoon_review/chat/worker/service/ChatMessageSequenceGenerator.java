package com.hymin.webtoon_review.chat.worker.service;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.service.ChatRecentMessageCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("chat-worker")
@RequiredArgsConstructor
public class ChatMessageSequenceGenerator {

    private final ChatRecentMessageCacheService chatRecentMessageCacheService;

    public Long generateAndCache(ChatMessageResponse message) {
        Long sequence = chatRecentMessageCacheService.generateSequenceAndCache(message);
        if (sequence == null) {
            throw new IllegalStateException("메시지 순번 생성 결과가 없습니다.");
        }
        return sequence;
    }
}
