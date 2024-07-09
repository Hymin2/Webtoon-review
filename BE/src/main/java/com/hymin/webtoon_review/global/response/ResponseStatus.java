package com.hymin.webtoon_review.global.response;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ResponseStatus {
    OK(HttpStatus.OK, "정상 처리 됐습니다.");

    private HttpStatus httpStatus;
    private String message;

    public int getStatus() {
        return httpStatus.value();
    }

    public String getMessage() {
        return message;
    }
}
