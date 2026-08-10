package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.entity.vo.MessageBlock;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class ChatResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomListResponse {

        private Long roomId;
        private String roomName;
        private String roomMemberId;
        private String lastChatMessage;
        private String lastChatMessageCreatedAt;
        private Integer unreadCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomJoinResponse {

        private Long roomId;
        private String roomMemberId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomResponse {

        private Long roomId;
        private String roomName;
        private Long totalCount;
        private Long onlineCount;
        private Map<Long, Long> lastReadCountMap;
    }

    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageResponse {

        private Long roomId;
        private Long messageSequence;
        private List<MessageBlock> messageBlocks;
        private String roomMemberId;
        private String clientMessageId;
        private String sender;
        private String createdAt;
    }
}
