package com.hymin.webtoon_review.global.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> extends RestResponse {

    private T data;

    protected ApiResponse(Integer status, String message, T data) {
        super(status, message);
        this.data = data;
    }

    public static <T> ApiResponse<T> onSuccess(T data) {
        return ApiResponse.of(ResponseStatus.OK, data);
    }

    public static <T> ApiResponse<T> of(ResponseStatus responseStatus, T data) {
        return new ApiResponse<>(
            responseStatus.getHttpStatusValue(),
            responseStatus.getMessage(),
            data
        );
    }
}
