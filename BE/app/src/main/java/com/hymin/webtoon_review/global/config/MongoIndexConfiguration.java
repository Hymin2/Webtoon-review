package com.hymin.webtoon_review.global.config;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.entity.MessageCreatedOutbox;
import org.bson.Document;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;

@Configuration
public class MongoIndexConfiguration {

    @Bean
    public ApplicationRunner chatMessageMongoIndexInitializer(MongoTemplate mongoTemplate) {
        return arguments -> {
            mongoTemplate.indexOps(ChatMessage.class).ensureIndex(
                new Index()
                    .on("roomId", Direction.ASC)
                    .on("senderId", Direction.ASC)
                    .on("clientMessageId", Direction.ASC)
                    .named("room_sender_client_message_unique_idx")
                    .unique()
            );
            mongoTemplate.indexOps(ChatMessage.class).ensureIndex(
                new Index()
                    .on("roomId", Direction.ASC)
                    .on("messageSequence", Direction.DESC)
                    .named("room_message_sequence_unique_idx")
                    .unique()
                    .partial(PartialIndexFilter.of(
                        new Document(
                            "messageSequence",
                            new Document("$type", "number")
                        )
                    ))
            );
            mongoTemplate.indexOps(MessageCreatedOutbox.class).ensureIndex(
                new Index()
                    .on("messageId", Direction.ASC)
                    .named("message_id_unique_idx")
                    .unique()
            );
        };
    }
}
