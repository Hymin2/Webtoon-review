package com.hymin.chattest.contract;

import java.util.List;

public record ChatMessageRequest(
    Long roomId,
    String personalUUID,
    List<MessageBlock> messageBlocks
) {
}
