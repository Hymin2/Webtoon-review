package com.hymin.webtoon_review.chat.common.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.webtoon_review.chat.common.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import org.junit.jupiter.api.Test;

class ChatMapperTest {

    @Test
    void preservesServerMessageIdUntilPersistence() {
        ChatMessageDto dto = ChatMessageDto.builder()
            .messageId("server-message-id")
            .roomId(1L)
            .roomMemberId("room-member-id")
            .clientMessageId("client-message-id")
            .senderNickname("sender")
            .build();

        ChatMessageResponse response = ChatMapper.toChatMessageResponse(
            dto,
            null,
            "2026-08-14T03:00:00"
        );
        ChatMessage message = ChatMapper.toChatMessage(response, 2L, "trace-id");

        assertThat(response.getMessageId()).isEqualTo("server-message-id");
        assertThat(message.getId()).isEqualTo("server-message-id");
        assertThat(message.getClientMessageId()).isEqualTo("client-message-id");
        assertThat(message.getRoomMemberId()).isEqualTo("room-member-id");
        assertThat(message.getSender()).isEqualTo("sender");
    }
}
