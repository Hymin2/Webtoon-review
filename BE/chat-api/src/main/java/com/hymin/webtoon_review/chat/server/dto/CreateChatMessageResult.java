package com.hymin.webtoon_review.chat.server.dto;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;

public record CreateChatMessageResult(boolean created, ChatMessage message) {
}
