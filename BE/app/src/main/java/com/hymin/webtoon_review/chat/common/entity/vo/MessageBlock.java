package com.hymin.webtoon_review.chat.common.entity.vo;

import com.hymin.webtoon_review.chat.common.entity.enums.MessageBlockType;
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
