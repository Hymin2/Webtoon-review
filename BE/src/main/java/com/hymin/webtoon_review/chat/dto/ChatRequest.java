package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.entity.vo.MessageBlock;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class ChatRequest {

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRequest {

        private Long roomId;
        private String personalUUID;
        private List<MessageBlock> messageBlocks;
    }
}
