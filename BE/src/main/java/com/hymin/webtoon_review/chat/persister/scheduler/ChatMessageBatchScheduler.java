package com.hymin.webtoon_review.chat.persister.scheduler;

import com.hymin.webtoon_review.chat.persister.service.ChatMessagePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat-persister")
@RequiredArgsConstructor
public class ChatMessageBatchScheduler {

    private final ChatMessagePersistenceService chatMessagePersistenceService;

    @Scheduled(fixedDelay = 1000)
    public void processBatchMessages() {
        chatMessagePersistenceService.processMessagesBatch();
    }

    @Scheduled(fixedDelay = 30000)
    public void processBatchPendingMessages() {
        chatMessagePersistenceService.processPendingMessagesBatch();
    }
}
