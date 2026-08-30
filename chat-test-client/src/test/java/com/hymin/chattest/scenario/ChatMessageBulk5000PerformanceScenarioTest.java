package com.hymin.chattest.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.chattest.support.DockerChatPersisterController;
import com.hymin.chattest.support.DockerChatPersisterController.BatchStatistics;
import com.hymin.chattest.support.MongoPersistencePerformanceProbe;
import com.hymin.chattest.support.PersistencePerformanceProperties;
import com.hymin.chattest.support.RedisPersistencePerformanceProbe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("persistence-bulk-5000")
class ChatMessageBulk5000PerformanceScenarioTest {

    private static final int BATCH_SIZE = 5_000;
    private static final Duration POLL_INTERVAL = Duration.ofMillis(50);
    private static final Duration PROCESS_TIMEOUT = Duration.ofHours(1);

    @Test
    @DisplayName("5000건 Bulk Insert의 메시지 저장 성능을 측정한다")
    void 오천_건_Bulk_Insert의_메시지_저장_성능을_측정한다() {
        PersistencePerformanceProperties properties =
            PersistencePerformanceProperties.fromEnvironment();
        DockerChatPersisterController.verifyImage(properties);

        String scenarioId = UUID.randomUUID().toString().substring(0, 8);
        String streamKey = "chat-message-persistence-performance:bulk-5000:" + scenarioId;
        String groupName = "persistence-performance-bulk-5000-" + scenarioId;
        List<PersistenceResult> results = new ArrayList<>();

        try (
            RedisPersistencePerformanceProbe redis =
                new RedisPersistencePerformanceProbe(properties);
            MongoPersistencePerformanceProbe mongo =
                new MongoPersistencePerformanceProbe(properties);
            DockerChatPersisterController persister =
                new DockerChatPersisterController(
                    properties,
                    "bulk-5000",
                    "bulk",
                    BATCH_SIZE,
                    streamKey,
                    groupName
                )
        ) {
            persister.pause();
            try {
                for (int messageCount : properties.messageCounts()) {
                    results.add(runPersistence(
                        messageCount,
                        streamKey,
                        groupName,
                        persister,
                        redis,
                        mongo
                    ));
                }
            } finally {
                redis.delete(streamKey);
            }
        }

        printResults(results);
        for (PersistenceResult result : results) {
            assertThat(result.persistedCount()).isEqualTo(result.messageCount());
            assertThat(result.pendingCount()).isZero();
        }
    }

    private PersistenceResult runPersistence(
        int messageCount,
        String streamKey,
        String groupName,
        DockerChatPersisterController persister,
        RedisPersistencePerformanceProbe redis,
        MongoPersistencePerformanceProbe mongo
    ) {
        String testRunId = "persistence-bulk-5000-" + UUID.randomUUID();
        long roomId = Math.floorMod(UUID.randomUUID().getMostSignificantBits(), Long.MAX_VALUE);
        mongo.delete(roomId);
        redis.appendMessages(streamKey, "bulk-5000", testRunId, roomId, messageCount);

        BatchStatistics before = persister.batchStatistics();
        long startedAt = System.nanoTime();
        persister.unpause();
        CompletionState completion = awaitCompletion(
            messageCount,
            roomId,
            streamKey,
            groupName,
            mongo,
            redis
        );
        long elapsedNanos = System.nanoTime() - startedAt;
        persister.pause();
        BatchStatistics after = persister.batchStatistics();

        mongo.delete(roomId);
        redis.trim(streamKey);

        return new PersistenceResult(
            messageCount,
            elapsedNanos,
            completion.persistedCount(),
            completion.pendingCount(),
            after.batchCount() - before.batchCount(),
            after.messageCount() - before.messageCount()
        );
    }

    private CompletionState awaitCompletion(
        int expectedCount,
        long roomId,
        String streamKey,
        String groupName,
        MongoPersistencePerformanceProbe mongo,
        RedisPersistencePerformanceProbe redis
    ) {
        long deadline = System.nanoTime() + PROCESS_TIMEOUT.toNanos();
        long persistedCount = 0;
        long pendingCount = 0;

        while (System.nanoTime() < deadline) {
            persistedCount = mongo.count(roomId);
            pendingCount = redis.pendingCount(streamKey, groupName);
            if (persistedCount == expectedCount && pendingCount == 0) {
                return new CompletionState(persistedCount, pendingCount);
            }
            sleep(POLL_INTERVAL);
        }
        throw new IllegalStateException(
            "Bulk 5000건 저장 시간이 초과됐습니다: expected=" + expectedCount
                + ", persisted=" + persistedCount
                + ", pending=" + pendingCount
        );
    }

    private void printResults(List<PersistenceResult> results) {
        System.out.println("[Bulk Insert 5000건 성능 테스트 결과]");
        System.out.println();
        for (PersistenceResult result : results) {
            System.out.printf("메시지 수: %,d건%n", result.messageCount());
            System.out.printf(Locale.US, "처리 시간: %.2f초%n", result.elapsedSeconds());
            System.out.printf(Locale.US, "처리량: %,.0f건/초%n", result.throughput());
            System.out.printf(Locale.US, "평균 배치 크기: %.2f건%n", result.averageBatchSize());
            System.out.printf("MongoDB 저장 건수: %,d건%n", result.persistedCount());
            System.out.printf("Redis Pending: %,d건%n", result.pendingCount());
            System.out.println("Redis Lag: " + (result.isComplete() ? "0건" : "확인 필요"));
            System.out.println("저장 건수 및 ACK 검증: " + (result.isComplete() ? "성공" : "실패"));
            System.out.println();
        }
    }

    private void sleep(Duration duration) {
        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("저장 완료 확인이 중단되었습니다.", exception);
        }
    }

    private record CompletionState(long persistedCount, long pendingCount) {
    }

    private record PersistenceResult(
        int messageCount,
        long elapsedNanos,
        long persistedCount,
        long pendingCount,
        long batchCount,
        long processedMessageCount
    ) {

        double elapsedSeconds() {
            return elapsedNanos / 1_000_000_000.0;
        }

        double throughput() {
            return messageCount / elapsedSeconds();
        }

        double averageBatchSize() {
            return batchCount == 0 ? 0.0 : (double) processedMessageCount / batchCount;
        }

        boolean isComplete() {
            return persistedCount == messageCount && pendingCount == 0;
        }
    }
}
