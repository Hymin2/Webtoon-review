package com.hymin.webtoon_review.chat.dto;

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
        private Map<Object, Object> readCountMap;
    }
}
