package com.hymin.webtoon_review.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResponseStatus {
    OK(HttpStatus.OK, "정상 처리 됐습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusValue() {
        return httpStatus.value();
    }
}
