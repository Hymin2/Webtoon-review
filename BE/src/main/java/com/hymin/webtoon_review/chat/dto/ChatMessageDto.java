package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.entity.vo.MessageBlock;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long roomId;
    private Long senderId;
    private List<MessageBlock> messageBlocks;
    private String traceId;
    private String personalUUID;
    private String messageUUID;
    private String senderNickname;
}
