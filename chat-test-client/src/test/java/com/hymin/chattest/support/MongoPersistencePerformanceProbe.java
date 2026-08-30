package com.hymin.chattest.support;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public final class MongoPersistencePerformanceProbe implements AutoCloseable {

    private static final String COLLECTION_NAME = "chat_messages";

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public MongoPersistencePerformanceProbe(PersistencePerformanceProperties properties) {
        mongoClient = MongoClients.create(
            "mongodb://%s:%d".formatted(properties.mongoHost(), properties.mongoPort())
        );
        collection = mongoClient.getDatabase(properties.mongoDatabase())
            .getCollection(COLLECTION_NAME);
    }

    public long count(long roomId) {
        return collection.countDocuments(new Document("roomId", roomId));
    }

    public void delete(long roomId) {
        collection.deleteMany(new Document("roomId", roomId));
    }

    @Override
    public void close() {
        mongoClient.close();
    }
}
