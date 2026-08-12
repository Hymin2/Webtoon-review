package com.hymin.webtoon_review.chat.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMetrics {

    private static final String METRIC_NAME = "chat.messages";

    private final Counter receivedSuccess;
    private final Counter receivedFailure;
    private final Counter workerSuccess;
    private final Counter workerFailure;
    private final Counter persistedSuccess;
    private final Counter persistedFailure;

    public ChatMessageMetrics(MeterRegistry meterRegistry) {
        receivedSuccess = counter(meterRegistry, "received", "success");
        receivedFailure = counter(meterRegistry, "received", "failure");
        workerSuccess = counter(meterRegistry, "worker", "success");
        workerFailure = counter(meterRegistry, "worker", "failure");
        persistedSuccess = counter(meterRegistry, "persisted", "success");
        persistedFailure = counter(meterRegistry, "persisted", "failure");
    }

    public void receivedSuccess() {
        receivedSuccess.increment();
    }

    public void receivedFailure() {
        receivedFailure.increment();
    }

    public void workerSuccess() {
        workerSuccess.increment();
    }

    public void workerFailure() {
        workerFailure.increment();
    }

    public void persisted(int successCount, int failureCount) {
        persistedSuccess.increment(successCount);
        persistedFailure.increment(failureCount);
    }

    private Counter counter(MeterRegistry meterRegistry, String stage, String result) {
        return Counter.builder(METRIC_NAME)
                .description("채팅 메시지 파이프라인 처리 건수")
                .tag("stage", stage)
                .tag("result", result)
                .register(meterRegistry);
    }
}
