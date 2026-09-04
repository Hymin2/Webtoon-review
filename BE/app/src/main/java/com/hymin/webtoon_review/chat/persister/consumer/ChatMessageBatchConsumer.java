package com.hymin.webtoon_review.chat.persister.consumer;

import com.hymin.webtoon_review.chat.persister.service.ChatMessagePersistenceService;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat-persister")
public class ChatMessageBatchConsumer implements SmartLifecycle {

    private static final long ERROR_RETRY_DELAY_MILLIS = 1_000L;

    private final ChatMessagePersistenceService chatMessagePersistenceService;
    private final Executor messagesBatchConsumerExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ChatMessageBatchConsumer(
        ChatMessagePersistenceService chatMessagePersistenceService,
        @Qualifier("messagesBatchConsumerExecutor") Executor messagesBatchConsumerExecutor
    ) {
        this.chatMessagePersistenceService = chatMessagePersistenceService;
        this.messagesBatchConsumerExecutor = messagesBatchConsumerExecutor;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        messagesBatchConsumerExecutor.execute(this::consumeContinuously);
        log.info("[채팅 메시지 저장 서버] Redis Stream 연속 소비 시작");
    }

    @Override
    public void stop() {
        running.set(false);
        log.info("[채팅 메시지 저장 서버] Redis Stream 연속 소비 중지 요청");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void consumeContinuously() {
        while (running.get()) {
            try {
                chatMessagePersistenceService.processMessagesBatch();
            } catch (RuntimeException e) {
                log.error("[채팅 메시지 저장 서버] Redis Stream 소비 실패", e);
                waitBeforeRetry();
            }
        }
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(ERROR_RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running.set(false);
        }
    }
}
