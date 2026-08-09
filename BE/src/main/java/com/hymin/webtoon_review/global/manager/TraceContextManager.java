package com.hymin.webtoon_review.global.manager;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraceContextManager {

    private final Tracer tracer;

    public TraceScope startNewSpan(String spanName) {
        Span span = tracer.nextSpan().name(spanName).start();

        MDC.put("traceId", span.context().traceId());
        MDC.put("spanName", spanName);

        return new TraceScope(span, tracer.withSpan(span));
    }

    public TraceScope setExternalTraceId(String traceId, String spanName) {
        Span span;

        if (traceId == null || traceId.length() != 32) {
            log.warn("유효하지 않은 TraceID. 새 추적 시작: {}", traceId);
            span = tracer.nextSpan().name(spanName).start();
        } else {
            TraceContext remoteContext = tracer.traceContextBuilder()
                .traceId(traceId)
                .spanId(tracer.nextSpan().context().spanId())
                .sampled(true)
                .build();

            span = tracer.spanBuilder()
                .setParent(remoteContext)
                .name(spanName)
                .start();
        }

        MDC.put("traceId", span.context().traceId());
        MDC.put("spanName", spanName);

        return new TraceScope(span, tracer.withSpan(span));
    }

    public void putChatMDC(Long roomId, Long userId) {
        MDC.put("roomId", String.valueOf(roomId));
        MDC.put("userId", String.valueOf(userId));
    }

    public void removeChatMDC() {
        MDC.remove("roomId");
        MDC.remove("userId");
    }

    public record TraceScope(Span span, Tracer.SpanInScope scope) implements AutoCloseable {

        @Override
        public void close() {
            scope.close();
            span.end();
            MDC.remove("traceId");
            MDC.remove("spanName");
            MDC.clear();
        }
    }
}
