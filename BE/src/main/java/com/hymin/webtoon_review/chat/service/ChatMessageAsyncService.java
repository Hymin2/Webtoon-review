package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.repository.ChatMessageRepository;
import com.hymin.webtoon_review.global.annotation.Queue;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.QueueProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Queue(topic = "chat", threshold = 100)
public class ChatMessageAsyncService implements QueueProcessor<ChatMessage> {

    private final ChatMessageRepository chatMessageRepository;

    @Async
    @Override
    public void process(List<Job<ChatMessage>> jobs) {
        chatMessageRepository.saveChatMessages(
            jobs.stream()
                .map(Job::getData)
                .toList()
        );
    }

    @Override
    public Class<ChatMessage> getType() {
        return ChatMessage.class;
    }
}
