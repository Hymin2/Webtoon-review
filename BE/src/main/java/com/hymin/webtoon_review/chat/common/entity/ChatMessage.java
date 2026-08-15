package com.hymin.webtoon_review.chat.common.entity;

import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import jakarta.persistence.Id;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndexes({
    @CompoundIndex(
        name = "room_sender_client_message_unique_idx",
        def = "{'roomId': 1, 'senderId': 1, 'clientMessageId': 1}",
        unique = true
    ),
    @CompoundIndex(
        name = "room_message_sequence_unique_idx",
        def = "{'roomId': 1, 'messageSequence': -1}",
        unique = true,
        partialFilter = "{'messageSequence': {'$type': 'number'}}"
    )
})
public class ChatMessage {

    @Id
    private String id;
    private Long roomId;
    private Long senderId;
    private String roomMemberId;
    private Long messageSequence;
    private String clientMessageId;
    private String traceId;
    private String sender;
    private String createdAt;
    private List<MessageBlock> messageBlocks;
}
