package com.hymin.webtoon_review.chat.common.dto;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDispatchDto {

    private Long senderId;
    private String traceId;
    private ChatMessageResponse chatMessageResponse;
    private List<String> userIds;
}
