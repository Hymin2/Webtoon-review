package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.enums.ChatType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {

        private Long roomId;
        private ChatType type;
        @Setter
        private String sender;
        private String message;
        private String personalUUID;
        private String messageUUID;
        private String createdAt;

        public static ChatMessage from(RetryMessage retryMessage) {
            return new ChatMessage(
                retryMessage.getRoomId(),
                retryMessage.getType(),
                retryMessage.getSender(),
                retryMessage.getMessage(),
                retryMessage.getPersonalUUID(),
                retryMessage.getMessageUUID(),
                retryMessage.getCreatedAt()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryMessage {

        private Long roomId;
        private ChatType type;
        private String receiver;
        private String sender;
        private String message;
        private String personalUUID;
        private String messageUUID;
        private String createdAt;
        private Integer retryCount;
        private LocalDateTime lastAttemptTime;

        public static RetryMessage from(String receiver, ChatMessage chatMessage) {
            return new RetryMessage(
                chatMessage.getRoomId(),
                chatMessage.getType(),
                receiver,
                chatMessage.getSender(),
                chatMessage.getMessage(),
                chatMessage.getPersonalUUID(),
                chatMessage.getMessageUUID(),
                chatMessage.getCreatedAt(),
                0,
                LocalDateTime.now()
            );
        }

        public void increaseRetryCount() {
            this.retryCount++;
        }

        public void updateLastAttemptTime() {
            this.lastAttemptTime = LocalDateTime.now();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AckMessage {

        private String messageUUID;
    }
}
