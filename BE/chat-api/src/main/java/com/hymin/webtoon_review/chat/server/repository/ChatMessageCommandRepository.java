package com.hymin.webtoon_review.chat.server.repository;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.entity.MessageCreatedOutbox;
import com.hymin.webtoon_review.chat.common.entity.RoomSequence;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageCommandRepository {

    private final MongoTemplate mongoTemplate;

    public Optional<ChatMessage> findMessage(
        Long roomId,
        Long senderId,
        String clientMessageId
    ) {
        Query query = Query.query(Criteria.where("roomId").is(roomId)
            .and("senderId").is(senderId)
            .and("clientMessageId").is(clientMessageId));
        return Optional.ofNullable(mongoTemplate.findOne(query, ChatMessage.class));
    }

    public boolean outboxExists(String messageId) {
        return mongoTemplate.exists(
            Query.query(Criteria.where("messageId").is(messageId)),
            MessageCreatedOutbox.class
        );
    }

    public long allocateNextRoomSequence(Long roomId) {
        RoomSequence result = mongoTemplate.findAndModify(
            Query.query(Criteria.where("_id").is(roomId)),
            new Update().inc("sequence", 1L),
            FindAndModifyOptions.options().upsert(true).returnNew(true),
            RoomSequence.class
        );
        if (result == null) {
            throw new IllegalStateException("Room sequence allocation returned no result");
        }
        return result.getSequence();
    }

    public ChatMessage insertMessage(ChatMessage message) {
        return mongoTemplate.insert(message);
    }

    public MessageCreatedOutbox insertOutbox(MessageCreatedOutbox outbox) {
        return mongoTemplate.insert(outbox);
    }
}
