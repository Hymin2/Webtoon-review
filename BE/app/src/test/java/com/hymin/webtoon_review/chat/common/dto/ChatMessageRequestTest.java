package com.hymin.webtoon_review.chat.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatMessageRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void clientMessageId는_필수이다() {
        ChatMessageRequest request = new ChatMessageRequest(1L, null, List.of());

        assertThat(validator.validate(request))
            .anyMatch(violation -> violation.getPropertyPath().toString()
                .equals("clientMessageId"));
    }

    @Test
    void clientMessageId는_UUID_길이를_초과할_수_없다() {
        ChatMessageRequest request = new ChatMessageRequest(
            1L, "a".repeat(37), List.of());

        assertThat(validator.validate(request))
            .anyMatch(violation -> violation.getPropertyPath().toString()
                .equals("clientMessageId"));
    }
}
