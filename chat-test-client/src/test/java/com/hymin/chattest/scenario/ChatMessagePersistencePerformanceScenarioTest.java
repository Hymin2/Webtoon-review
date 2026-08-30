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

@Tag("persistence-performance")
class ChatMessagePersistencePerformanceScenarioTest {

    private static final Duration POLL_INTERVAL = Duration.ofMillis(50);
    private static final Duration PROCESS_TIMEOUT = Duration.ofHours(4);

    @Test
    @DisplayName("일반 Insert와 100건 및 1000건 Bulk Insert의 저장 성능을 비교한다")
    void 일반_Insert와_두_Bulk_Insert의_저장_성능을_비교한다() {
        PersistencePerformanceProperties properties =
            PersistencePerformanceProperties.fromEnvironment();
        DockerChatPersisterController.verifyImage(properties);

        String scenarioId = UUID.randomUUID().toString().substring(0, 8);
        String singleStream = "chat-message-persistence-performance:single:" + scenarioId;
        String bulk100Stream = "chat-message-persistence-performance:bulk-100:" + scenarioId;
        String bulk1000Stream = "chat-message-persistence-performance:bulk-1000:" + scenarioId;
        String singleGroup = "persistence-performance-single-" + scenarioId;
        String bulk100Group = "persistence-performance-bulk-100-" + scenarioId;
        String bulk1000Group = "persistence-performance-bulk-1000-" + scenarioId;
        List<ComparisonResult> results = new ArrayList<>();

        try (
            RedisPersistencePerformanceProbe redis =
                new RedisPersistencePerformanceProbe(properties);
            MongoPersistencePerformanceProbe mongo =
                new MongoPersistencePerformanceProbe(properties);
            DockerChatPersisterController singlePersister =
                new DockerChatPersisterController(
                    properties,
                    "single",
                    "single",
                    100,
                    singleStream,
                    singleGroup
                );
            DockerChatPersisterController bulk100Persister =
                new DockerChatPersisterController(
                    properties,
                    "bulk-100",
                    "bulk",
                    100,
                    bulk100Stream,
                    bulk100Group
                );
            DockerChatPersisterController bulk1000Persister =
                new DockerChatPersisterController(
                    properties,
                    "bulk-1000",
                    "bulk",
                    1000,
                    bulk1000Stream,
                    bulk1000Group
                )
        ) {
            singlePersister.pause();
            bulk100Persister.pause();
            bulk1000Persister.pause();

            try {
                for (int messageCount : properties.messageCounts()) {
                    PersistenceResult singleResult = runPersistence(
                        "single",
                        messageCount,
                        singleStream,
                        singleGroup,
                        singlePersister,
                        redis,
                        mongo
                    );
                    PersistenceResult bulk100Result = runPersistence(
                        "bulk-100",
                        messageCount,
                        bulk100Stream,
                        bulk100Group,
                        bulk100Persister,
                        redis,
                        mongo
                    );
                    PersistenceResult bulk1000Result = runPersistence(
                        "bulk-1000",
                        messageCount,
                        bulk1000Stream,
                        bulk1000Group,
                        bulk1000Persister,
                        redis,
                        mongo
                    );
                    results.add(new ComparisonResult(
                        singleResult,
                        bulk100Result,
                        bulk1000Result
                    ));
                }
            } finally {
                redis.delete(singleStream, bulk100Stream, bulk1000Stream);
            }
        }

        printResults(results);
        for (ComparisonResult result : results) {
            assertThat(result.single().persistedCount()).isEqualTo(result.single().messageCount());
            assertThat(result.bulk100().persistedCount()).isEqualTo(result.bulk100().messageCount());
            assertThat(result.bulk1000().persistedCount())
                .isEqualTo(result.bulk1000().messageCount());
            assertThat(result.single().pendingCount()).isZero();
            assertThat(result.bulk100().pendingCount()).isZero();
            assertThat(result.bulk1000().pendingCount()).isZero();
        }
    }

    private PersistenceResult runPersistence(
        String mode,
        int messageCount,
        String streamKey,
        String groupName,
        DockerChatPersisterController persister,
        RedisPersistencePerformanceProbe redis,
        MongoPersistencePerformanceProbe mongo
    ) {
        String testRunId = "persistence-" + mode + "-" + UUID.randomUUID();
        long roomId = Math.floorMod(UUID.randomUUID().getMostSignificantBits(), Long.MAX_VALUE);
        mongo.delete(roomId);
        redis.appendMessages(streamKey, mode, testRunId, roomId, messageCount);

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

        long batchCount = after.batchCount() - before.batchCount();
        long processedMessageCount = after.messageCount() - before.messageCount();
        return new PersistenceResult(
            mode,
            messageCount,
            elapsedNanos,
            completion.persistedCount(),
            completion.pendingCount(),
            batchCount,
            processedMessageCount
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
            "메시지 저장 시간이 초과됐습니다: expected=" + expectedCount
                + ", persisted=" + persistedCount
                + ", pending=" + pendingCount
        );
    }

    private void printResults(List<ComparisonResult> results) {
        System.out.println("[메시지 저장 성능 비교 결과]");
        System.out.println();

        for (ComparisonResult comparison : results) {
            PersistenceResult single = comparison.single();
            PersistenceResult bulk100 = comparison.bulk100();
            PersistenceResult bulk1000 = comparison.bulk1000();
            System.out.printf("메시지 수: %,d건%n", single.messageCount());
            System.out.println();
            printResult("일반 Insert", single);
            System.out.println();
            printResult("Bulk Insert (100건)", bulk100);
            System.out.println();
            printResult("Bulk Insert (1000건)", bulk1000);
            System.out.println();
            System.out.printf(
                Locale.US,
                "Bulk Insert (100건) 성능 향상: %.2f배%n",
                single.elapsedSeconds() / bulk100.elapsedSeconds()
            );
            System.out.printf(
                Locale.US,
                "Bulk Insert (1000건) 성능 향상: %.2f배%n",
                single.elapsedSeconds() / bulk1000.elapsedSeconds()
            );
            System.out.printf(
                Locale.US,
                "Bulk 1000건/100건 처리 속도 비율: %.2f배%n",
                bulk100.elapsedSeconds() / bulk1000.elapsedSeconds()
            );
            System.out.println(
                "저장 건수 및 ACK 검증: "
                    + (single.isComplete()
                    && bulk100.isComplete()
                    && bulk1000.isComplete() ? "성공" : "실패")
            );
            System.out.println();
        }
    }

    private void printResult(String title, PersistenceResult result) {
        System.out.println(title);
        System.out.printf(Locale.US, "- 처리 시간: %.2f초%n", result.elapsedSeconds());
        System.out.printf(Locale.US, "- 처리량: %,.0f건/초%n", result.throughput());
        System.out.printf(Locale.US, "- 평균 배치 크기: %.2f건%n", result.averageBatchSize());
        System.out.printf("- MongoDB 저장 건수: %,d건%n", result.persistedCount());
        System.out.printf("- Redis Pending: %,d건%n", result.pendingCount());
        System.out.println("- Redis Lag: " + (result.isComplete() ? "0건" : "확인 필요"));
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

    private record ComparisonResult(
        PersistenceResult single,
        PersistenceResult bulk100,
        PersistenceResult bulk1000
    ) {
    }

    private record PersistenceResult(
        String mode,
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
