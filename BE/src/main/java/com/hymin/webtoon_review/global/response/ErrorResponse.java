package com.hymin.webtoon_review.global.response;

public class ErrorResponse extends RestResponse {

    protected ErrorResponse(Integer status, String message) {
        super(status, message);
    }

    public static ErrorResponse of(ResponseStatus responseStatus) {
        return new ErrorResponse(responseStatus.getHttpStatusValue(), responseStatus.getMessage());
    }
}
