package com.hymin.webtoon_review.chat.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.Document;

class MessageCreatedOutboxTest {

    @Test
    void usesDedicatedOutboxCollectionAndStableEventContract() {
        Document document = MessageCreatedOutbox.class.getAnnotation(Document.class);

        assertThat(document.collection()).isEqualTo("chat_message_outbox");
        assertThat(MessageCreatedOutbox.EVENT_TYPE).isEqualTo("MessageCreated");
        assertThat(MessageCreatedOutbox.SCHEMA_VERSION).isEqualTo(1);
    }
}
