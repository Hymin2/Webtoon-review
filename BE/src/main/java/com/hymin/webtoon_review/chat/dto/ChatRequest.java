package com.hymin.webtoon_review.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatRequest {

    public enum ChatType {
        JOIN, MESSAGE, IMAGE, FILE, EXIT
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {

        private Long id;
        private Long roomId;
        private ChatType type;
        @Setter
        private String sender;
        private String message;
        private String uuid;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinExitMessage {

        private Long roomId;
        private Long lastReadId;
        private ChatType type;
        private String uuid;
    }
}
