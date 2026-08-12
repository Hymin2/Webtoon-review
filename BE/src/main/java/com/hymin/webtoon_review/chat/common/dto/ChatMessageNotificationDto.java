package com.hymin.webtoon_review.chat.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageNotificationDto {

    private Long roomId;
    private String roomName;
    private String sender;
    private String content;
    private List<Long> ids;
}
