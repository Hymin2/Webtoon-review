package com.hymin.webtoon_review.chat.server.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("chat")
public class ChatMessageQueryMetrics {

    private static final String DATABASE_LOAD_METRIC = "chat.message.cache.db.loads";

    private final Counter databaseLoads;

    public ChatMessageQueryMetrics(MeterRegistry meterRegistry) {
        databaseLoads = Counter.builder(DATABASE_LOAD_METRIC)
            .description("메시지 캐시 미스로 MongoDB를 조회한 횟수")
            .register(meterRegistry);
    }

    public void databaseLoad() {
        databaseLoads.increment();
    }
}
