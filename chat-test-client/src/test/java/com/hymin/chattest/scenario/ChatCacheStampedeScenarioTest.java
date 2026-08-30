package com.hymin.chattest.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.chattest.fixture.ChatTestContext;
import com.hymin.chattest.fixture.ChatTestFixture;
import com.hymin.chattest.fixture.WebtoonFixtureProperties;
import com.hymin.chattest.support.CacheStampedeDataProbe;
import com.hymin.chattest.support.CacheStampedeLoadClient;
import com.hymin.chattest.support.CacheStampedeLoadClient.Result;
import com.hymin.chattest.support.CacheStampedeTestProperties;
import com.hymin.chattest.support.ChatMessageQueryMetricProbe;
import com.hymin.chattest.support.ChatTestProperties;
import com.hymin.chattest.support.DockerChatServerController;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("cache-stampede")
class ChatCacheStampedeScenarioTest {

    private static final int DEFAULT_LOAD_BALANCER_PORT = 18080;
    private static final int BEFORE_LOAD_BALANCER_PORT = 18180;
    private static final int AFTER_LOAD_BALANCER_PORT = 18190;
    private static final Duration DATABASE_LOAD_DELAY = Duration.ofMillis(100L);

    @Test
    @DisplayName("DB 조회가 지연될 때 100개 동시 요청의 분산 락 적용 전후를 비교한다")
    void 캐시가_없을_때_분산락_적용_전후를_비교한다() {
        CacheStampedeTestProperties properties = CacheStampedeTestProperties.fromEnvironment();
        DockerChatServerController serverController = new DockerChatServerController(properties);
        ChatMessageQueryMetricProbe metricProbe = new ChatMessageQueryMetricProbe();
        String testRunId = "stampede-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            URI beforeUrl = loadBalancerUrl(BEFORE_LOAD_BALANCER_PORT);
            URI afterUrl = loadBalancerUrl(AFTER_LOAD_BALANCER_PORT);
            serverController.start(
                false,
                BEFORE_LOAD_BALANCER_PORT,
                DATABASE_LOAD_DELAY
            );
            ChatTestContext context = new ChatTestFixture(
                testUser(beforeUrl),
                WebtoonFixtureProperties.fromEnvironment()
            ).prepareChatRoom();

            try (CacheStampedeDataProbe dataProbe = new CacheStampedeDataProbe(properties)) {
                long afterSequence = dataProbe.prepareMessages(
                    context.roomId(),
                    testRunId,
                    properties.fixtureMessageCount()
                );
                try {
                    PhaseResult before = executePhase(
                        dataProbe,
                        metricProbe,
                        context,
                        beforeUrl,
                        afterSequence,
                        properties.requestCount()
                    );

                    serverController.start(
                        true,
                        AFTER_LOAD_BALANCER_PORT,
                        DATABASE_LOAD_DELAY
                    );
                    PhaseResult after = executePhase(
                        dataProbe,
                        metricProbe,
                        context,
                        afterUrl,
                        afterSequence,
                        properties.requestCount()
                    );

                    printResult(properties.requestCount(), before, after);

                    assertThat(before.loadResult().successCount())
                        .isEqualTo(properties.requestCount());
                    assertThat(after.loadResult().successCount())
                        .isEqualTo(properties.requestCount());
                    assertThat(before.databaseLoads()).isGreaterThan(1L);
                    assertThat(after.databaseLoads()).isEqualTo(1L);
                    assertThat(after.databaseLoads()).isLessThan(before.databaseLoads());
                } finally {
                    dataProbe.deleteMessages(testRunId);
                    dataProbe.clearMessageCache(context.roomId());
                }
            }
        } finally {
            serverController.start(true, DEFAULT_LOAD_BALANCER_PORT, Duration.ZERO);
        }
    }

    private PhaseResult executePhase(
        CacheStampedeDataProbe dataProbe,
        ChatMessageQueryMetricProbe metricProbe,
        ChatTestContext context,
        URI loadBalancerUrl,
        long afterSequence,
        int requestCount
    ) {
        dataProbe.clearMessageCache(context.roomId());
        long loadsBefore = metricProbe.totalDatabaseLoads();
        Result loadResult;
        try (CacheStampedeLoadClient loadClient = new CacheStampedeLoadClient()) {
            loadResult = loadClient.execute(
                loadBalancerUrl,
                context.roomId(),
                afterSequence,
                requestCount
            );
        }
        long databaseLoads = metricProbe.totalDatabaseLoads() - loadsBefore;
        return new PhaseResult(loadResult, databaseLoads);
    }

    private ChatTestProperties testUser(URI httpUrl) {
        return new ChatTestProperties(
            httpUrl,
            URI.create(httpUrl.toString().replace("http://", "ws://") + "/stomp-chat"),
            "chat_cache_stampede",
            "CacheStampede2026",
            "cache-stampede-client",
            "CHAT_TEST",
            Duration.ofSeconds(30L)
        );
    }

    private URI loadBalancerUrl(int port) {
        return URI.create("http://localhost:" + port);
    }

    private void printResult(int requestCount, PhaseResult before, PhaseResult after) {
        System.out.println("[캐시 스탬피드 테스트 결과]");
        System.out.println();
        System.out.println("동시 요청 수: " + requestCount + "건");
        System.out.println();
        printPhase("분산 락 미적용", before);
        System.out.println();
        printPhase("분산 락 적용", after);
        System.out.println();
        double reductionRate = before.databaseLoads() == 0L
            ? 0.0
            : (1.0 - (double) after.databaseLoads() / before.databaseLoads()) * 100.0;
        System.out.printf("DB 조회 감소율: %.2f%%%n", reductionRate);
        System.out.println(
            "최종 결과: "
                + (after.databaseLoads() < before.databaseLoads()
                    ? "캐시 스탬피드 방지 성공"
                    : "캐시 스탬피드 방지 실패")
        );
    }

    private void printPhase(String name, PhaseResult phase) {
        Result result = phase.loadResult();
        System.out.println("[" + name + "]");
        System.out.println("요청 성공: " + result.successCount() + "/" + result.requestCount() + "건");
        System.out.println("DB 조회 횟수: " + phase.databaseLoads() + "회");
        System.out.printf("평균 응답시간: %.2fms%n", result.averageMillis());
        System.out.println("p95 응답시간: " + result.p95Millis() + "ms");
        System.out.println("p99 응답시간: " + result.p99Millis() + "ms");
        if (result.firstFailure() != null) {
            System.out.println(
                "첫 실패 원인: " + result.firstFailure().getClass().getSimpleName()
                    + " - " + result.firstFailure().getMessage()
            );
        }
    }

    private record PhaseResult(Result loadResult, long databaseLoads) {
    }
}
