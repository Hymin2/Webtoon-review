package com.hymin.webtoon_review.chat.server.dto;

import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatMessageRequest {

    @NotBlank
    @Size(max = 36)
    private String clientMessageId;
    private List<MessageBlock> messageBlocks;
}
