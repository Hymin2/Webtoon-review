package com.hymin.chattest.support;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.models.stream.PendingMessages;
import io.lettuce.core.LettuceFutures;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RedisPersistencePerformanceProbe implements AutoCloseable {

    private static final int PIPELINE_SIZE = 1_000;
    private static final Duration PIPELINE_TIMEOUT = Duration.ofMinutes(2);

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;

    public RedisPersistencePerformanceProbe(PersistencePerformanceProperties properties) {
        RedisURI.Builder uri = RedisURI.Builder.redis(
            properties.redisHost(),
            properties.redisPort()
        );
        if (!properties.redisPassword().isBlank()) {
            uri.withPassword(properties.redisPassword().toCharArray());
        }
        redisClient = RedisClient.create(uri.build());
        connection = redisClient.connect();
        commands = connection.sync();
    }

    public void appendMessages(
        String streamKey,
        String mode,
        String testRunId,
        long roomId,
        int messageCount
    ) {
        connection.setAutoFlushCommands(false);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        List<RedisFuture<String>> pendingCommands = new ArrayList<>(PIPELINE_SIZE);

        try {
            for (int index = 1; index <= messageCount; index++) {
                String payload = payload(mode, testRunId, roomId, index);
                pendingCommands.add(asyncCommands.xadd(streamKey, Map.of("payload", payload)));

                if (pendingCommands.size() == PIPELINE_SIZE) {
                    flushAndAwait(pendingCommands);
                }
            }
            flushAndAwait(pendingCommands);
        } finally {
            connection.setAutoFlushCommands(true);
        }
    }

    public long pendingCount(String streamKey, String groupName) {
        PendingMessages pendingMessages = commands.xpending(streamKey, groupName);
        return pendingMessages.getCount();
    }

    public void trim(String streamKey) {
        commands.xtrim(streamKey, 0);
    }

    public void delete(String... streamKeys) {
        commands.del(streamKeys);
    }

    private void flushAndAwait(List<RedisFuture<String>> pendingCommands) {
        if (pendingCommands.isEmpty()) {
            return;
        }
        connection.flushCommands();
        boolean completed = LettuceFutures.awaitAll(
            PIPELINE_TIMEOUT,
            pendingCommands.toArray(new RedisFuture[0])
        );
        if (!completed) {
            throw new IllegalStateException("Redis Stream 메시지 적재 시간이 초과됐습니다.");
        }
        pendingCommands.clear();
    }

    private String payload(
        String mode,
        String testRunId,
        long roomId,
        int sequence
    ) {
        return """
            {"id":"%s-%d","roomId":%d,"senderId":1,"roomMemberId":"performance-member",\
            "messageSequence":%d,"clientMessageId":"%s-message-%d","traceId":"%s",\
            "sender":"성능 테스트","createdAt":"2026-01-01T00:00:00",\
            "messageBlocks":[{"type":"TEXT","content":"%s insert 성능 테스트",\
            "metadata":{"testRunId":"%s"}}]}
            """.formatted(
            testRunId,
            sequence,
            roomId,
            sequence,
            testRunId,
            sequence,
            testRunId,
            mode,
            testRunId
        ).replace("\n", "");
    }

    @Override
    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
