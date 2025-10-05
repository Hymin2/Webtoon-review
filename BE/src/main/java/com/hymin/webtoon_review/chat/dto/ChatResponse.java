package com.hymin.webtoon_review.chat.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomInfo {

        private Long roomId;
        private String personalUUID;
        private Integer userCount;
        private Integer connectedUserCount;
        private Map<LocalDateTime, Long> readCountMap;
    }
}
