package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.entity.vo.MessageBlock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
        @NotBlank
        @Size(max = 36)
        private String clientMessageId;
        private List<MessageBlock> messageBlocks;
    }
}
