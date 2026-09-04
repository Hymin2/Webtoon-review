package com.hymin.chattest.scenario;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.compoundIndex;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class ChatMessageCommandTransactionPerformanceScenarioTest {

    private static final String DATABASE = "webtoon_review_task004_benchmark";
    private static final TransactionOptions TRANSACTION_OPTIONS = TransactionOptions.builder()
        .readConcern(ReadConcern.SNAPSHOT)
        .writeConcern(WriteConcern.MAJORITY)
        .build();

    @Test
    @Tag("message-command-transaction-performance")
    @DisplayName("Message+Outbox와 RoomSequence+Message+Outbox transaction을 비교한다")
    void transactionBoundaryABComparison() throws Exception {
        BenchmarkProperties properties = BenchmarkProperties.load();
        List<RunResult> results = new ArrayList<>();

        try (MongoClient client = MongoClients.create(properties.mongodbUri())) {
            BenchmarkHarness harness = new BenchmarkHarness(client, properties);
            harness.run(Variant.A, Pattern.HOT_ROOM, properties.warmupRequests(), true);
            harness.run(Variant.B, Pattern.HOT_ROOM, properties.warmupRequests(), true);
            harness.run(Variant.A, Pattern.DISTRIBUTED_ROOMS, properties.warmupRequests(), true);
            harness.run(Variant.B, Pattern.DISTRIBUTED_ROOMS, properties.warmupRequests(), true);

            for (int repeat = 0; repeat < properties.repeats(); repeat++) {
                for (Pattern pattern : Pattern.values()) {
                    if (repeat % 2 == 0) {
                        results.add(harness.run(Variant.A, pattern, properties.requests(), false));
                        results.add(harness.run(Variant.B, pattern, properties.requests(), false));
                    } else {
                        results.add(harness.run(Variant.B, pattern, properties.requests(), false));
                        results.add(harness.run(Variant.A, pattern, properties.requests(), false));
                    }
                }
            }
        }

        printResults(results, properties);
        assertThat(results).allSatisfy(result -> {
            assertThat(result.committed() + result.errors()).isEqualTo(result.requests());
            assertThat(result.committed()).isGreaterThan(0);
        });
    }

    @Test
    @Tag("message-command-max-attempts-performance")
    @EnabledIfEnvironmentVariable(named = "TASK004_MAX_ATTEMPTS_BENCHMARK", matches = "true")
    @DisplayName("production maxAttempts=5 hot-room retry exhaustion을 측정한다")
    void productionMaxAttemptsHotRoomComparison() throws Exception {
        List<Integer> requestCounts = requestCounts();
        List<RunResult> results = new ArrayList<>();
        BenchmarkProperties properties = BenchmarkProperties.loadProductionMaxAttempts();

        try (MongoClient client = MongoClients.create(properties.mongodbUri())) {
            BenchmarkHarness harness = new BenchmarkHarness(client, properties);
            harness.run(Variant.B, Pattern.HOT_ROOM, properties.warmupRequests(), true);
            for (int requestCount : requestCounts) {
                for (int repeat = 0; repeat < properties.repeats(); repeat++) {
                    results.add(harness.run(
                        Variant.B,
                        Pattern.HOT_ROOM,
                        requestCount,
                        false
                    ));
                }
            }
        }

        printProductionMaxAttemptsResults(results, properties, requestCounts);
        assertThat(results).allSatisfy(result ->
            assertThat(result.committed() + result.errors()).isEqualTo(result.requests())
        );
    }

    private void printResults(List<RunResult> results, BenchmarkProperties properties) {
        System.out.printf(
            "[TASK-004 transaction A/B] requests=%d concurrency=%d repeats=%d warmup=%d rooms=%d%n",
            properties.requests(),
            properties.concurrency(),
            properties.repeats(),
            properties.warmupRequests(),
            properties.distributedRooms()
        );
        for (RunResult result : results) {
            System.out.printf(
                Locale.US,
                "%s %-18s throughput=%8.1f/s p50=%7.2fms p95=%7.2fms p99=%7.2fms "
                    + "error=%5.2f%% conflicts=%d retries=%d exhaustion=%d%n",
                result.variant(),
                result.pattern(),
                result.throughput(),
                result.p50Millis(),
                result.p95Millis(),
                result.p99Millis(),
                result.errorRate() * 100.0,
                result.conflicts(),
                result.retries(),
                result.exhaustions()
            );
        }
        System.out.println("[median and throughput range]");
        for (Pattern pattern : Pattern.values()) {
            for (Variant variant : Variant.values()) {
                List<RunResult> group = results.stream()
                    .filter(result -> result.pattern() == pattern && result.variant() == variant)
                    .sorted(Comparator.comparingDouble(RunResult::throughput))
                    .toList();
                RunResult median = group.get(group.size() / 2);
                System.out.printf(
                    Locale.US,
                    "%s %-18s median throughput=%8.1f/s range=%8.1f..%8.1f/s "
                        + "p50/p95/p99=%6.2f/%6.2f/%6.2fms error=%5.2f%% conflicts=%d "
                        + "retries=%d exhaustion=%d%n",
                    variant,
                    pattern,
                    median.throughput(),
                    group.get(0).throughput(),
                    group.get(group.size() - 1).throughput(),
                    median.p50Millis(),
                    median.p95Millis(),
                    median.p99Millis(),
                    median.errorRate() * 100.0,
                    median.conflicts(),
                    median.retries(),
                    median.exhaustions()
                );
            }
        }
    }

    private void printProductionMaxAttemptsResults(
        List<RunResult> results,
        BenchmarkProperties properties,
        List<Integer> requestCounts
    ) {
        System.out.printf(
            "[TASK-004 production maxAttempts] concurrency=%d repeats=%d warmup=%d "
                + "maxAttempts=%d%n",
            properties.concurrency(),
            properties.repeats(),
            properties.warmupRequests(),
            properties.maxAttempts()
        );
        for (RunResult result : results) {
            System.out.printf(
                Locale.US,
                "requests=%4d success=%4d failure=%4d throughput=%8.1f/s "
                    + "p50/p95/p99=%6.2f/%6.2f/%6.2fms error=%5.2f%% "
                    + "conflicts=%d retries=%d exhaustion=%d%n",
                result.requests(),
                result.committed(),
                result.errors(),
                result.throughput(),
                result.p50Millis(),
                result.p95Millis(),
                result.p99Millis(),
                result.errorRate() * 100.0,
                result.conflicts(),
                result.retries(),
                result.exhaustions()
            );
        }
        System.out.println("| Requests | Success | Failure | Error Rate | Throughput | p50 | p95 | p99 | Conflict | Retry | Exhaustion |");
        System.out.println("| -------: | ------: | ------: | ---------: | ---------: | --: | --: | --: | -------: | ----: | ---------: |");
        for (int requestCount : requestCounts) {
            List<RunResult> group = results.stream()
                .filter(result -> result.requests() == requestCount)
                .sorted(Comparator.comparingDouble(RunResult::throughput))
                .toList();
            RunResult median = group.get(group.size() / 2);
            System.out.printf(
                Locale.US,
                "| %d | %d | %d | %.2f%% | %.1f/s | %.2fms | %.2fms | %.2fms | %d | %d | %d |%n",
                median.requests(),
                median.committed(),
                median.errors(),
                median.errorRate() * 100.0,
                median.throughput(),
                median.p50Millis(),
                median.p95Millis(),
                median.p99Millis(),
                median.conflicts(),
                median.retries(),
                median.exhaustions()
            );
        }
    }

    private List<Integer> requestCounts() {
        return List.of(System.getProperty(
                "chat.message-command.max-attempts.request-counts",
                "100,500,1000"
            ).split(","))
            .stream()
            .map(String::trim)
            .map(Integer::parseInt)
            .toList();
    }

    private enum Variant {
        A,
        B
    }

    private enum Pattern {
        HOT_ROOM,
        DISTRIBUTED_ROOMS
    }

    private record BenchmarkProperties(
        String mongodbUri,
        int requests,
        int concurrency,
        int repeats,
        int warmupRequests,
        int distributedRooms,
        int maxAttempts
    ) {

        private static BenchmarkProperties load() {
            return new BenchmarkProperties(
                System.getProperty(
                    "task004.mongodb.uri",
                    "mongodb://localhost:27017/" + DATABASE
                        + "?replicaSet=rs0&directConnection=true"
                ),
                integer("chat.message-command.performance.requests", 500),
                integer("chat.message-command.performance.concurrency", 8),
                integer("chat.message-command.performance.repeats", 3),
                integer("chat.message-command.performance.warmup-requests", 100),
                integer("chat.message-command.performance.distributed-rooms", 50),
                integer("chat.message-command.performance.max-attempts", 50)
            );
        }

        private static BenchmarkProperties loadProductionMaxAttempts() {
            return new BenchmarkProperties(
                System.getProperty(
                    "task004.mongodb.uri",
                    "mongodb://localhost:27017/" + DATABASE
                        + "?replicaSet=rs0&directConnection=true"
                ),
                0,
                integer("chat.message-command.max-attempts.concurrency", 8),
                integer("chat.message-command.max-attempts.repeats", 3),
                integer("chat.message-command.max-attempts.warmup-requests", 100),
                1,
                5
            );
        }

        private static int integer(String key, int defaultValue) {
            return Integer.parseInt(System.getProperty(key, Integer.toString(defaultValue)));
        }
    }

    private static final class BenchmarkHarness {

        private final MongoClient client;
        private final BenchmarkProperties properties;

        private BenchmarkHarness(MongoClient client, BenchmarkProperties properties) {
            this.client = client;
            this.properties = properties;
        }

        private RunResult run(
            Variant variant,
            Pattern pattern,
            int requestCount,
            boolean warmup
        ) throws Exception {
            MongoDatabase database = prepareDatabase();
            MongoCollection<Document> messages = database.getCollection("chat_messages");
            MongoCollection<Document> outbox = database.getCollection("chat_message_outbox");
            MongoCollection<Document> sequences = database.getCollection("room_sequence");
            ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
            AtomicInteger committed = new AtomicInteger();
            AtomicInteger errors = new AtomicInteger();
            AtomicInteger conflicts = new AtomicInteger();
            AtomicInteger retries = new AtomicInteger();
            AtomicInteger exhaustions = new AtomicInteger();
            String runId = (warmup ? "warmup-" : "measure-") + UUID.randomUUID();

            long startedAt = System.nanoTime();
            try (ExecutorService executor = Executors.newFixedThreadPool(properties.concurrency())) {
                List<Future<?>> futures = new ArrayList<>();
                for (int index = 0; index < requestCount; index++) {
                    int requestIndex = index;
                    futures.add(executor.submit(() -> executeRequest(
                        variant,
                        pattern,
                        runId,
                        requestIndex,
                        messages,
                        outbox,
                        sequences,
                        latencies,
                        committed,
                        errors,
                        conflicts,
                        retries,
                        exhaustions
                    )));
                }
                for (Future<?> future : futures) {
                    future.get(2, TimeUnit.MINUTES);
                }
            }
            long elapsedNanos = System.nanoTime() - startedAt;
            return RunResult.create(
                variant,
                pattern,
                requestCount,
                committed.get(),
                errors.get(),
                conflicts.get(),
                retries.get(),
                exhaustions.get(),
                elapsedNanos,
                new ArrayList<>(latencies)
            );
        }

        private MongoDatabase prepareDatabase() {
            MongoDatabase database = client.getDatabase(DATABASE);
            database.drop();
            database.createCollection("chat_messages");
            database.createCollection("chat_message_outbox");
            database.createCollection("room_sequence");
            database.getCollection("chat_messages").createIndex(
                compoundIndex(ascending("roomId"), ascending("senderId"), ascending("clientMessageId")),
                new IndexOptions().name("room_sender_client_message_unique_idx").unique(true)
            );
            database.getCollection("chat_messages").createIndex(
                compoundIndex(ascending("roomId"), ascending("messageSequence")),
                new IndexOptions()
                    .name("room_message_sequence_unique_idx")
                    .unique(true)
                    .partialFilterExpression(new Document(
                        "messageSequence",
                        new Document("$type", "number")
                    ))
            );
            database.getCollection("chat_message_outbox").createIndex(
                ascending("messageId"),
                new IndexOptions().name("message_id_unique_idx").unique(true)
            );
            return database;
        }

        private void executeRequest(
            Variant variant,
            Pattern pattern,
            String runId,
            int requestIndex,
            MongoCollection<Document> messages,
            MongoCollection<Document> outbox,
            MongoCollection<Document> sequences,
            ConcurrentLinkedQueue<Long> latencies,
            AtomicInteger committed,
            AtomicInteger errors,
            AtomicInteger conflicts,
            AtomicInteger retries,
            AtomicInteger exhaustions
        ) {
            long roomIndex = pattern == Pattern.HOT_ROOM
                ? 0L
                : requestIndex % properties.distributedRooms();
            long roomId = 1_000_000L + roomIndex;
            long assignedSequence = pattern == Pattern.HOT_ROOM
                ? requestIndex + 1L
                : requestIndex / properties.distributedRooms() + 1L;
            String messageId = runId + "-message-" + requestIndex;
            String eventId = runId + "-event-" + requestIndex;
            String clientMessageId = "client-" + requestIndex;
            String createdAt = Instant.now().toString();
            long startedAt = System.nanoTime();

            for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
                try (ClientSession session = client.startSession()) {
                    session.startTransaction(TRANSACTION_OPTIONS);
                    try {
                        long roomSequence = assignedSequence;
                        if (variant == Variant.B) {
                            Document counter = sequences.findOneAndUpdate(
                                session,
                                eq("_id", roomId),
                                Updates.inc("sequence", 1L),
                                new FindOneAndUpdateOptions()
                                    .upsert(true)
                                    .returnDocument(ReturnDocument.AFTER)
                            );
                            roomSequence = counter.getLong("sequence");
                        }
                        Document message = message(
                            messageId,
                            roomId,
                            roomSequence,
                            clientMessageId,
                            createdAt
                        );
                        messages.insertOne(session, message);
                        outbox.insertOne(session, outbox(
                            eventId,
                            messageId,
                            roomId,
                            roomSequence,
                            clientMessageId,
                            createdAt
                        ));
                        session.commitTransaction();
                        committed.incrementAndGet();
                        latencies.add(System.nanoTime() - startedAt);
                        return;
                    } catch (MongoException exception) {
                        if (session.hasActiveTransaction()) {
                            try {
                                session.abortTransaction();
                            } catch (MongoException ignored) {
                                // The original transaction error is the benchmark result.
                            }
                        }
                        if (exception.hasErrorLabel("TransientTransactionError")) {
                            conflicts.incrementAndGet();
                            if (attempt < properties.maxAttempts()) {
                                retries.incrementAndGet();
                                continue;
                            }
                            exhaustions.incrementAndGet();
                        }
                        errors.incrementAndGet();
                        latencies.add(System.nanoTime() - startedAt);
                        return;
                    }
                }
            }
            errors.incrementAndGet();
            latencies.add(System.nanoTime() - startedAt);
        }

        private Document message(
            String messageId,
            long roomId,
            long roomSequence,
            String clientMessageId,
            String createdAt
        ) {
            return new Document("_id", messageId)
                .append("roomId", roomId)
                .append("senderId", 7L)
                .append("roomMemberId", "benchmark-member")
                .append("messageSequence", roomSequence)
                .append("clientMessageId", clientMessageId)
                .append("sender", "benchmark-sender")
                .append("createdAt", createdAt)
                .append("messageBlocks", List.of(new Document("type", "TEXT")
                    .append("content", "TASK-004 transaction benchmark")
                    .append("metadata", new Document())));
        }

        private Document outbox(
            String eventId,
            String messageId,
            long roomId,
            long roomSequence,
            String clientMessageId,
            String createdAt
        ) {
            return new Document("_id", eventId)
                .append("eventType", "MessageCreated")
                .append("schemaVersion", 1)
                .append("messageId", messageId)
                .append("roomId", roomId)
                .append("roomSequence", roomSequence)
                .append("senderId", 7L)
                .append("roomMemberId", "benchmark-member")
                .append("clientMessageId", clientMessageId)
                .append("sender", "benchmark-sender")
                .append("messageBlocks", List.of(new Document("type", "TEXT")
                    .append("content", "TASK-004 transaction benchmark")
                    .append("metadata", new Document())))
                .append("messageCreatedAt", createdAt)
                .append("occurredAt", createdAt)
                .append("publishedAt", null);
        }
    }

    private record RunResult(
        Variant variant,
        Pattern pattern,
        int requests,
        int committed,
        int errors,
        int conflicts,
        int retries,
        int exhaustions,
        double throughput,
        double p50Millis,
        double p95Millis,
        double p99Millis
    ) {

        private static RunResult create(
            Variant variant,
            Pattern pattern,
            int requests,
            int committed,
            int errors,
            int conflicts,
            int retries,
            int exhaustions,
            long elapsedNanos,
            List<Long> latencies
        ) {
            latencies.sort(Long::compareTo);
            return new RunResult(
                variant,
                pattern,
                requests,
                committed,
                errors,
                conflicts,
                retries,
                exhaustions,
                committed / (elapsedNanos / 1_000_000_000.0),
                percentileMillis(latencies, 0.50),
                percentileMillis(latencies, 0.95),
                percentileMillis(latencies, 0.99)
            );
        }

        private static double percentileMillis(List<Long> latencies, double percentile) {
            if (latencies.isEmpty()) {
                return 0.0;
            }
            int index = Math.max(
                0,
                (int) Math.ceil(percentile * latencies.size()) - 1
            );
            return latencies.get(index) / 1_000_000.0;
        }

        private double errorRate() {
            return requests == 0 ? 0.0 : (double) errors / requests;
        }
    }
}
