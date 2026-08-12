package com.hymin.webtoon_review.chat.common.entity;

import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import jakarta.persistence.Id;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndex(name = "room_seq_idx", def = "{'roomId': 1, 'sequence': -1}")
public class ChatMessage {

    @Id
    private String id;
    private Long roomId;
    private Long senderId;
    private Long messageSequence;
    @Indexed(unique = true)
    private String clientMessageId;
    private String traceId;
    private String sender;
    private String createdAt;
    private List<MessageBlock> messageBlocks;
}
