package com.hymin.chattest.support;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public final class CacheStampedeDataProbe implements AutoCloseable {

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> redisConnection;
    private final RedisCommands<String, String> redisCommands;

    public CacheStampedeDataProbe(CacheStampedeTestProperties properties) {
        mongoClient = MongoClients.create(
            "mongodb://%s:%d".formatted(properties.mongoHost(), properties.mongoPort())
        );
        collection = mongoClient.getDatabase(properties.mongoDatabase())
            .getCollection("chat_messages");

        RedisURI.Builder redisUri = RedisURI.Builder.redis(
            properties.redisHost(),
            properties.redisPort()
        );
        if (!properties.redisPassword().isBlank()) {
            redisUri.withPassword(properties.redisPassword().toCharArray());
        }
        redisClient = RedisClient.create(redisUri.build());
        redisConnection = redisClient.connect();
        redisCommands = redisConnection.sync();
    }

    public long prepareMessages(long roomId, String testRunId, int messageCount) {
        Document latest = collection.find(Filters.eq("roomId", roomId))
            .sort(Sorts.descending("messageSequence"))
            .first();
        long afterSequence = latest == null || latest.get("messageSequence") == null
            ? 0L
            : ((Number) latest.get("messageSequence")).longValue();

        List<Document> messages = new ArrayList<>(messageCount);
        for (int index = 1; index <= messageCount; index++) {
            long sequence = afterSequence + index;
            messages.add(new Document("_id", testRunId + "-" + index)
                .append("roomId", roomId)
                .append("senderId", 1L)
                .append("roomMemberId", "cache-stampede-member")
                .append("messageSequence", sequence)
                .append("clientMessageId", testRunId + "-client-" + index)
                .append("sender", "캐시 스탬피드 테스트")
                .append("createdAt", "2026-01-01T00:00:00")
                .append("messageBlocks", List.of(new Document("type", "TEXT")
                    .append("content", "캐시 스탬피드 테스트 메시지")
                    .append("metadata", new Document("testRunId", testRunId)))));
        }
        collection.insertMany(messages);
        return afterSequence;
    }

    public void clearMessageCache(long roomId) {
        redisCommands.del(
            "chat:room:" + roomId + ":recent-messages",
            "chat:room:" + roomId + ":recent-message-contents",
            "chat:room:" + roomId + ":message-cache:lock"
        );
    }

    public void deleteMessages(String testRunId) {
        collection.deleteMany(Filters.regex("_id", "^" + testRunId + "-"));
    }

    @Override
    public void close() {
        redisConnection.close();
        redisClient.shutdown();
        mongoClient.close();
    }
}
