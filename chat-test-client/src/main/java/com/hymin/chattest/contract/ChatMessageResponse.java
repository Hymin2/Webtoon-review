package com.hymin.chattest.contract;

import java.util.List;

public record ChatMessageResponse(
    Long roomId,
    Long messageSequence,
    List<MessageBlock> messageBlocks,
    String personalUUID,
    String messageUUID,
    String sender,
    String createdAt
) {
}
