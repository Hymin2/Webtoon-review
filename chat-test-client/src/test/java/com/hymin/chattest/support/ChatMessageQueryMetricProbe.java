package com.hymin.chattest.support;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestClient;

public final class ChatMessageQueryMetricProbe {

    private static final List<URI> CHAT_SERVER_URLS = List.of(
        URI.create("http://localhost:18081"),
        URI.create("http://localhost:18082")
    );
    private static final String METRIC_NAME = "chat.message.cache.db.loads";

    public long totalDatabaseLoads() {
        return CHAT_SERVER_URLS.stream()
            .mapToLong(this::databaseLoads)
            .sum();
    }

    private long databaseLoads(URI serverUrl) {
        MetricResponse response = RestClient.builder()
            .baseUrl(serverUrl.toString())
            .build()
            .get()
            .uri("/actuator/metrics/{metricName}", METRIC_NAME)
            .retrieve()
            .body(MetricResponse.class);

        if (response == null || response.measurements() == null) {
            return 0L;
        }
        return Math.round(Arrays.stream(response.measurements())
            .filter(measurement -> "COUNT".equals(measurement.statistic()))
            .mapToDouble(Measurement::value)
            .sum());
    }

    private record MetricResponse(Measurement[] measurements) {
    }

    private record Measurement(String statistic, double value) {
    }
}
