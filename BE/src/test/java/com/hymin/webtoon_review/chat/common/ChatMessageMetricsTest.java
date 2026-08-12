package com.hymin.webtoon_review.chat.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatMessageMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private ChatMessageMetrics chatMessageMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        chatMessageMetrics = new ChatMessageMetrics(meterRegistry);
    }

    @Test
    void recordsSuccessfulMessagePipelineStages() {
        chatMessageMetrics.receivedSuccess();
        chatMessageMetrics.workerSuccess();
        chatMessageMetrics.persisted(3, 0);

        assertThat(count("received", "success")).isEqualTo(1);
        assertThat(count("worker", "success")).isEqualTo(1);
        assertThat(count("persisted", "success")).isEqualTo(3);
    }

    @Test
    void recordsFailedMessagePipelineStages() {
        chatMessageMetrics.receivedFailure();
        chatMessageMetrics.workerFailure();
        chatMessageMetrics.persisted(0, 2);

        assertThat(count("received", "failure")).isEqualTo(1);
        assertThat(count("worker", "failure")).isEqualTo(1);
        assertThat(count("persisted", "failure")).isEqualTo(2);
    }

    private double count(String stage, String result) {
        return meterRegistry.get("chat.messages")
                .tags("stage", stage, "result", result)
                .counter()
                .count();
    }
}
