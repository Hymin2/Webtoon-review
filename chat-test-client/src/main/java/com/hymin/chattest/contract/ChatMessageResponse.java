package com.hymin.chattest.contract;

import java.util.List;

public record ChatMessageResponse(
    String messageId,
    Long roomId,
    Long messageSequence,
    List<MessageBlock> messageBlocks,
    String roomMemberId,
    String clientMessageId,
    String sender,
    String createdAt
) {
}
