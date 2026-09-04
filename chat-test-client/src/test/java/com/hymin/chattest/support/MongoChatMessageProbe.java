package com.hymin.chattest.support;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.conversions.Bson;

public final class MongoChatMessageProbe implements AutoCloseable {

    private static final String COLLECTION_NAME = "chat_messages";

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public static MongoChatMessageProbe fromEnvironment() {
        String host = environment("CHAT_TEST_MONGODB_HOST", "localhost");
        int port = Integer.parseInt(environment("CHAT_TEST_MONGODB_PORT", "27017"));
        String database = environment("CHAT_TEST_MONGODB_DATABASE", "test");
        return new MongoChatMessageProbe(host, port, database);
    }

    private MongoChatMessageProbe(String host, int port, String database) {
        mongoClient = MongoClients.create("mongodb://%s:%d".formatted(host, port));
        collection = mongoClient.getDatabase(database).getCollection(COLLECTION_NAME);
    }

    public Document awaitMessage(long roomId, String clientMessageId, Duration timeout) {
        Bson filter = messageFilter(roomId, clientMessageId);
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            Document message = collection.find(filter).first();
            if (message != null) {
                return message;
            }
            waitForNextQuery();
        }
        throw new IllegalStateException(
            "MongoDB에 테스트 메시지가 저장되지 않았습니다: clientMessageId=" + clientMessageId
        );
    }

    public long countMessages(long roomId, String clientMessageId) {
        return collection.countDocuments(messageFilter(roomId, clientMessageId));
    }

    private Bson messageFilter(long roomId, String clientMessageId) {
        return new Document("roomId", roomId)
            .append("clientMessageId", clientMessageId);
    }

    private void waitForNextQuery() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("MongoDB 저장 확인 대기가 중단되었습니다.", exception);
        }
    }

    private static String environment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @Override
    public void close() {
        mongoClient.close();
    }
}
