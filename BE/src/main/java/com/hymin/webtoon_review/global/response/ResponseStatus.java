package com.hymin.webtoon_review.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResponseStatus {
    OK(HttpStatus.OK, "정상 처리 됐습니다."),
    CREATED(HttpStatus.CREATED, "정상 처리 됐습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "정상 처리 됐습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),
    INVALID_JSON_WEB_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    WEBTOON_NOT_FOUND(HttpStatus.NOT_FOUND, "웹툰을 찾을 수 없습니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "추천을 찾을 수 없습니다."),

    ALREADY_USER_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusValue() {
        return httpStatus.value();
    }
}
