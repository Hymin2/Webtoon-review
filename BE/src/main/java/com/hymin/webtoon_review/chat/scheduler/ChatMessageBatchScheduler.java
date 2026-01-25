package com.hymin.webtoon_review.chat.scheduler;

import com.hymin.webtoon_review.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBatchScheduler {

    private final ChatMessageService chatMessageService;

    @Scheduled(fixedDelay = 1000)
    public void processBatchMessages() {
        chatMessageService.processMessagesBatch();
    }

    @Scheduled(fixedDelay = 30000)
    public void processBatchPendingMessages() {
        chatMessageService.processPendingMessagesBatch();
    }
}
