package com.hymin.webtoon_review.chat.common.entity;

import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message_outbox")
public class MessageCreatedOutbox {

    public static final String EVENT_TYPE = "MessageCreated";
    public static final int SCHEMA_VERSION = 1;

    @Id
    private String eventId;
    private String eventType;
    private int schemaVersion;
    @Indexed(name = "message_id_unique_idx", unique = true)
    private String messageId;
    private Long roomId;
    private Long roomSequence;
    private Long senderId;
    private String roomMemberId;
    private String clientMessageId;
    private String sender;
    private List<MessageBlock> messageBlocks;
    private String messageCreatedAt;
    private String occurredAt;
    private String publishedAt;
}
