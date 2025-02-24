package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatRequest {


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {

        @Setter
        private String id;
        private Long roomId;
        private ChatType type;
        @Setter
        private String sender;
        private String message;
        private String personalUUID;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectDisConnectMessage {

        private Long roomId;
        private ChatType type;
        private String personalUUID;
        private String lastMessageUUID;
    }
}
