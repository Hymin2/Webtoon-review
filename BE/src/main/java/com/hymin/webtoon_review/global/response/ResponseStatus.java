package com.hymin.webtoon_review.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResponseStatus {
    OK(HttpStatus.OK, "정상 처리 됐습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatus() {
        return httpStatus.value();
    }

    public String getMessage() {
        return message;
    }
}
