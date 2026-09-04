package com.hymin.webtoon_review.chat.server.dto;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatMessageResponse {

    private final String messageId;
    private final Long roomId;
    private final Long messageSequence;
    private final String clientMessageId;
    private final String createdAt;

    public static CreateChatMessageResponse from(ChatMessage message) {
        return CreateChatMessageResponse.builder()
            .messageId(message.getId())
            .roomId(message.getRoomId())
            .messageSequence(message.getMessageSequence())
            .clientMessageId(message.getClientMessageId())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
