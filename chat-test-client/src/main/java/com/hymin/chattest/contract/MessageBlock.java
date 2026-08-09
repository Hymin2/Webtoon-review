package com.hymin.chattest.contract;

import java.util.Map;

public record MessageBlock(
    MessageBlockType type,
    String content,
    Map<String, Object> metadata
) {
}
