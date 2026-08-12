package com.hymin.webtoon_review.chat.worker.service;

import com.hymin.webtoon_review.chat.entity.ChatMessage;
import com.hymin.webtoon_review.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("chat-worker")
@RequiredArgsConstructor
public class ChatMessageSequenceQueryService {

    private final ChatMessageRepository chatMessageRepository;

    public Long getMaxMessageSequence(Long roomId) {
        return chatMessageRepository.findFirstByRoomIdOrderByMessageSequenceDesc(roomId)
            .map(ChatMessage::getMessageSequence)
            .orElse(0L);
    }
}
