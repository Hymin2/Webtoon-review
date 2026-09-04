package com.hymin.webtoon_review.chat.server.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageCommandMetrics {

    private final Counter transactionRetries;
    private final Counter transactionRetryExhaustions;

    public ChatMessageCommandMetrics(MeterRegistry meterRegistry) {
        transactionRetries = Counter.builder("chat.message.command.transaction.retries")
            .description("Transient Mongo transaction whole-body retries")
            .register(meterRegistry);
        transactionRetryExhaustions = Counter.builder(
                "chat.message.command.transaction.retry.exhaustions"
            )
            .description("Exhausted transient Mongo transaction retries")
            .register(meterRegistry);
    }

    public void transactionRetry() {
        transactionRetries.increment();
    }

    public void transactionRetryExhausted() {
        transactionRetryExhaustions.increment();
    }
}
