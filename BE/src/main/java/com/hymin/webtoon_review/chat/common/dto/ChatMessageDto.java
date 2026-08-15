package com.hymin.webtoon_review.chat.common.dto;

import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
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

    private String messageId;
    private Long roomId;
    private Long senderId;
    private List<MessageBlock> messageBlocks;
    private String traceId;
    private String roomMemberId;
    private String clientMessageId;
    private String senderNickname;
}
