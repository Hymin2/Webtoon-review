package com.hymin.webtoon_review.global.exception;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GeneralException extends RuntimeException {

    private final ResponseStatus responseStatus;
}
