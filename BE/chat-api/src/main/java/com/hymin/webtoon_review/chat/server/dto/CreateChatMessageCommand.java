package com.hymin.webtoon_review.chat.server.dto;

import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import java.util.List;

public record CreateChatMessageCommand(
    Long roomId,
    Long senderId,
    String sender,
    String clientMessageId,
    List<MessageBlock> messageBlocks
) {
}
