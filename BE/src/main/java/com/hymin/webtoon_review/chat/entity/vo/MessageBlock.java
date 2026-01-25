package com.hymin.webtoon_review.chat.entity.vo;

import com.hymin.webtoon_review.chat.entity.enums.MessageBlockType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBlock {

    private MessageBlockType type;
    private String content;
    private Map<String, Object> metadata;
}
