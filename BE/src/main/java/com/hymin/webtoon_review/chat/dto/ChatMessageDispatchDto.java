package com.hymin.webtoon_review.chat.dto;

import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatMessageResponse;
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

    ChatMessageResponse chatMessageResponse;
    List<String> userIds;
}
